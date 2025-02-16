package com.ylz.yx.pay.payment.task;

import com.ylz.core.logging.Logger;
import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.yx.pay.core.entity.PayTkdd00;
import com.ylz.yx.pay.payment.service.ChannelOrderReissueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/*
* 补单定时任务(退款单)
*/
@Component
@EnableScheduling
@ConditionalOnProperty(value = "enable.scheduling.refundReissue", havingValue = "true")
public class RefundOrderReissueTask {

    private static final Logger log = new Logger(RefundOrderReissueTask.class.getName());

    private static final int QUERY_PAGE_SIZE = 100; //每次查询数量

    @Autowired private ChannelOrderReissueService channelOrderReissueService;
    @Autowired private JdbcGateway jdbcGateway;

    @Scheduled(cron="0 0/1 * * * ?") // 每分钟执行一次
    public void start() {

        while(true){
            try {
                List<PayTkdd00> payTkdd00List = jdbcGateway.selectList("pay.tkdd00.selectIngList");

                if(payTkdd00List == null || payTkdd00List.isEmpty()){ //本次查询无结果, 不再继续查询;
                    break;
                }

                for(PayTkdd00 payTkdd00: payTkdd00List){
                    channelOrderReissueService.processRefundOrder(payTkdd00);
                }
                break;
            } catch (Exception e) { //出现异常，直接退出，避免死循环。
                log.error("error", e);
                break;
            }

        }
    }



}
