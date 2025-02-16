package com.ylz.yx.pay.payment.task;

import com.ylz.core.logging.Logger;
import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.payment.service.ChannelOrderReissueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/*
* 补单定时任务
*/
@Component
@EnableScheduling
@ConditionalOnProperty(value = "enable.scheduling.payReissue", havingValue = "true")
public class PayOrderReissueTask {

    private static final Logger log = new Logger(PayOrderReissueTask.class.getName());

    @Autowired private ChannelOrderReissueService channelOrderReissueService;
    @Autowired private JdbcGateway jdbcGateway;

    @Scheduled(cron="0/2 * * * * ?") // 每2秒钟执行一次
    public void start() {

        while(true){

            try {
                List<PayZfdd00> payZfdd00List = jdbcGateway.selectList("pay.zfdd00.selectIngList");

                if(payZfdd00List == null || payZfdd00List.isEmpty()){ //本次查询无结果, 不再继续查询;
                    break;
                }

                for(PayZfdd00 payZfdd00: payZfdd00List){
                    channelOrderReissueService.processPayOrder(payZfdd00);
                }
                break;
            } catch (Exception e) { //出现异常，直接退出，避免死循环。
                log.error("error", e);
                break;
            }

        }
    }



}
