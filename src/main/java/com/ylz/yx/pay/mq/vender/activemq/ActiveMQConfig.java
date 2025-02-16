package com.ylz.yx.pay.mq.vender.activemq;

import com.ylz.yx.pay.mq.constant.MQSendTypeEnum;
import com.ylz.yx.pay.mq.model.AbstractMQ;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.apache.activemq.command.ActiveMQTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.config.JmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
* activeMQ的配置项
*/
@Component
public class ActiveMQConfig {

    @Bean
    ConnectionFactory connectionFactory() {
        return new ActiveMQConnectionFactory();
    }

    @Bean
    JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        JmsTemplate jmsTemplate = new JmsTemplate(connectionFactory);
        jmsTemplate.setPriority(999);
        return jmsTemplate;
    }

    Map<String, Destination> map = new ConcurrentHashMap<>();

    public Destination getDestination(AbstractMQ mqModel){

        if(map.get(mqModel.getMQName()) == null){
            this.init(mqModel.getMQName(), mqModel.getMQType());
        }
        return map.get(mqModel.getMQName());
    }

    private synchronized void init(String mqName, MQSendTypeEnum mqSendTypeEnum){

        if(mqSendTypeEnum == MQSendTypeEnum.QUEUE){
            map.put(mqName, new ActiveMQQueue(mqName) );
        }else{
            map.put(mqName, new ActiveMQTopic(mqName) );
        }
    }

    @Bean("jmsListenerContainerFactory")
    public JmsListenerContainerFactory queueContainer(ConnectionFactory mqQueueConnectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(mqQueueConnectionFactory);
        return factory;
    }

    public static final String TOPIC_LISTENER_CONTAINER = "jmsTopicListenerContainer";

    /** 新增jmsListenerContainer, 用于接收topic类型的消息**/
    @Bean
    public JmsListenerContainerFactory<?> jmsTopicListenerContainer(ConnectionFactory factory){
        DefaultJmsListenerContainerFactory bean = new DefaultJmsListenerContainerFactory();
        bean.setPubSubDomain(true);
        bean.setConnectionFactory(factory);
        return bean;
    }

}
