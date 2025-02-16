package com.ylz.yx.pay.payment.task;

import com.ylz.core.logging.Logger;
import com.ylz.yx.pay.payment.service.impl.RefundOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

/*
* 退款订单过期定时任务
*/
@Component
//@EnableScheduling
public class RefundOrderExpiredTask {

    private static final Logger log = new Logger(RefundOrderExpiredTask.class.getName());

    @Autowired private RefundOrderService refundOrderService;

    //@Scheduled(cron="0 0/1 * * * ?") // 每分钟执行一次
    public void start() {

        int updateCount = refundOrderService.updateOrderExpired();
        log.info("处理退款订单超时" + updateCount + "条.");
    }


}
