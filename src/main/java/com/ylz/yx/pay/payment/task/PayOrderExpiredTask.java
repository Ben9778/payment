package com.ylz.yx.pay.payment.task;

import com.ylz.core.logging.Logger;
import com.ylz.yx.pay.payment.service.impl.PayOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/*
 * 订单过期定时任务
 */
@Component
@EnableScheduling
@ConditionalOnProperty(value = "enable.scheduling.payExpired", havingValue = "true")
public class PayOrderExpiredTask {

    private static final Logger log = new Logger(PayOrderExpiredTask.class.getName());

    @Autowired
    private PayOrderService payOrderService;

    @Scheduled(cron = "0 0/1 * * * ?") // 每分钟执行一次
    public void start() {

        int updateCount = payOrderService.updateOrderExpired();
        log.info("处理订单超时" + updateCount + "条.");
    }


}
