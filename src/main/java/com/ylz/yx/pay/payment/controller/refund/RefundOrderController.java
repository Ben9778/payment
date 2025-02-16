package com.ylz.yx.pay.payment.controller.refund;

import com.ylz.core.logging.Logger;
import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.yx.pay.core.entity.PayFwqd00;
import com.ylz.yx.pay.core.exception.BizException;
import com.ylz.yx.pay.core.model.ApiRes;
import com.ylz.yx.pay.payment.channel.IRefundService;
import com.ylz.yx.pay.payment.controller.ApiController;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.core.entity.PayTkdd00;
import com.ylz.yx.pay.payment.exception.ChannelException;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import com.ylz.yx.pay.payment.rqrs.refund.RefundOrderRQ;
import com.ylz.yx.pay.payment.rqrs.refund.RefundOrderRS;
import com.ylz.yx.pay.payment.service.ConfigContextQueryService;
import com.ylz.yx.pay.payment.service.PayMchNotifyService;
import com.ylz.yx.pay.payment.service.impl.PayOrderService;
import com.ylz.yx.pay.payment.service.impl.RefundOrderService;
import com.ylz.yx.pay.utils.SeqKit;
import com.ylz.yx.pay.utils.SpringBeansUtil;
import com.ylz.yx.pay.utils.StringKit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/*
* 商户发起退款 controller
*/
@RestController
public class RefundOrderController extends ApiController {

    private static final Logger log = new Logger(RefundOrderController.class.getName());

    @Autowired private PayOrderService payOrderService;
    @Autowired private RefundOrderService refundOrderService;
    @Autowired private PayMchNotifyService payMchNotifyService;
    @Autowired private ConfigContextQueryService configContextQueryService;
    @Autowired private JdbcGateway jdbcGateway;

    /** 申请退款 **/
    @PostMapping("/api/refund/refundOrder")
    public ApiRes refundOrder(){
        PayTkdd00 payTkdd00 = null;

        //获取参数 & 验签
        RefundOrderRQ rq = getRQByWithMchSign(RefundOrderRQ.class);

        try {
            if(StringUtils.isAllEmpty(rq.getMchOrderNo(), rq.getPayOrderId())){
                throw new BizException("mchOrderNo 和 payOrderId不能同时为空");
            }
            PayZfdd00 payZfdd00 = payOrderService.queryMchOrder(rq.getPayOrderId(), rq.getMchOrderNo());
            if(payZfdd00 == null){
                throw new BizException("退款订单不存在");
            }
            if(!payZfdd00.getDdzt00().equals(PayZfdd00.STATE_SUCCESS)){
                throw new BizException("订单状态不正确， 无法完成退款");
            }
            if(payZfdd00.getTkzt00().equals(PayZfdd00.REFUND_STATE_ALL) || payZfdd00.getTkzje0() >= payZfdd00.getZfje00()){
                throw new BizException("订单已全额退款，本次申请失败");
            }
            if(payZfdd00.getTkzje0() + rq.getRefundAmount() > payZfdd00.getZfje00()){
                throw new BizException("申请金额超出订单可退款余额，请检查退款金额");
            }
            Integer tksqCount = jdbcGateway.selectOne("pay.tkdd00.selectIngCount", payZfdd00.getXtddh0());
            if(tksqCount > 0){
                throw new BizException("支付订单已存在退款申请，请稍后再试");
            }
            //全部退款金额 （退款订单表）
            Long sumSuccessRefundAmount = jdbcGateway.selectOne("pay.tkdd00.sumSuccessRefundAmount",payZfdd00.getXtddh0());
            if(sumSuccessRefundAmount >= payZfdd00.getZfje00()){
                throw new BizException("退款单已完成全部订单退款，本次申请失败");
            }
            if(sumSuccessRefundAmount + rq.getRefundAmount() > payZfdd00.getZfje00()){
                throw new BizException("申请金额超出订单可退款余额，请检查退款金额");
            }
            String appId = rq.getAppId();

            // 校验退款单号是否重复
            Integer tkddh0Count = jdbcGateway.selectOne("pay.tkdd00.selectCountByFwddh0", rq.getMchRefundNo());
            if(tkddh0Count > 0){
                throw new BizException("商户退款订单号["+rq.getMchRefundNo()+"]已存在");
            }
            if(StringUtils.isNotEmpty(rq.getNotifyUrl()) && !StringKit.isAvailableUrl(rq.getNotifyUrl())){
                throw new BizException("异步通知地址协议仅支持http:// 或 https:// !");
            }
            //获取支付参数 (缓存数据) 和 商户信息
            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(appId);
            if(mchAppConfigContext == null){
                throw new BizException("获取商户应用信息失败");
            }
            PayFwqd00 payFwqd00 = mchAppConfigContext.getPayFwqd00();

            //获取退款接口
            IRefundService refundService = SpringBeansUtil.getBean(payZfdd00.getZfqd00() + "RefundService", IRefundService.class);
            if(refundService == null){
                throw new BizException("当前通道不支持退款！");
            }
            payTkdd00 = genRefundOrder(rq, payZfdd00, payFwqd00);
            //退款单入库 退款单状态：生成状态  此时没有和任何上游渠道产生交互。
            jdbcGateway.insert("pay.tkdd00.insertSelective", payTkdd00);
            // 调起退款接口
            ChannelRetMsg channelRetMsg = refundService.refund(rq, payTkdd00, payZfdd00, mchAppConfigContext);
            //处理退款单状态
            this.processChannelMsg(channelRetMsg, payTkdd00);
            RefundOrderRS bizRes = RefundOrderRS.buildByRefundOrder(payTkdd00);
            return ApiRes.okWithSign(bizRes, configContextQueryService.queryMchApp(rq.getAppId()).getQdmy00());
        } catch (BizException e) {
            return ApiRes.customFail(e.getMessage());
        } catch (ChannelException e) {
            //处理上游返回数据
            this.processChannelMsg(e.getChannelRetMsg(), payTkdd00);
            if(e.getChannelRetMsg().getChannelState() == ChannelRetMsg.ChannelState.SYS_ERROR ){
                return ApiRes.customFail(e.getMessage());
            }
            RefundOrderRS bizRes = RefundOrderRS.buildByRefundOrder(payTkdd00);
            return ApiRes.okWithSign(bizRes, configContextQueryService.queryMchApp(rq.getAppId()).getQdmy00());
        } catch (Exception e) {
            log.error("系统异常：{}", e);
            return ApiRes.customFail("系统异常");
        }
    }

    private PayTkdd00 genRefundOrder(RefundOrderRQ rq, PayZfdd00 payZfdd00, PayFwqd00 payFwqd00){

        Date nowTime = new Date();
        PayTkdd00 payTkdd00 = new PayTkdd00();
        payTkdd00.setFwqdid(payFwqd00.getId0000()); //商户应用ID
        if(StringUtils.isNotBlank(rq.getMchNo())){
            payTkdd00.setXtddh0(rq.getMchNo()); //退款订单号
        } else {
            payTkdd00.setXtddh0(SeqKit.genRefundOrderId()); //退款订单号
        }

        payTkdd00.setFwddh0(rq.getMchRefundNo()); //商户退款单号
        payTkdd00.setZfddh0(null); //渠道订单号

        payTkdd00.setYxtddh(payZfdd00.getXtddh0()); //支付订单号
        payTkdd00.setYfwddh(payZfdd00.getFwddh0()); //商户订单号
        payTkdd00.setYzfddh(payZfdd00.getZfddh0()); //渠道支付单号

        payTkdd00.setZfqd00(payZfdd00.getZfqd00()); //支付接口代码
        payTkdd00.setZffs00(payZfdd00.getZffs00()); //支付方式代码
        payTkdd00.setTkje00(rq.getRefundAmount()); //退款金额,单位分
        payTkdd00.setTkzt00(PayTkdd00.STATE_INIT); //退款状态:0-订单生成,1-退款中,2-退款成功,3-退款失败
        payTkdd00.setTkyy00(rq.getRefundReason()); //退款原因
        payTkdd00.setDdcjsj(nowTime); //创建时间
        payTkdd00.setTkcgsj(null); //订单退款成功时间

        payTkdd00.setYbtzdz(rq.getNotifyUrl()); //通知地址
        payTkdd00.setQdcs00(rq.getChannelExtra()); //渠道参数
        payTkdd00.setKzcs00(rq.getExtParam()); //扩展参数

        payTkdd00.setCzyid0(rq.getOperatorId()); //操作员ID
        payTkdd00.setCzyxm0(rq.getOperatorName()); //操作员姓名

        payTkdd00.setYyid00(rq.getHospitalId()); //医院ID

        payTkdd00.setZfcwm0(null); //渠道错误码
        payTkdd00.setZfcwms(null); //渠道错误描述


        //payTkdd00.setExpiredTime(DateUtil.offsetHour(nowTime, 2)); //订单超时关闭时间 默认两个小时



        return payTkdd00;
    }


    /** 处理返回的渠道信息，并更新退款单状态
     *  payOrder将对部分信息进行 赋值操作。
     * **/
    private void processChannelMsg(ChannelRetMsg channelRetMsg, PayTkdd00 payTkdd00){

        //对象为空 || 上游返回状态为空， 则无需操作
        if(channelRetMsg == null || channelRetMsg.getChannelState() == null){
            return ;
        }

        //明确成功
        if(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS == channelRetMsg.getChannelState()) {
            this.updateInitOrderStateThrowException(PayTkdd00.STATE_SUCCESS, payTkdd00, channelRetMsg);
            payMchNotifyService.refundOrderNotify(payTkdd00);
            //明确失败
        }else if(ChannelRetMsg.ChannelState.CONFIRM_FAIL == channelRetMsg.getChannelState()) {

            this.updateInitOrderStateThrowException(PayTkdd00.STATE_FAIL, payTkdd00, channelRetMsg);
            payMchNotifyService.refundOrderNotify(payTkdd00);
            // 上游处理中 || 未知 || 上游接口返回异常  退款单为退款中状态
        }else if( ChannelRetMsg.ChannelState.WAITING == channelRetMsg.getChannelState() ||
                ChannelRetMsg.ChannelState.UNKNOWN == channelRetMsg.getChannelState() ||
                ChannelRetMsg.ChannelState.API_RET_ERROR == channelRetMsg.getChannelState()
        ){
            this.updateInitOrderStateThrowException(PayTkdd00.STATE_ING, payTkdd00, channelRetMsg);

            // 系统异常：  退款单不再处理。  为： 生成状态
        }else if( ChannelRetMsg.ChannelState.SYS_ERROR == channelRetMsg.getChannelState() ){

        }else{

            throw new BizException("ChannelState 返回异常！");
        }

    }


    /** 更新退款单状态 --》 退款单生成--》 其他状态  (向外抛出异常) **/
    private void updateInitOrderStateThrowException(String orderState, PayTkdd00 payTkdd00, ChannelRetMsg channelRetMsg){

        payTkdd00.setTkzt00(orderState);
        payTkdd00.setZfddh0(channelRetMsg.getChannelOrderId());
        payTkdd00.setZfcwm0(channelRetMsg.getChannelErrCode());
        payTkdd00.setZfcwms(channelRetMsg.getChannelErrMsg());


        boolean isSuccess = refundOrderService.updateInit2Ing(payTkdd00.getXtddh0(), channelRetMsg.getChannelOrderId());
        if(!isSuccess){
            throw new BizException("更新退款单异常!");
        }

        isSuccess = refundOrderService.updateIng2SuccessOrFail(payTkdd00.getXtddh0(), payTkdd00.getTkzt00(),
                channelRetMsg.getChannelOrderId(), channelRetMsg.getChannelErrCode(), channelRetMsg.getChannelErrMsg());
        if(!isSuccess){
            throw new BizException("更新退款单异常!");
        }
    }

}
