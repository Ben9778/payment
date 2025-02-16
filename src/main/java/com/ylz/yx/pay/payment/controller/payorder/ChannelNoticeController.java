package com.ylz.yx.pay.payment.controller.payorder;

import com.ylz.core.logging.Logger;
import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.yx.pay.core.ctrl.AbstractCtrl;
import com.ylz.yx.pay.core.exception.BizException;
import com.ylz.yx.pay.core.exception.ResponseException;
import com.ylz.yx.pay.payment.channel.IChannelNoticeService;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import com.ylz.yx.pay.payment.service.ConfigContextQueryService;
import com.ylz.yx.pay.payment.service.PayMchNotifyService;
import com.ylz.yx.pay.payment.service.PayOrderProcessService;
import com.ylz.yx.pay.payment.service.impl.PayOrderService;
import com.ylz.yx.pay.utils.SpringBeansUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/*
* 渠道侧的通知入口Controller 【分为同步跳转（doReturn）和异步回调(doNotify) 】
*/
@Controller
public class ChannelNoticeController extends AbstractCtrl {

    private static final Logger log = new Logger(ChannelNoticeController.class.getName());

    @Autowired private PayOrderService payOrderService;
    @Autowired private ConfigContextQueryService configContextQueryService;
    @Autowired private PayMchNotifyService payMchNotifyService;
    @Autowired private PayOrderProcessService payOrderProcessService;
    @Autowired private JdbcGateway jdbcGateway;

    /** 同步通知入口 **/
    @RequestMapping(value= {"/api/pay/return/{ifCode}", "/api/pay/return/{ifCode}/{payOrderId}"})
    public String doReturn(HttpServletRequest request, @PathVariable("ifCode") String ifCode, @PathVariable(value = "payOrderId", required = false) String urlOrderId){

        String payOrderId = null;
        String logPrefix = "进入[" +ifCode+ "]支付同步跳转：urlOrderId：["+ StringUtils.defaultIfEmpty(urlOrderId, "") + "] ";
        log.info("===== "+logPrefix+" =====");

        try {

            // 参数有误
            if(StringUtils.isEmpty(ifCode)){
                return this.toReturnPage("ifCode is empty");
            }

            //查询支付接口是否存在
            IChannelNoticeService payNotifyService = SpringBeansUtil.getBean(ifCode + "ChannelNoticeService", IChannelNoticeService.class);

            // 支付通道接口实现不存在
            if(payNotifyService == null){
                logger.info("{}, interface not exists ", logPrefix);
                return this.toReturnPage("[" + ifCode + "] interface not exists");
            }

            // 解析订单号 和 请求参数
            MutablePair<String, Object> mutablePair = payNotifyService.parseParams(request, urlOrderId, IChannelNoticeService.NoticeTypeEnum.DO_RETURN);
            if(mutablePair == null){ // 解析数据失败， 响应已处理
                logger.info("{}, mutablePair is null ", logPrefix);
                throw new BizException("解析数据异常！"); //需要实现类自行抛出ResponseException, 不应该在这抛此异常。
            }

            //解析到订单号
            payOrderId = mutablePair.left;
            log.info(logPrefix + ", 解析数据为：payOrderId:"+payOrderId+", params:"+ mutablePair.getRight());

            if(StringUtils.isNotEmpty(urlOrderId) && !urlOrderId.equals(payOrderId)){
                log.error(logPrefix + ", 订单号不匹配. urlOrderId="+urlOrderId+", payOrderId= "+ payOrderId);
                throw new BizException("订单号不匹配！");
            }

            //获取订单号 和 订单数据
            PayZfdd00 payZfdd00 = jdbcGateway.selectOne("pay.zfdd00.selectByXtddh0", payOrderId);

            // 订单不存在
            if(payZfdd00 == null){
                log.error(logPrefix+", 订单不存在. payOrderId= "+ payOrderId);
                return this.toReturnPage("支付订单不存在");
            }

            //查询出商户应用的配置信息
            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(payZfdd00.getFwqdid());

            //调起接口的回调判断
            ChannelRetMsg notifyResult = payNotifyService.doNotice(request, mutablePair.getRight(), payZfdd00, mchAppConfigContext, IChannelNoticeService.NoticeTypeEnum.DO_RETURN);

            // 返回null 表明出现异常， 无需处理通知下游等操作。
            if(notifyResult == null || notifyResult.getChannelState() == null || notifyResult.getResponseEntity() == null){
                log.error(logPrefix+ ", 处理回调事件异常  notifyResult data error, notifyResult = "+ notifyResult);
                throw new BizException("处理回调事件异常！"); //需要实现类自行抛出ResponseException, 不应该在这抛此异常。
            }

            //判断订单状态
            if(notifyResult.getChannelState() == ChannelRetMsg.ChannelState.CONFIRM_SUCCESS) {
                payZfdd00.setDdzt00(PayZfdd00.STATE_SUCCESS);
            }else if(notifyResult.getChannelState() == ChannelRetMsg.ChannelState.CONFIRM_FAIL) {
                payZfdd00.setDdzt00(PayZfdd00.STATE_FAIL);
            }

            boolean hasReturnUrl = StringUtils.isNotBlank(payZfdd00.getYbtzdz());
            log.info("===== "+logPrefix+", 订单通知完成。 payOrderId="+payOrderId+", parseState = "+notifyResult.getChannelState()+", hasReturnUrl="+hasReturnUrl+" =====");

            //包含通知地址时
            if(hasReturnUrl){
                // 重定向
                response.sendRedirect(payMchNotifyService.createReturnUrl(payZfdd00, mchAppConfigContext.getPayFwqd00().getQdmy00()));
                return null;
            }else{

                //跳转到支付成功页面
                return this.toReturnPage(null);
            }

        } catch (BizException e) {
            log.error(logPrefix+", payOrderId="+payOrderId+", BizException"+ e);
            return this.toReturnPage(e.getMessage());

        } catch (ResponseException e) {
            log.error(logPrefix+", payOrderId="+payOrderId+", ResponseException"+ e);
            return this.toReturnPage(e.getMessage());

        } catch (Exception e) {
            log.error(logPrefix+", payOrderId="+payOrderId+", 系统异常"+ e);
            return this.toReturnPage(e.getMessage());
        }
    }

    /** 异步回调入口 **/
    @ResponseBody
    @RequestMapping(value= {"/api/pay/notify/{ifCode}", "/api/pay/notify/{ifCode}/{payOrderId}"})
    public ResponseEntity doNotify(HttpServletRequest request, @PathVariable("ifCode") String ifCode, @PathVariable(value = "payOrderId", required = false) String urlOrderId){

        String payOrderId = null;
        String logPrefix = "进入[" +ifCode+ "]支付回调：urlOrderId：["+ StringUtils.defaultIfEmpty(urlOrderId, "") + "] ";
        log.info("===== "+logPrefix+" =====");

        try {

            // 参数有误
            if(StringUtils.isEmpty(ifCode)){
                return ResponseEntity.badRequest().body("ifCode is empty");
            }

            //查询支付接口是否存在
            IChannelNoticeService payNotifyService = SpringBeansUtil.getBean(ifCode + "ChannelNoticeService", IChannelNoticeService.class);

            // 支付通道接口实现不存在
            if(payNotifyService == null){
                log.error(logPrefix + ", interface not exists ");
                return ResponseEntity.badRequest().body("[" + ifCode + "] interface not exists");
            }

            // 解析订单号 和 请求参数
            MutablePair<String, Object> mutablePair = payNotifyService.parseParams(request, urlOrderId, IChannelNoticeService.NoticeTypeEnum.DO_NOTIFY);
            if(mutablePair == null){ // 解析数据失败， 响应已处理
                log.error(logPrefix + ", mutablePair is null ");
                throw new BizException("解析数据异常！"); //需要实现类自行抛出ResponseException, 不应该在这抛此异常。
            }

            //解析到订单号
            payOrderId = mutablePair.left;
            log.info(logPrefix+", 解析数据为：payOrderId:"+payOrderId+", params:"+mutablePair.getRight());

            if(StringUtils.isNotEmpty(urlOrderId) && !urlOrderId.equals(payOrderId)){
                log.error(logPrefix+", 订单号不匹配. urlOrderId="+urlOrderId+", payOrderId="+ payOrderId);
                throw new BizException("订单号不匹配！");
            }

            //获取订单号 和 订单数据
            PayZfdd00 payZfdd00 = jdbcGateway.selectOne("pay.zfdd00.selectByXtddh0", payOrderId);

            // 订单不存在
            if(payZfdd00 == null){
                log.error(logPrefix+", 订单不存在. payOrderId="+payOrderId);
                return payNotifyService.doNotifyOrderNotExists(request);
            }

            //查询出商户应用的配置信息
            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(payZfdd00.getFwqdid());
            String autoHandle = mchAppConfigContext.getPayFwqd00().getSfzdcz();

            //调起接口的回调判断
            ChannelRetMsg notifyResult = payNotifyService.doNotice(request, mutablePair.getRight(), payZfdd00, mchAppConfigContext, IChannelNoticeService.NoticeTypeEnum.DO_NOTIFY);

            // 返回null 表明出现异常， 无需处理通知下游等操作。
            if(notifyResult == null || notifyResult.getChannelState() == null || notifyResult.getResponseEntity() == null){
                log.error(logPrefix+", 处理回调事件异常  notifyResult data error, notifyResult ="+ notifyResult);
                throw new BizException("处理回调事件异常！"); //需要实现类自行抛出ResponseException, 不应该在这抛此异常。
            }

            boolean updateOrderSuccess = true; //默认更新成功
            // 订单是 【支付中状态】
            if(payZfdd00.getDdzt00().equals(PayZfdd00.STATE_ING)) {
                //明确成功
                if(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS.equals(notifyResult.getChannelState())) {
                    updateOrderSuccess = payOrderService.updateIng2Success(payOrderId, notifyResult.getChannelOrderId(), notifyResult.getChannelUserId());
                    if ("1".equals(autoHandle)) { // 自动充值
                        notifyResult.setAutoRecharge(true);
                    } else if("2".equals(autoHandle)){ // 自动结算
                        notifyResult.setAutoSettle(true);
                    } else if("3".equals(autoHandle)){ // 自动充值+结算
                        notifyResult.setAutoRechargeAndSettle(true);
                    }
                    //明确失败
                }else if(ChannelRetMsg.ChannelState.CONFIRM_FAIL.equals(notifyResult.getChannelState())) {
                    updateOrderSuccess = payOrderService.updateIng2Fail(payOrderId, notifyResult.getChannelOrderId(), notifyResult.getChannelUserId(), notifyResult.getChannelErrCode(), notifyResult.getChannelErrMsg());
                }
            }

            // 更新订单 异常
            if(!updateOrderSuccess){
                log.error(logPrefix+", updateOrderSuccess = "+ updateOrderSuccess);
                return payNotifyService.doNotifyOrderStateUpdateFail(request);
            }

            //订单支付成功 其他业务逻辑
            if(notifyResult.getChannelState().equals(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS)){
                payOrderProcessService.confirmSuccess(payZfdd00, notifyResult.isAutoRecharge(), notifyResult.isAutoSettle(), notifyResult.isAutoRechargeAndSettle());
            }

            //订单支付成功 其他业务逻辑
            if(notifyResult.getChannelState().equals(ChannelRetMsg.ChannelState.CONFIRM_FAIL)){
                payOrderProcessService.confirmFail(payZfdd00);
            }

            log.info("===== "+logPrefix+", 订单通知完成。 payOrderId="+payOrderId+", parseState = "+notifyResult.getChannelState()+" =====");

            return notifyResult.getResponseEntity();

        } catch (BizException e) {
            log.error(logPrefix+", payOrderId="+payOrderId+", BizException"+ e);
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (ResponseException e) {
            log.error(logPrefix+", payOrderId="+payOrderId+", ResponseException"+ e);
            return e.getResponseEntity();

        } catch (Exception e) {
            log.error(logPrefix+", payOrderId="+payOrderId+", 系统异常"+ e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    /*  跳转到支付成功页面 **/
    private String toReturnPage(String errInfo){
        return "cashier/returnPage";
    }

}
