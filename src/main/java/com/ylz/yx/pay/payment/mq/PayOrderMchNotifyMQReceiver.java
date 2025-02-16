package com.ylz.yx.pay.payment.mq;

import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.http.HttpUtil;
import com.ylz.core.logging.Logger;
import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.yx.pay.core.entity.PayTzjlb0;
import com.ylz.yx.pay.mq.model.PayOrderMchNotifyMQ;
import com.ylz.yx.pay.mq.vender.IMQSender;
import com.ylz.yx.pay.payment.service.impl.MchNotifyRecordService;
import com.ylz.yx.pay.payment.service.impl.PayOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 接收MQ消息
 * 业务： 支付订单商户通知
 */
@Component
public class PayOrderMchNotifyMQReceiver implements PayOrderMchNotifyMQ.IMQReceiver {

    private static final Logger log = new Logger(PayOrderMchNotifyMQReceiver.class.getName());

    @Autowired
    private PayOrderService payOrderService;
    @Autowired
    private MchNotifyRecordService mchNotifyRecordService;
    @Autowired
    private IMQSender mqSender;
    @Autowired
    private JdbcGateway jdbcGateway;

    @Override
    public void receive(PayOrderMchNotifyMQ.MsgPayload payload) {

        try {
            log.info("接收商户通知MQ, msg=" + payload.toString());

            String notifyId = payload.getNotifyId();
            PayTzjlb0 record = jdbcGateway.selectOne("pay.tzjlb0.selectByPrimaryKey", notifyId);
            if(record == null || record.getTzzt00() != PayTzjlb0.STATE_ING){
                log.info("查询通知记录不存在或状态不是通知中");
                mqSender.send(PayOrderMchNotifyMQ.build(notifyId), 5);
                return;
            }
            if( record.getTzcs00() >= record.getZdtzcs() ){
                log.info("已达到最大发送次数");
                return;
            }

            //1. (发送结果最多6次)
            Integer currentCount = record.getTzcs00() + 1;

            String notifyUrl = record.getYbtzdz();
            String res = "";
            try {
                res = HttpUtil.createPost(notifyUrl).timeout(600000).execute().body();
            } catch (Exception e) {
                log.error("http error", e);
                res = "连接["+ UrlBuilder.of(notifyUrl).getHost() +"]异常:【" + e.getMessage() + "】";
            }
            log.info("接收商户通知MQ, res=" + res);
            //支付订单 & 第一次通知: 更新为已通知
            if(currentCount == 1 && PayTzjlb0.TYPE_PAY_ORDER == record.getDdlx00()){
                payOrderService.updateNotifySent(record.getXtddh0());
            }

            //通知成功
            if("SUCCESS".equalsIgnoreCase(res)){
                mchNotifyRecordService.updateNotifyResult(notifyId, PayTzjlb0.STATE_SUCCESS, res);
                return;
            }

            //通知次数 >= 最大通知次数时， 更新响应结果为异常， 不在继续延迟发送消息
            if( currentCount >= record.getZdtzcs() ){
                mchNotifyRecordService.updateNotifyResult(notifyId, PayTzjlb0.STATE_FAIL, res);
                return;
            }

            // 继续发送MQ 延迟发送
            mchNotifyRecordService.updateNotifyResult(notifyId, PayTzjlb0.STATE_ING, res);
            // 通知延时次数
            //        1   2  3  4   5   6
            //        0  30 60 90 120 150
            mqSender.send(PayOrderMchNotifyMQ.build(notifyId), currentCount * 30);

            return;
        }catch (Exception e) {
            log.error(e.getMessage(), e);
            return;
        }
    }
}
