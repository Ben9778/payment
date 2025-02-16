package com.ylz.yx.pay.mq.vender.activemq;

import com.ylz.yx.pay.mq.model.AbstractMQ;
import com.ylz.yx.pay.mq.vender.IMQSender;
import org.apache.activemq.ScheduledMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.ObjectMessage;
import javax.jms.TextMessage;

/**
*  activeMQ 消息发送器的实现
*/
@Component
public class ActiveMQSender implements IMQSender {

    @Autowired
    private ActiveMQConfig activeMQConfig;

    @Autowired
    private JmsTemplate jmsTemplate;

    //@Autowired
    //private JmsMessagingTemplate jmsMessagingTemplate;

    @Override
    public void send(AbstractMQ mqModel) {
        jmsTemplate.convertAndSend(activeMQConfig.getDestination(mqModel), mqModel.toMessage());
    }

    @Override
    public void send(AbstractMQ mqModel, int delay) {
        //jmsTemplate.convertAndSend(activeMQConfig.getDestination(mqModel), mqModel.toMessage());
        jmsTemplate.send(activeMQConfig.getDestination(mqModel), session -> {
            TextMessage tm = session.createTextMessage(mqModel.toMessage());
            tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, delay * 1000);
            tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_PERIOD, 1*1000);
            tm.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_REPEAT, 1);
            return tm;
            /*ObjectMessage objectMessage = session.createObjectMessage(mqModel.toMessage());
            objectMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,delay*1000);
            return objectMessage;*/
        });


    }

}
