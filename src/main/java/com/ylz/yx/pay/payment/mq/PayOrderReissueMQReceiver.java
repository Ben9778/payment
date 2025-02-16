package com.ylz.yx.pay.payment.mq;

import com.jeequan.jeepay.JeepayClient;
import com.jeequan.jeepay.exception.JeepayException;
import com.jeequan.jeepay.model.PayOrderCloseReqModel;
import com.jeequan.jeepay.request.PayOrderCloseRequest;
import com.jeequan.jeepay.response.PayOrderCloseResponse;
import com.ylz.core.logging.Logger;
import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.yx.pay.config.ApplicationProperty;
import com.ylz.yx.pay.core.entity.PayFwqd00;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.core.exception.CustomException;
import com.ylz.yx.pay.core.exception.HttpStatus;
import com.ylz.yx.pay.mq.model.PayOrderReissueMQ;
import com.ylz.yx.pay.mq.vender.IMQSender;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import com.ylz.yx.pay.payment.service.ChannelOrderReissueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 接收MQ消息
 * 业务： 支付订单补单（一般用于没有回调的接口，比如微信的条码支付）
 */
@Component
public class PayOrderReissueMQReceiver implements PayOrderReissueMQ.IMQReceiver {

    private static final Logger log = new Logger(PayOrderReissueMQReceiver.class.getName());

    @Autowired
    private IMQSender mqSender;
    @Autowired
    private ChannelOrderReissueService channelOrderReissueService;
    @Autowired
    private JdbcGateway jdbcGateway;
    @Resource
    ApplicationProperty applicationProperty;


    @Override
    public void receive(PayOrderReissueMQ.MsgPayload payload) {
        try {
            String payOrderId = payload.getPayOrderId();
            int currentCount = payload.getCount();
            log.info("接收轮询查单通知MQ, payOrderId="+payOrderId+", count=" + currentCount);
            currentCount++ ;

            PayZfdd00 payOrder = jdbcGateway.selectOne("pay.zfdd00.selectByXtddh0", payOrderId);
            if(payOrder == null) {
                log.warn("查询支付订单为空,payOrderId=" + payOrderId);
                return;
            }

            if(!PayZfdd00.STATE_ING.equals(payOrder.getDdzt00())){
                log.warn("订单状态不是支付中,不需查询渠道, payOrderId=" + payOrderId + ", ddzt00=" + payOrder.getDdzt00());
                //mqSender.send(PayOrderReissueMQ.build(payOrderId, currentCount), 5); //延迟5s再次查询
                return;
            }

            ChannelRetMsg channelRetMsg = channelOrderReissueService.processPayOrder(payOrder);

            //返回null 可能为接口报错等， 需要再次轮询
            if(channelRetMsg == null || channelRetMsg.getChannelState() == null || channelRetMsg.getChannelState().equals(ChannelRetMsg.ChannelState.WAITING)){
                //最多查询6次
                if(currentCount <= 6){
                    mqSender.send(PayOrderReissueMQ.build(payOrderId, currentCount), 5); //延迟5s再次查询
                }else{
                    //TODO 调用【撤销订单】接口
                    String xtddh0 = payOrderId;

                    PayZfdd00 payZfdd00 = jdbcGateway.selectOne("pay.zfdd00.selectByXtddh0", xtddh0);
                    if (payZfdd00 == null) {
                        return;
                    }

                    if(!PayZfdd00.STATE_ING.equals(payZfdd00.getDdzt00())){
                        return;
                    }


                    PayOrderCloseRequest request = new PayOrderCloseRequest();
                    PayOrderCloseReqModel model = new PayOrderCloseReqModel();
                    request.setBizModel(model);

                    model.setAppId(payZfdd00.getFwqdid());
                    model.setPayOrderId(payZfdd00.getXtddh0());

                    PayFwqd00 mchApp = jdbcGateway.selectOne("pay.fwqd00.selectByPrimaryKey", payZfdd00.getFwqdid());

                    JeepayClient jeepayClient = new JeepayClient(applicationProperty.getPaySiteBackUrl(), mchApp.getQdmy00());

                    try {
                        PayOrderCloseResponse response = jeepayClient.execute(request);
                        if(response.getCode() != 0){
                            return;
                        }
                    } catch (JeepayException e) {
                        log.error(e.getMessage());
                    }
                }
            }else{ //其他状态， 不需要再次轮询。

            }
        }catch (Exception e) {
            log.error(e.getMessage());
            return;
        }
    }
}
