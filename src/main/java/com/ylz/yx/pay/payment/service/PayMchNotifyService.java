package com.ylz.yx.pay.payment.service;

import com.alibaba.fastjson.JSONObject;
import com.ylz.core.logging.Logger;
import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.yx.pay.core.entity.PayTkdd00;
import com.ylz.yx.pay.core.entity.PayTzjlb0;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.mq.model.PayOrderMchNotifyMQ;
import com.ylz.yx.pay.mq.vender.IMQSender;
import com.ylz.yx.pay.payment.rqrs.payorder.QueryPayOrderRS;
import com.ylz.yx.pay.payment.rqrs.refund.QueryRefundOrderRS;
import com.ylz.yx.pay.payment.service.impl.MchNotifyRecordService;
import com.ylz.yx.pay.utils.PayKit;
import com.ylz.yx.pay.utils.StringKit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
 * 商户通知 service
 */
@Service
public class PayMchNotifyService {

    private static final Logger log = new Logger(PayMchNotifyService.class.getName());

    @Autowired
    private MchNotifyRecordService mchNotifyRecordService;
    @Autowired
    private ConfigContextQueryService configContextQueryService;
    @Autowired
    private IMQSender mqSender;
    @Autowired
    private JdbcGateway jdbcGateway;

    /**
     * 商户通知信息， 只有订单是终态，才会发送通知， 如明确成功和明确失败
     **/
    public void payOrderNotify(PayZfdd00 dbPayZfdd00) {

        try {
            // 通知地址为空
            if (StringUtils.isEmpty(dbPayZfdd00.getYbtzdz())) {
                return;
            }

            //获取到通知对象
            PayTzjlb0 payTzjlb0 = mchNotifyRecordService.findByPayOrder(dbPayZfdd00.getXtddh0());

            if (payTzjlb0 != null) {
                log.info("当前已存在通知消息， 不再发送。");
                return;
            }

            //商户app私钥
            String appSecret = configContextQueryService.queryMchApp(dbPayZfdd00.getFwqdid()).getQdmy00();

            // 封装通知url
            String notifyUrl = createNotifyUrl(dbPayZfdd00, appSecret);
            payTzjlb0 = new PayTzjlb0();
            payTzjlb0.setId0000(StringKit.getUUID());
            payTzjlb0.setXtddh0(dbPayZfdd00.getXtddh0());
            payTzjlb0.setDdlx00(PayTzjlb0.TYPE_PAY_ORDER);
            payTzjlb0.setFwddh0(dbPayZfdd00.getFwddh0()); //商户订单号
            payTzjlb0.setFwqdid(dbPayZfdd00.getFwqdid());
            payTzjlb0.setYbtzdz(notifyUrl);
            payTzjlb0.setTzyxjg("");
            payTzjlb0.setTzcs00(0);
            payTzjlb0.setTzzt00(PayTzjlb0.STATE_ING); // 通知中

            try {
                jdbcGateway.insert("pay.tzjlb0.insertSelective", payTzjlb0);
            } catch (Exception e) {
                log.info("数据库已存在[" + payTzjlb0.getXtddh0() + "]消息，本次不再推送。"+ e);
                return;
            }

            //推送到MQ
            String notifyId = payTzjlb0.getId0000();
            mqSender.send(PayOrderMchNotifyMQ.build(notifyId));

        } catch (Exception e) {
            log.error("推送失败！", e);
        }
    }

    /**
     * 商户通知信息，退款成功的发送通知
     **/
    public void refundOrderNotify(PayTkdd00 dbRefundOrder) {

        try {
            // 通知地址为空
            if (StringUtils.isEmpty(dbRefundOrder.getYbtzdz())) {
                return;
            }

            //获取到通知对象
            PayTzjlb0 payTzjlb0 = mchNotifyRecordService.findByRefundOrder(dbRefundOrder.getXtddh0());

            if (payTzjlb0 != null) {
                log.info("当前已存在通知消息， 不再发送。");
                return;
            }

            //商户app私钥
            String appSecret = configContextQueryService.queryMchApp(dbRefundOrder.getFwqdid()).getQdmy00();

            // 封装通知url
            String notifyUrl = createNotifyUrl(dbRefundOrder, appSecret);
            payTzjlb0 = new PayTzjlb0();
            payTzjlb0.setId0000(StringKit.getUUID());
            payTzjlb0.setXtddh0(dbRefundOrder.getXtddh0());
            payTzjlb0.setDdlx00(PayTzjlb0.TYPE_REFUND_ORDER);
            payTzjlb0.setFwddh0(dbRefundOrder.getFwddh0()); //商户订单号
            payTzjlb0.setFwqdid(dbRefundOrder.getFwqdid());
            payTzjlb0.setYbtzdz(notifyUrl);
            payTzjlb0.setTzyxjg("");
            payTzjlb0.setTzcs00(0);
            payTzjlb0.setTzzt00(PayTzjlb0.STATE_ING); // 通知中

            try {
                jdbcGateway.insert("pay.tzjlb0.insertSelective", payTzjlb0);
            } catch (Exception e) {
                log.info("数据库已存在[" + payTzjlb0.getXtddh0() + "]消息，本次不再推送。");
                return;
            }

            //推送到MQ
            String notifyId = payTzjlb0.getId0000();
            mqSender.send(PayOrderMchNotifyMQ.build(notifyId));

        } catch (Exception e) {
            log.error("推送失败！", e);
        }
    }


    /**
     * 创建响应URL
     */
    public String createNotifyUrl(PayZfdd00 payOrder, String appSecret) {

        QueryPayOrderRS queryPayOrderRS = QueryPayOrderRS.buildByPayOrder(payOrder);
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(queryPayOrderRS);
        jsonObject.put("reqTime", System.currentTimeMillis()); //添加请求时间

        // 报文签名
        jsonObject.put("sign", PayKit.getSign(jsonObject, appSecret));

        // 生成通知
        return StringKit.appendUrlQuery(payOrder.getYbtzdz(), jsonObject);
    }


    /**
     * 创建响应URL
     */
    public String createNotifyUrl(PayTkdd00 refundOrder, String appSecret) {

        QueryRefundOrderRS queryRefundOrderRS = QueryRefundOrderRS.buildByRefundOrder(refundOrder);
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(queryRefundOrderRS);
        jsonObject.put("reqTime", System.currentTimeMillis()); //添加请求时间

        // 报文签名
        jsonObject.put("sign", PayKit.getSign(jsonObject, appSecret));

        // 生成通知
        return StringKit.appendUrlQuery(refundOrder.getYbtzdz(), jsonObject);
    }


    /**
     * 创建响应URL
     */
    public String createReturnUrl(PayZfdd00 payOrder, String appSecret) {

        if (StringUtils.isEmpty(payOrder.getYmtzdz())) {
            return "";
        }

        QueryPayOrderRS queryPayOrderRS = QueryPayOrderRS.buildByPayOrder(payOrder);
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(queryPayOrderRS);
        jsonObject.put("reqTime", System.currentTimeMillis()); //添加请求时间

        // 报文签名
        jsonObject.put("sign", PayKit.getSign(jsonObject, appSecret));   // 签名

        // 生成跳转地址
        return StringKit.appendUrlQuery(payOrder.getYmtzdz(), jsonObject);

    }

}
