package com.ylz.yx.pay.payment.service.impl;

import com.baomidou.mybatisplus.extension.toolkit.SqlHelper;
import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.order.pay.model.UpdPayZfdd00Param;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * <p>
 * 支付订单表 服务实现类
 * </p>
 */
@Service
public class PayOrderService {

    @Autowired
    private JdbcGateway jdbcGateway;

    /** 更新订单状态  【订单生成】 --》 【支付中】 **/
    public boolean updateInit2Ing(String payOrderId, PayZfdd00 payZfdd00){

        UpdPayZfdd00Param updateRecord = new UpdPayZfdd00Param();
        updateRecord.setDdzt00(PayZfdd00.STATE_ING);

        //同时更新， 未确定 --》 已确定的其他信息。
        updateRecord.setZfqd00(payZfdd00.getZfqd00());
        updateRecord.setZffs00(payZfdd00.getZffs00());
        updateRecord.setQdyhbs(payZfdd00.getQdyhbs());
        updateRecord.setZfddh0(payZfdd00.getZfddh0());

        updateRecord.setXtddh0(payOrderId);
        updateRecord.setYddzt0(PayZfdd00.STATE_INIT);

        return SqlHelper.retBool(jdbcGateway.update("pay.zfdd00.updateByXtddh0", updateRecord));
    }

    /** 更新订单状态  【支付中】 --》 【支付成功】 **/
    public boolean updateIng2Success(String payOrderId, String channelOrderNo, String channelUserId){

        UpdPayZfdd00Param updateRecord = new UpdPayZfdd00Param();
        updateRecord.setDdzt00(PayZfdd00.STATE_SUCCESS);
        updateRecord.setZfddh0(channelOrderNo);
        updateRecord.setQdyhbs(channelUserId);
        updateRecord.setDdzfsj(new Date());

        updateRecord.setXtddh0(payOrderId);
        updateRecord.setYddzt0(PayZfdd00.STATE_ING);

        return SqlHelper.retBool(jdbcGateway.update("pay.zfdd00.updateByXtddh0", updateRecord));
    }

    /** 更新订单状态  【支付中】 --》 【订单关闭】 **/
    public boolean updateIng2Close(String payOrderId){

        UpdPayZfdd00Param updateRecord = new UpdPayZfdd00Param();
        updateRecord.setDdzt00(PayZfdd00.STATE_CLOSED);
        updateRecord.setDdzfsj(new Date());
        updateRecord.setZfcwms("渠道方发起关闭");

        updateRecord.setXtddh0(payOrderId);
        updateRecord.setYddzt0(PayZfdd00.STATE_ING);
        int count1 = jdbcGateway.update("pay.zfdd00.updateByXtddh0", updateRecord);

        UpdPayZfdd00Param updateRecord2 = new UpdPayZfdd00Param();
        updateRecord2.setDdzt00(PayZfdd00.STATE_CANCEL);
        updateRecord2.setDdzfsj(new Date());
        updateRecord2.setZfcwms("渠道方发起撤销");

        updateRecord2.setXtddh0(payOrderId);
        updateRecord2.setYddzt0(PayZfdd00.STATE_INIT);
        int count2 = jdbcGateway.update("pay.zfdd00.updateByXtddh0", updateRecord2);

        return count1 >= 1 || count2 >= 1;
    }


    /** 更新订单状态  【支付中】 --》 【支付失败】 **/
    public boolean updateIng2Fail(String payOrderId, String channelOrderNo, String channelUserId, String channelErrCode, String channelErrMsg){

        UpdPayZfdd00Param updateRecord = new UpdPayZfdd00Param();
        updateRecord.setDdzt00(PayZfdd00.STATE_FAIL);
        updateRecord.setZfcwm0(channelErrCode);
        updateRecord.setZfcwms(channelErrMsg);
        updateRecord.setZfddh0(channelOrderNo);
        updateRecord.setQdyhbs(channelUserId);

        updateRecord.setXtddh0(payOrderId);
        updateRecord.setYddzt0(PayZfdd00.STATE_ING);

        return SqlHelper.retBool(jdbcGateway.update("pay.zfdd00.updateByXtddh0", updateRecord));
    }


    /** 更新订单状态  【支付中】 --》 【支付成功/支付失败】 **/
    public boolean updateIng2SuccessOrFail(String payOrderId, String updateState, String channelOrderNo, String channelUserId, String channelErrCode, String channelErrMsg){

        if(updateState.equals(PayZfdd00.STATE_ING)){
            return true;
        }else if(updateState.equals(PayZfdd00.STATE_SUCCESS)){
            return updateIng2Success(payOrderId, channelOrderNo, channelUserId);
        }else if(updateState.equals(PayZfdd00.STATE_FAIL)){
            return updateIng2Fail(payOrderId, channelOrderNo, channelUserId, channelErrCode, channelErrMsg);
        }
        return false;
    }

    /** 查询商户订单 **/
    public PayZfdd00 queryMchOrder(String payOrderId, String mchOrderNo){
        //如果订单号不为空，根据订单号查询订单，否则根据商户号查询，都没有返回Null
        if(StringUtils.isNotEmpty(payOrderId)){
            return jdbcGateway.selectOne("pay.zfdd00.selectByXtddh0", payOrderId);
        }else if(StringUtils.isNotEmpty(mchOrderNo)){
            return jdbcGateway.selectOne("pay.zfdd00.selectByFwddh0", mchOrderNo);
        }else{
            return null;
        }
    }

    /** 更新订单为 超时状态 **/
    public Integer updateOrderExpired(){

        UpdPayZfdd00Param updateRecord = new UpdPayZfdd00Param();
        updateRecord.setDdzt00(PayZfdd00.STATE_CLOSED);
        updateRecord.setZfcwms("订单超时");
        return jdbcGateway.update("pay.zfdd00.updateZfdd00", updateRecord);
    }

    /** 更新订单 通知状态 --> 已发送 **/
    public int updateNotifySent(String payOrderId){
        UpdPayZfdd00Param payZfdd00 = new UpdPayZfdd00Param();
        payZfdd00.setTzzt00(CS.YES);
        payZfdd00.setXtddh0(payOrderId);
        return jdbcGateway.update("pay.zfdd00.updateZfdd00", payZfdd00);
    }
}
