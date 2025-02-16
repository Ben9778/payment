package com.ylz.yx.pay.payment.service;

import com.ylz.core.logging.Logger;
import com.ylz.yx.pay.payment.channel.IPayOrderQueryService;
import com.ylz.yx.pay.payment.channel.IRefundService;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.core.entity.PayTkdd00;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import com.ylz.yx.pay.payment.service.impl.PayOrderService;
import com.ylz.yx.pay.utils.SpringBeansUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
 * 查询上游订单， &  补单服务实现类
 */

@Service
public class ChannelOrderReissueService {

    private static final Logger log = new Logger(ChannelOrderReissueService.class.getName());

    @Autowired
    private ConfigContextQueryService configContextQueryService;
    @Autowired
    private PayOrderService payOrderService;
    @Autowired
    private PayOrderProcessService payOrderProcessService;
    @Autowired
    private RefundOrderProcessService refundOrderProcessService;


    /**
     * 处理订单
     **/
    public ChannelRetMsg processPayOrder(PayZfdd00 payZfdd00) {

        try {

            String payOrderId = payZfdd00.getXtddh0();

            //查询支付接口是否存在
            IPayOrderQueryService queryService = SpringBeansUtil.getBean(payZfdd00.getZfqd00() + "PayOrderQueryService", IPayOrderQueryService.class);

            // 支付通道接口实现不存在
            if (queryService == null) {
                log.error(payZfdd00.getZfqd00() + " interface not exists!");
                return null;
            }

            //查询出商户应用的配置信息
            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(payZfdd00.getFwqdid());
            String autoHandle = mchAppConfigContext.getPayFwqd00().getSfzdcz();

            ChannelRetMsg channelRetMsg = queryService.query(payZfdd00, mchAppConfigContext);
            if (channelRetMsg == null) {
                log.error("channelRetMsg is null");
                return null;
            }

            log.info("补单[" + payOrderId + "]查询结果为：" + channelRetMsg);

            if ("1".equals(autoHandle)) { // 自动充值
                channelRetMsg.setAutoRecharge(true);
            } else if("2".equals(autoHandle)){ // 自动结算
                channelRetMsg.setAutoSettle(true);
            } else if("3".equals(autoHandle)){ // 自动充值+结算
                channelRetMsg.setAutoRechargeAndSettle(true);
            }

            // 查询成功
            if (channelRetMsg.getChannelState() == ChannelRetMsg.ChannelState.CONFIRM_SUCCESS) {
                if (payOrderService.updateIng2Success(payOrderId, channelRetMsg.getChannelOrderId(), channelRetMsg.getChannelUserId())) {
                    //订单支付成功，其他业务逻辑
                    payOrderProcessService.confirmSuccess(payZfdd00, channelRetMsg.isAutoRecharge(), channelRetMsg.isAutoSettle(), channelRetMsg.isAutoRechargeAndSettle());
                }
            } else if (channelRetMsg.getChannelState() == ChannelRetMsg.ChannelState.CONFIRM_FAIL) {  //确认失败
                //1. 更新支付订单表为失败状态
                payOrderService.updateIng2Fail(payOrderId, channelRetMsg.getChannelOrderId(), channelRetMsg.getChannelUserId(), channelRetMsg.getChannelErrCode(), channelRetMsg.getChannelErrMsg());
            }

            return channelRetMsg;

        } catch (Exception e) {  //继续下一次迭代查询
            log.error("error payOrderId = " + payZfdd00.getXtddh0(), e);
            return null;
        }

    }

    /**
     * 处理退款订单
     **/
    public ChannelRetMsg processRefundOrder(PayTkdd00 payTkdd00) {

        try {

            String refundOrderId = payTkdd00.getXtddh0();

            //查询支付接口是否存在
            IRefundService queryService = SpringBeansUtil.getBean(payTkdd00.getZfqd00() + "RefundService", IRefundService.class);

            // 支付通道接口实现不存在
            if (queryService == null) {
                log.error("退款补单：" + payTkdd00.getZfqd00() + " interface not exists!");
                return null;
            }

            //查询出商户应用的配置信息
            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(payTkdd00.getFwqdid());

            ChannelRetMsg channelRetMsg = queryService.query(payTkdd00, mchAppConfigContext);
            if (channelRetMsg == null) {
                log.error("退款补单：channelRetMsg is null");
                return null;
            }

            log.info("退款补单：[" + refundOrderId + "]查询结果为：" + channelRetMsg);
            // 根据渠道返回结果，处理退款订单
            refundOrderProcessService.handleRefundOrder4Channel(channelRetMsg, payTkdd00);

            return channelRetMsg;

        } catch (Exception e) {  //继续下一次迭代查询
            log.error("退款补单：error refundOrderId = " + payTkdd00.getXtddh0(), e);
            return null;
        }

    }


}
