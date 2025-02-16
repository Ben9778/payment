package com.ylz.yx.pay.payment.service.impl;

import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.yx.pay.core.entity.PayTkdd00;
import com.ylz.yx.pay.core.exception.BizException;
import com.ylz.yx.pay.order.refund.model.UdpPayTkdd00Param;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * <p>
 * 退款订单表 服务实现类
 * </p>
 */
@Service
public class RefundOrderService {

    @Autowired
    private JdbcGateway jdbcGateway;

    /** 查询商户订单 **/
    public PayTkdd00 queryMchOrder(String mchRefundNo, String refundOrderId){

        if(StringUtils.isNotEmpty(refundOrderId)){
            return jdbcGateway.selectOne("pay.tkdd00.selectByXtddh0", refundOrderId);
        }else if(StringUtils.isNotEmpty(mchRefundNo)){
            return jdbcGateway.selectOne("pay.tkdd00.selectByFwddh0", mchRefundNo);
        }else{
            return null;
        }
    }


    /** 更新退款单状态  【退款单生成】 --》 【退款中】 **/
    public boolean updateInit2Ing(String refundOrderId, String channelOrderNo){

        UdpPayTkdd00Param updateRecord = new UdpPayTkdd00Param();
        updateRecord.setTkzt00(PayTkdd00.STATE_ING);
        updateRecord.setZfddh0(channelOrderNo);
        updateRecord.setXtddh0(refundOrderId);
        updateRecord.setYtkzt0(PayTkdd00.STATE_INIT);
        return SqlHelper.retBool(jdbcGateway.update("pay.tkdd00.updateByXtddh0", updateRecord));
    }

    /** 更新退款单状态  【退款中】 --》 【退款成功】 **/
    @Transactional
    public boolean updateIng2Success(String refundOrderId, String channelOrderNo){

        PayTkdd00 payTkdd00 = jdbcGateway.selectOne("pay.tkdd00.selectByXtddh0", refundOrderId);

        UdpPayTkdd00Param updateRecord = new UdpPayTkdd00Param();
        updateRecord.setTkzt00(PayTkdd00.STATE_SUCCESS);
        updateRecord.setZfddh0(channelOrderNo);
        updateRecord.setTkcgsj(new Date());
        updateRecord.setXtddh0(refundOrderId);
        updateRecord.setYtkzt0(PayTkdd00.STATE_ING);

        //1. 更新退款订单表数据
        int count = jdbcGateway.update("pay.tkdd00.updateByXtddh0", updateRecord);
        if(count <=0){
            return false;
        }

        //2. 更新订单表数据（更新退款次数,退款状态,如全额退款更新支付状态为已退款）
        int updateCount = jdbcGateway.update("pay.zfdd00.updateRefundAmountAndCount", payTkdd00);
        if(updateCount <= 0){
            throw new BizException("更新订单数据异常");
        }
        return true;
    }


    /** 更新退款单状态  【退款中】 --》 【退款失败】 **/
    @Transactional
    public boolean updateIng2Fail(String refundOrderId, String channelOrderNo, String channelErrCode, String channelErrMsg){

        UdpPayTkdd00Param updateRecord = new UdpPayTkdd00Param();
        updateRecord.setTkzt00(PayTkdd00.STATE_FAIL);
        updateRecord.setZfcwm0(channelErrCode);
        updateRecord.setZfcwms(channelErrMsg);
        updateRecord.setZfddh0(channelOrderNo);

        updateRecord.setXtddh0(refundOrderId);
        updateRecord.setYtkzt0(PayTkdd00.STATE_ING);

        return SqlHelper.retBool(jdbcGateway.update("pay.tkdd00.updateByXtddh0", updateRecord));
    }


    /** 更新退款单状态  【退款中】 --》 【退款成功/退款失败】 **/
    @Transactional
    public boolean updateIng2SuccessOrFail(String refundOrderId, String updateState, String channelOrderNo, String channelErrCode, String channelErrMsg){

        if(updateState.equals(PayTkdd00.STATE_ING)){
            return true;
        }else if(updateState.equals(PayTkdd00.STATE_SUCCESS)){
            return updateIng2Success(refundOrderId, channelOrderNo);
        }else if(updateState.equals(PayTkdd00.STATE_FAIL)){
            return updateIng2Fail(refundOrderId, channelOrderNo, channelErrCode, channelErrMsg);
        }
        return false;
    }


    /** 更新退款单为 关闭状态 **/
    public Integer updateOrderExpired(){

        UdpPayTkdd00Param payTkdd00 = new UdpPayTkdd00Param();
        payTkdd00.setTkzt00(PayTkdd00.STATE_CLOSED);

        return 0;
        /*baseMapper.update(refundOrder,
                RefundOrder.gw()
                        .in(RefundOrder::getState, Arrays.asList(RefundOrder.STATE_INIT, RefundOrder.STATE_ING))
                        .le(RefundOrder::getExpiredTime, new Date())
        );*/
    }

    public PayTkdd00 getById(String refundOrderId) {
        return jdbcGateway.selectOne("pay.tkdd00.selectByXtddh0", refundOrderId);
    }
}
