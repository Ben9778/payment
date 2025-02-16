package com.ylz.yx.pay.payment.channel.ysfpay;

import com.alibaba.fastjson.JSONObject;
import com.ylz.core.logging.Logger;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.payment.channel.IPayOrderCloseService;
import com.ylz.yx.pay.payment.channel.ysfpay.utils.YsfHttpUtil;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 云闪付 关闭订单接口实现类
 */
@Service
public class YsfpayPayOrderCloseService implements IPayOrderCloseService {

    private static final Logger log = new Logger(YsfpayPayOrderCloseService.class.getName());

    @Override
    public String getIfCode() {
        return CS.IF_CODE.YSFPAY;
    }

    @Autowired
    private YsfpayPaymentService ysfpayPaymentService;

    @Override
    public ChannelRetMsg close(PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext) throws Exception {
        JSONObject reqParams = new JSONObject();
        String orderType = YsfHttpUtil.getOrderTypeByCommon(payZfdd00.getZffs00());
        String logPrefix = "【云闪付("+orderType+")关闭订单】";

        try {
            reqParams.put("orderNo", payZfdd00.getXtddh0()); //订单号
            reqParams.put("orderType", orderType); //订单类型

            //封装公共参数 & 签名 & 调起http请求 & 返回响应数据并包装为json格式。
            JSONObject resJSON = ysfpayPaymentService.packageParamAndReq("/gateway/api/pay/closeOrder", reqParams, logPrefix, mchAppConfigContext);
            log.info("关闭订单 payorderId:"+payZfdd00.getXtddh0()+", 返回结果:" + resJSON);
            if(resJSON == null){
                return ChannelRetMsg.sysError("【云闪付】请求关闭订单异常");
            }

            //请求 & 响应成功， 判断业务逻辑
            String respCode = resJSON.getString("respCode"); //应答码
            String respMsg = resJSON.getString("respMsg"); //应答信息
            if(("00").equals(respCode)){// 请求成功
                return ChannelRetMsg.confirmSuccess(null);  //关单成功
            }
            return ChannelRetMsg.sysError(respMsg); // 关单失败
        }catch (Exception e) {
            return ChannelRetMsg.sysError(e.getMessage()); // 关单失败
        }
    }

}
