package com.ylz.yx.pay.payment.channel.wxpay;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.service.WxPayService;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.exception.BizException;
import com.ylz.yx.pay.core.model.params.wxpay.WxpayMchParams;
import com.ylz.yx.pay.payment.channel.AbstractPaymentService;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.model.WxServiceWrapper;
import com.ylz.yx.pay.payment.rqrs.AbstractRS;
import com.ylz.yx.pay.payment.rqrs.payorder.UnifiedOrderRQ;
import com.ylz.yx.pay.payment.util.PaywayUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/*
* 支付接口： 微信官方
* 支付方式： 自适应
*/
@Service
public class WxpayPaymentService extends AbstractPaymentService {

    @Override
    public String getIfCode() {
        return CS.IF_CODE.WXPAY;
    }

    @Override
    public boolean isSupport(String wayCode) {
        return true;
    }

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayZfdd00 payZfdd00) {
        return PaywayUtil.getRealPaywayService(this, payZfdd00.getZffs00()).preCheck(rq, payZfdd00);
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext) throws Exception {

        // 微信API版本

        WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);

        String apiVersion = wxServiceWrapper.getApiVersion();
        if (CS.PAY_IF_VERSION.WX_V2.equals(apiVersion)) {
            return PaywayUtil.getRealPaywayService(this, payZfdd00.getZffs00()).pay(rq, payZfdd00, mchAppConfigContext);
        } else if (CS.PAY_IF_VERSION.WX_V3.equals(apiVersion)) {
            return PaywayUtil.getRealPaywayV3Service(this, payZfdd00.getZffs00()).pay(rq, payZfdd00, mchAppConfigContext);
        } else {
            throw new BizException("不支持的微信支付API版本");
        }

    }

    /**
     * 构建微信统一下单请求数据
     * @param payZfdd00
     * @return
     */
    public WxPayUnifiedOrderRequest buildUnifiedOrderRequest(PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext) {
        String payOrderId = payZfdd00.getXtddh0();

        // 微信统一下单请求对象
        WxPayUnifiedOrderRequest request = new WxPayUnifiedOrderRequest();
        request.setOutTradeNo(payOrderId);
        request.setBody(payZfdd00.getDdmc00());
        request.setFeeType("CNY");
        request.setTotalFee(payZfdd00.getZfje00().intValue());
        request.setSpbillCreateIp("127.0.0.1");
        request.setNotifyUrl(getNotifyUrl());
        request.setProductId(System.currentTimeMillis() + "");
        request.setTimeExpire(DateUtil.format(payZfdd00.getDdsxsj(), DatePattern.PURE_DATETIME_PATTERN));
        if(payZfdd00.getDdmc00().contains("住院")){
            request.setGoodsTag("HOSPITALIZATION");
        } else {
            request.setGoodsTag("MEDICINE_AND_TEST");
        }

        // 特约商户
        WxpayMchParams mchParams = (WxpayMchParams) configContextQueryService.queryMchParams(mchAppConfigContext.getFwqdid(), getIfCode());
        if (StringUtils.isNotBlank(mchParams.getSubMchId())) {
            request.setSubMchId(mchParams.getSubMchId());
        }
        if (StringUtils.isNotBlank(mchParams.getSubMchAppId())) {
            request.setSubAppId(mchParams.getSubMchAppId());
        }



        return request;
    }

    /**
     * 构建微信APIV3接口  统一下单请求数据
     * @param payZfdd00
     * @return
     */
    public JSONObject buildV3OrderRequest(PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext) {
        String payOrderId = payZfdd00.getXtddh0();

        // 微信统一下单请求对象
        JSONObject reqJSON = new JSONObject();
        reqJSON.put("out_trade_no", payOrderId);
        reqJSON.put("description", payZfdd00.getDdmc00());
        // 订单失效时间，遵循rfc3339标准格式，格式为yyyy-MM-DDTHH:mm:ss+TIMEZONE,示例值：2018-06-08T10:34:56+08:00
        reqJSON.put("time_expire", String.format("%sT%s+08:00", DateUtil.format(payZfdd00.getDdsxsj(), DatePattern.NORM_DATE_FORMAT), DateUtil.format(payZfdd00.getDdsxsj(), DatePattern.NORM_TIME_FORMAT)));

        reqJSON.put("notify_url", getNotifyUrl(payOrderId));

        JSONObject amount = new JSONObject();
        amount.put("total", payZfdd00.getZfje00().intValue());
        amount.put("currency", "CNY");
        reqJSON.put("amount", amount);

        JSONObject sceneInfo = new JSONObject();
        sceneInfo.put("payer_client_ip", "127.0.0.1");
        reqJSON.put("scene_info", sceneInfo);

        WxPayService wxPayService = configContextQueryService.getWxServiceWrapper(mchAppConfigContext).getWxPayService();

        WxpayMchParams mchParams = (WxpayMchParams) configContextQueryService.queryMchParams(mchAppConfigContext.getFwqdid(), getIfCode());
        if (StringUtils.isNotBlank(mchParams.getSubMchId()) && StringUtils.isNotBlank(mchParams.getSubMchAppId())) {
            reqJSON.put("sub_mchid", mchParams.getSubMchId());
            reqJSON.put("sub_appid", mchParams.getSubMchAppId());
        }
        reqJSON.put("appid", wxPayService.getConfig().getAppId());
        reqJSON.put("mchid", wxPayService.getConfig().getMchId());

        return reqJSON;
    }

}
