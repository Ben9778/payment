package com.ylz.yx.pay.payment.controller.payorder;

import cn.hutool.core.date.DateUtil;
import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.yx.pay.config.ApplicationProperty;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.entity.PayFwqd00;
import com.ylz.yx.pay.core.entity.PayFwzfgx;
import com.ylz.yx.pay.core.entity.PayZfqd00;
import com.ylz.yx.pay.core.exception.BizException;
import com.ylz.yx.pay.core.model.ApiRes;
import com.ylz.yx.pay.mq.model.PayOrderReissueMQ;
import com.ylz.yx.pay.mq.vender.IMQSender;
import com.ylz.yx.pay.payment.channel.IPaymentService;
import com.ylz.yx.pay.payment.controller. ApiController;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.payment.exception.ChannelException;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import com.ylz.yx.pay.payment.rqrs.payorder.UnifiedOrderRQ;
import com.ylz.yx.pay.payment.rqrs.payorder.UnifiedOrderRS;
import com.ylz.yx.pay.payment.rqrs.payorder.payway.QrCashierOrderRQ;
import com.ylz.yx.pay.payment.rqrs.payorder.payway.QrCashierOrderRS;
import com.ylz.yx.pay.payment.service.ConfigContextQueryService;
import com.ylz.yx.pay.payment.service.PayOrderProcessService;
import com.ylz.yx.pay.payment.service.impl.PayOrderService;
import com.ylz.yx.pay.utils.SeqKit;
import com.ylz.yx.pay.utils.SpringBeansUtil;
import com.ylz.yx.pay.utils.StringKit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*
* 创建支付订单抽象类
*/
public abstract class AbstractPayOrderController extends ApiController {

    @Autowired private PayOrderService payOrderService;
    @Autowired private ConfigContextQueryService configContextQueryService;
    @Autowired private PayOrderProcessService payOrderProcessService;
    //@Autowired private ChannelOrderReissueService channelOrderReissueService;
    @Autowired private JdbcGateway jdbcGateway;
    @Resource protected ApplicationProperty applicationProperty;
    @Autowired private IMQSender mqSender;


    /** 统一下单 (新建订单模式) **/
    protected ApiRes unifiedOrder(String wayCode, UnifiedOrderRQ bizRQ){
        return unifiedOrder(wayCode, bizRQ, null);
    }

    /** 统一下单 **/
    protected ApiRes unifiedOrder(String wayCode, UnifiedOrderRQ bizRQ, PayZfdd00 payZfdd00){

        // 响应数据
        UnifiedOrderRS bizRS = null;

        //是否新订单模式 [  一般接口都为新订单模式，  由于QR_CASHIER支付方式，需要先 在DB插入一个新订单， 导致此处需要特殊判断下。 如果已存在则直接更新，否则为插入。  ]
        boolean isNewOrder = payZfdd00 == null;

        try {

            if(payZfdd00 != null){ //当订单存在时，封装公共参数。

                if(!payZfdd00.getDdzt00().equals(PayZfdd00.STATE_INIT)){
                    throw new BizException("订单状态异常");
                }

                payZfdd00.setZffs00(wayCode); // 需要将订单更新 支付方式
                payZfdd00.setQdyhbs(bizRQ.getChannelUserId()); //更新渠道用户信息
                bizRQ.setAppId(payZfdd00.getFwqdid());
                bizRQ.setMchOrderNo(payZfdd00.getFwddh0());
                bizRQ.setWayCode(wayCode);
                bizRQ.setAmount(payZfdd00.getZfje00());
                bizRQ.setSubject(payZfdd00.getDdmc00());
                bizRQ.setNotifyUrl(payZfdd00.getYbtzdz());
                bizRQ.setReturnUrl(payZfdd00.getYmtzdz());
                bizRQ.setChannelExtra(payZfdd00.getQdcs00());
                bizRQ.setExtParam(payZfdd00.getKzcs00());
            }

            String appId = bizRQ.getAppId();

            // 只有新订单模式，进行校验
            PayZfdd00 isPayZfdd00 = payOrderService.queryMchOrder(null, bizRQ.getMchOrderNo());//根据商户号查询订单
            if(isNewOrder && isPayZfdd00 != null){//订单已经存在抛出异常提示
                throw new BizException("商户订单["+bizRQ.getMchOrderNo()+"]已存在");
            }
            //如果异步通知地址不为空,并且访问协议不是http或https,抛出异常提示
            if(StringUtils.isNotEmpty(bizRQ.getNotifyUrl()) && !StringKit.isAvailableUrl(bizRQ.getNotifyUrl())){
                throw new BizException("异步通知地址协议仅支持http:// 或 https:// !");
            }
            //如果同步通知地址不为空,并且访问协议不是http或https,抛出异常提示
            if(StringUtils.isNotEmpty(bizRQ.getReturnUrl()) && !StringKit.isAvailableUrl(bizRQ.getReturnUrl())){
                throw new BizException("同步通知地址协议仅支持http:// 或 https:// !");
            }

            //获取支付参数 (缓存数据) 和 商户信息
            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(appId);
            if(mchAppConfigContext == null){
                throw new BizException("获取商户应用信息失败");
            }

            PayFwqd00 payFwqd00 = mchAppConfigContext.getPayFwqd00();

            //收银台支付并且只有新订单需要走这里，  收银台二次下单的wayCode应该为实际支付方式。
            if(isNewOrder && CS.PAY_WAY_CODE.QR_CASHIER.equals(wayCode)){

                //生成订单
                payZfdd00 = genPayOrder(bizRQ, payFwqd00, null);
                String payOrderId = payZfdd00.getXtddh0();
                //订单入库 订单状态： 生成状态  此时没有和任何上游渠道产生交互。
                jdbcGateway.insert("pay.zfdd00.insertSelective", payZfdd00);

                QrCashierOrderRS qrCashierOrderRS = new QrCashierOrderRS();
                QrCashierOrderRQ qrCashierOrderRQ = (QrCashierOrderRQ)bizRQ;

                String payUrl = applicationProperty.genUniJsapiPayUrl(payOrderId);
                if(CS.PAY_DATA_TYPE.CODE_IMG_URL.equals(qrCashierOrderRQ.getPayDataType())){ //二维码地址
                    qrCashierOrderRS.setCodeImgUrl(applicationProperty.genScanImgUrl(payUrl));

                }else{ //默认都为跳转地址方式
                    qrCashierOrderRS.setCodeUrl(payUrl);
                }

                return packageApiResByPayOrder(bizRQ, qrCashierOrderRS, payZfdd00);
            }

            // 根据支付方式， 查询出 该商户 可用的支付接口
            Map<String, Object> map = new HashMap<>();
            map.put("fwqdid", appId);
            map.put("zffs00", wayCode);
            //根据服务渠道ID和支付方式查询支付接口信息
            PayFwzfgx payFwzfgx = jdbcGateway.selectOne("pay.fwzfgx.selectByFwqdidAndZffs00", map);
            if(payFwzfgx == null){
                throw new BizException("渠道不支持该支付方式");
            }

            //获取支付接口
            IPaymentService paymentService = checkMchWayCodeAndGetService(mchAppConfigContext, payFwzfgx);
            String ifCode = paymentService.getIfCode();

            //生成订单
            if(isNewOrder){
                payZfdd00 = genPayOrder(bizRQ, payFwqd00, ifCode);
            }else{
                payZfdd00.setZfqd00(ifCode);
            }

            //预先校验
            String errMsg = paymentService.preCheck(bizRQ, payZfdd00);
            if(StringUtils.isNotEmpty(errMsg)){
                throw new BizException(errMsg);
            }

            if(isNewOrder){
                //订单入库 订单状态： 生成状态  此时没有和任何上游渠道产生交互。
                jdbcGateway.insert("pay.zfdd00.insertSelective", payZfdd00);
            }

            //调起上游支付接口
            bizRS = (UnifiedOrderRS) paymentService.pay(bizRQ, payZfdd00, mchAppConfigContext);

            //处理上游返回数据
            this.processChannelMsg(bizRS.getChannelRetMsg(), payZfdd00);

            return packageApiResByPayOrder(bizRQ, bizRS, payZfdd00);

        } catch (BizException e) {
            return ApiRes.customFail(e.getMessage());

        } catch (ChannelException e) {

            //处理上游返回数据
            this.processChannelMsg(e.getChannelRetMsg(), payZfdd00);

            if(e.getChannelRetMsg().getChannelState() == ChannelRetMsg.ChannelState.SYS_ERROR ){
                return ApiRes.customFail(e.getMessage());
            }

            return this.packageApiResByPayOrder(bizRQ, bizRS, payZfdd00);


        } catch (Exception e) {
            logger.info("系统异常：{}", e);
            return ApiRes.customFail("系统异常");
        }
    }

    private PayZfdd00 genPayOrder(UnifiedOrderRQ rq, PayFwqd00 payFwqd00, String ifCode){

        PayZfdd00 payZfdd00 = new PayZfdd00();
        payZfdd00.setFwqdid(payFwqd00.getId0000()); //商户应用appId
        payZfdd00.setXtddh0(SeqKit.genPayOrderId()); //生成订单ID
        payZfdd00.setFwddh0(rq.getMchOrderNo()); //商户订单号
        payZfdd00.setHzxm00(rq.getUserName()); //患者姓名
        payZfdd00.setHzsjh0(rq.getPhone()); //患者手机号
        payZfdd00.setKhzyh0(rq.getCardNo()); //就诊卡号/住院号
        payZfdd00.setHzsfzh(rq.getIdCard()); //患者身份证号
        payZfdd00.setDdlx00(rq.getOrderType()); //订单类型
        if("01".equals(rq.getOrderType())){
            payZfdd00.setDdmc00("门诊-"+rq.getSubject()); //商品标题
        } else if("02".equals(rq.getOrderType())){
            payZfdd00.setDdmc00("住院-"+rq.getSubject()); //商品标题
        } else {
            payZfdd00.setDdmc00(rq.getSubject()); //商品标题
        }
        payZfdd00.setZfqd00(ifCode); //支付渠道
        payZfdd00.setZffs00(rq.getWayCode()); //支付方式
        payZfdd00.setZfje00(rq.getAmount()); //订单金额
        payZfdd00.setDdzt00(PayZfdd00.STATE_INIT); //订单状态, 默认订单生成状态

        Date nowDate = new Date();
        payZfdd00.setDdcjsj(nowDate); //订单创建时间
        //订单过期时间 单位： 秒
        if(rq.getExpiredTime() != null){
            payZfdd00.setDdsxsj(DateUtil.offsetSecond(nowDate, rq.getExpiredTime()));
        }else{
            payZfdd00.setDdsxsj(DateUtil.offsetMinute(nowDate, 10)); //订单过期时间 默认10分钟
        }

        payZfdd00.setYbtzdz(rq.getNotifyUrl()); //异步通知地址
        payZfdd00.setYmtzdz(rq.getReturnUrl()); //页面跳转地址
        payZfdd00.setQdcs00(rq.getChannelExtra()); //特殊渠道发起的附件额外参数
        payZfdd00.setKzcs00(rq.getExtParam()); //商户扩展参数
        payZfdd00.setQdyhbs(rq.getChannelUserId()); //渠道用户标志

        payZfdd00.setCzyid0(rq.getOperatorId()); //操作员ID
        payZfdd00.setCzyxm0(rq.getOperatorName()); //操作员姓名

        payZfdd00.setYyid00(rq.getHospitalId()); //医院ID

        return payZfdd00;
    }


    /**
     * 校验： 商户的支付方式是否可用
     * 返回： 支付接口
     * **/
    private IPaymentService checkMchWayCodeAndGetService(MchAppConfigContext mchAppConfigContext, PayFwzfgx payFwzfgx){

        // 接口代码
        String zfqdid = payFwzfgx.getZfqdid();
        PayZfqd00 payZfqd00 = jdbcGateway.selectOne("pay.zfqd00.selectByPrimaryKey", zfqdid);
        IPaymentService paymentService = SpringBeansUtil.getBean(payZfqd00.getQdbm00() + "PaymentService", IPaymentService.class);
        if(paymentService == null){
            throw new BizException("无此支付通道接口");
        }

        if(configContextQueryService.queryMchParams(mchAppConfigContext.getFwqdid(), payZfqd00.getQdbm00()) == null){
            throw new BizException("商户应用参数未配置");
        }

        return paymentService;

    }


    /** 处理返回的渠道信息，并更新订单状态
     *  payOrder将对部分信息进行 赋值操作。
     * **/
    private void processChannelMsg(ChannelRetMsg channelRetMsg, PayZfdd00 payZfdd00){

        //对象为空 || 上游返回状态为空， 则无需操作
        if(channelRetMsg == null || channelRetMsg.getChannelState() == null){
            return ;
        }

        //明确成功
        if(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS == channelRetMsg.getChannelState()) {
            this.updateInitOrderStateThrowException(PayZfdd00.STATE_SUCCESS, payZfdd00, channelRetMsg);
            //订单支付成功，其他业务逻辑
            payOrderProcessService.confirmSuccess(payZfdd00, channelRetMsg.isAutoRecharge(), channelRetMsg.isAutoSettle(), channelRetMsg.isAutoRechargeAndSettle());
        //明确失败
        }else if(ChannelRetMsg.ChannelState.CONFIRM_FAIL == channelRetMsg.getChannelState()) {
            this.updateInitOrderStateThrowException(PayZfdd00.STATE_FAIL, payZfdd00, channelRetMsg);
        // 上游处理中 || 未知 || 上游接口返回异常  订单为支付中状态
        }else if( ChannelRetMsg.ChannelState.WAITING == channelRetMsg.getChannelState() ||
                  ChannelRetMsg.ChannelState.UNKNOWN == channelRetMsg.getChannelState() ||
                  ChannelRetMsg.ChannelState.API_RET_ERROR == channelRetMsg.getChannelState()
        ){
            this.updateInitOrderStateThrowException(PayZfdd00.STATE_ING, payZfdd00, channelRetMsg);
        // 系统异常：  订单不再处理。  为： 生成状态
        }else if( ChannelRetMsg.ChannelState.SYS_ERROR == channelRetMsg.getChannelState()){

        }else{
            throw new BizException("ChannelState 返回异常！");
        }
        //判断是否需要轮询查单
        if(channelRetMsg.isNeedQuery()){
            mqSender.send(PayOrderReissueMQ.build(payZfdd00.getXtddh0(), 1), 5);
        }
    }


    /** 更新订单状态 --》 订单生成--》 其他状态  (向外抛出异常) **/
    private void updateInitOrderStateThrowException(String orderState, PayZfdd00 payZfdd00, ChannelRetMsg channelRetMsg){

        payZfdd00.setDdzt00(orderState);
        payZfdd00.setZfddh0(channelRetMsg.getChannelOrderId());
        payZfdd00.setZfcwm0(channelRetMsg.getChannelErrCode());
        payZfdd00.setZfcwms(channelRetMsg.getChannelErrMsg());

        // 聚合码场景 订单对象存在会员信息， 不可全部以上游为准。
        if(StringUtils.isNotEmpty(channelRetMsg.getChannelUserId())){
            payZfdd00.setQdyhbs(channelRetMsg.getChannelUserId());
        }

        boolean isSuccess = payOrderService.updateInit2Ing(payZfdd00.getXtddh0(), payZfdd00);
        if(!isSuccess){
            throw new BizException("更新订单异常!");
        }

        payOrderService.updateIng2SuccessOrFail(payZfdd00.getXtddh0(), payZfdd00.getDdzt00(),
                channelRetMsg.getChannelOrderId(), channelRetMsg.getChannelUserId(), channelRetMsg.getChannelErrCode(), channelRetMsg.getChannelErrMsg());
    }


    /** 统一封装订单数据  **/
    private ApiRes packageApiResByPayOrder(UnifiedOrderRQ bizRQ, UnifiedOrderRS bizRS, PayZfdd00 payZfdd00){

        // 返回接口数据
        bizRS.setPayOrderId(payZfdd00.getXtddh0());
        bizRS.setOrderState(payZfdd00.getDdzt00());
        bizRS.setMchOrderNo(payZfdd00.getFwddh0());
        bizRS.setIfCode(payZfdd00.getZfqd00());
        if(payZfdd00.getDdzt00() == PayZfdd00.STATE_FAIL){
            bizRS.setErrCode(bizRS.getChannelRetMsg() != null ? bizRS.getChannelRetMsg().getChannelErrCode() : null);
            bizRS.setErrMsg(bizRS.getChannelRetMsg() != null ? bizRS.getChannelRetMsg().getChannelErrMsg() : null);
        }

        return ApiRes.okWithSign(bizRS, configContextQueryService.queryMchApp(bizRQ.getAppId()).getQdmy00());
    }


}
