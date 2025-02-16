package com.ylz.yx.pay.payment.channel.alipay;

import com.alipay.api.domain.AlipayTradeCloseModel;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.payment.channel.IPayOrderCloseService;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import com.ylz.yx.pay.payment.service.ConfigContextQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 支付宝 关闭订单接口实现类
 */
@Service
public class AlipayPayOrderCloseService implements IPayOrderCloseService {

    @Autowired private ConfigContextQueryService configContextQueryService;

    @Override
    public String getIfCode() {
        return CS.IF_CODE.ALIPAY;
    }

    @Override
    public ChannelRetMsg close(PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext){

        AlipayTradeCloseRequest req = new AlipayTradeCloseRequest();

        // 商户订单号，商户网站订单系统中唯一订单号，必填
        AlipayTradeCloseModel model = new AlipayTradeCloseModel();
        model.setOutTradeNo(payZfdd00.getXtddh0());
        req.setBizModel(model);

        //通用字段
        AlipayKit.putApiIsvInfo(mchAppConfigContext, req, model);

        AlipayTradeCloseResponse resp = configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).execute(req);

        // 返回状态成功
        if (resp.isSuccess()) {
            return ChannelRetMsg.confirmSuccess(resp.getTradeNo());
        }else {
            return ChannelRetMsg.sysError(resp.getSubMsg());
        }
    }


}
