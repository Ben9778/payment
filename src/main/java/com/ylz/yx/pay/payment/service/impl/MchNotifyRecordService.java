package com.ylz.yx.pay.payment.service.impl;

import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.yx.pay.core.entity.PayTzjlb0;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 商户通知表 服务实现类
 * </p>
 */
@Service
public class MchNotifyRecordService {

    @Autowired
    private JdbcGateway jdbcGateway;

    /** 根据订单号和类型查询 */
    public PayTzjlb0 findByOrderAndType(String orderId, Integer orderType){
        PayTzjlb0 payTzjlb0 = new PayTzjlb0();
        payTzjlb0.setXtddh0(orderId);
        payTzjlb0.setDdlx00(orderType);
        return jdbcGateway.selectOne("pay.tzjlb0.selectByOrderAndType", payTzjlb0);
    }

    /** 查询支付订单 */
    public PayTzjlb0 findByPayOrder(String orderId){
        return findByOrderAndType(orderId, PayTzjlb0.TYPE_PAY_ORDER);
    }

    /** 查询退款订单订单 */
    public PayTzjlb0 findByRefundOrder(String orderId){
        return findByOrderAndType(orderId, PayTzjlb0.TYPE_REFUND_ORDER);
    }

    public Integer updateNotifyResult(String notifyId, Integer state, String resResult){
        PayTzjlb0 payTzjlb0 = new PayTzjlb0();
        payTzjlb0.setId0000(notifyId);
        payTzjlb0.setTzzt00(state);
        payTzjlb0.setTzyxjg(resResult);
        return jdbcGateway.update("pay.tzjlb0.updateNotifyResult", payTzjlb0);
    }
}
