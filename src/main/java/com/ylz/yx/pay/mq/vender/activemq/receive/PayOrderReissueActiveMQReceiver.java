package com.ylz.yx.pay.mq.vender.activemq.receive;

import com.ylz.yx.pay.mq.executor.MqThreadExecutor;
import com.ylz.yx.pay.mq.model.PayOrderReissueMQ;
import com.ylz.yx.pay.mq.vender.IMQMsgReceiver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * activeMQ 消息接收器：仅在vender=activeMQ时 && 项目实现IMQReceiver接口时 进行实例化
 * 业务：  支付订单补单（一般用于没有回调的接口，比如微信的条码支付）
 */
@Component
@EnableJms
public class PayOrderReissueActiveMQReceiver implements IMQMsgReceiver {

    @Autowired
    private PayOrderReissueMQ.IMQReceiver mqReceiver;

    /** 接收 【 queue 】 类型的消息 **/
    @Override
    @Async(MqThreadExecutor.EXECUTOR_PAYORDER_MCH_NOTIFY)
    @JmsListener(destination = PayOrderReissueMQ.MQ_NAME)
    public void receiveMsg(String msg){
        System.out.println("ActiveMQQueue成功接受Name" + msg);
        mqReceiver.receive(PayOrderReissueMQ.parse(msg));
    }

}
