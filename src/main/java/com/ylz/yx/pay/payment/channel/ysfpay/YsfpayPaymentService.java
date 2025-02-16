package com.ylz.yx.pay.payment.channel.ysfpay;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.ylz.core.logging.Logger;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.model.params.ysf.YsfpayMchParams;
import com.ylz.yx.pay.payment.channel.AbstractPaymentService;
import com.ylz.yx.pay.payment.channel.ysfpay.utils.YsfHttpUtil;
import com.ylz.yx.pay.payment.channel.ysfpay.utils.YsfSignUtils;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.AbstractRS;
import com.ylz.yx.pay.payment.rqrs.payorder.UnifiedOrderRQ;
import com.ylz.yx.pay.payment.util.PaywayUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * 云闪付下单
 */
@Service
public class YsfpayPaymentService extends AbstractPaymentService {

    private static final Logger log = new Logger(YsfpayPaymentService.class.getName());

    @Override
    public String getIfCode() {
        return CS.IF_CODE.YSFPAY;
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
        return PaywayUtil.getRealPaywayService(this, payZfdd00.getZffs00()).pay(rq, payZfdd00, mchAppConfigContext);
    }


    /**
     * 封装参数 & 统一请求
     **/
    public JSONObject packageParamAndReq(String apiUri, JSONObject reqParams, String logPrefix, MchAppConfigContext mchAppConfigContext) throws Exception {

        YsfpayMchParams ysfpayMchParams = (YsfpayMchParams) configContextQueryService.queryMchParams(mchAppConfigContext.getFwqdid(), getIfCode());
        reqParams.put("merId", ysfpayMchParams.getMerId()); // 商户号
        reqParams.put("serProvId", ysfpayMchParams.getSerProvId()); //云闪付服务商标识

        //签名
        String isvPrivateCertFile = channelCertConfigKitBean.getCertFilePath(ysfpayMchParams.getPrivateCertFile());
        String isvPrivateCertPwd = ysfpayMchParams.getPrivateCertPwd();
        reqParams.put("signature", YsfSignUtils.signBy256(reqParams, isvPrivateCertFile, isvPrivateCertPwd)); //RSA 签名串

        // 调起上游接口
        log.info(logPrefix + " reqJSON=" + reqParams);
        //向云闪付接口发起支付请求
        String resText = YsfHttpUtil.doPostJson(ysfpayMchParams.getGatewayUrl() + apiUri, null, reqParams);
        log.info(logPrefix + " resJSON=" + resText);

        if (StringUtils.isEmpty(resText)) {
            return null;
        }
        return JSONObject.parseObject(resText);
    }

    /**
     * 云闪付 jsapi下单请求统一发送参数
     **/
    public static void jsapiParamsSet(JSONObject reqParams, PayZfdd00 payZfdd00, String notifyUrl, String returnUrl) {
        String orderType = YsfHttpUtil.getOrderTypeByJSapi(payZfdd00.getZffs00());
        reqParams.put("orderType", orderType); //订单类型： alipayJs-支付宝， wechatJs-微信支付， upJs-银联二维码
        ysfPublicParams(reqParams, payZfdd00);
        reqParams.put("backUrl", notifyUrl); //交易通知地址
        reqParams.put("frontUrl", returnUrl); //前台通知地址
    }

    /**
     * 云闪付 bar下单请求统一发送参数
     **/
    public static void barParamsSet(JSONObject reqParams, PayZfdd00 payZfdd00) {
        String orderType = YsfHttpUtil.getOrderTypeByBar(payZfdd00.getZffs00());
        reqParams.put("orderType", orderType); //订单类型： alipay-支付宝， wechat-微信支付， -unionpay银联二维码
        ysfPublicParams(reqParams, payZfdd00);
        // TODO 终端编号暂时写死
        reqParams.put("termId", "01727367"); // 终端编号
    }

    /**
     * 云闪付公共参数赋值
     **/
    public static void ysfPublicParams(JSONObject reqParams, PayZfdd00 payZfdd00) {
        //获取订单类型
        reqParams.put("orderNo", payZfdd00.getXtddh0()); //订单号
        reqParams.put("orderTime", DateUtil.format(new Date(), DatePattern.PURE_DATETIME_PATTERN)); //订单时间 如：20180702142900
        reqParams.put("txnAmt", payZfdd00.getZfje00()); //交易金额 单位：分，不带小数点
        reqParams.put("currencyCode", "156"); //交易币种 不出现则默认为人民币-156
        reqParams.put("orderInfo", payZfdd00.getDdmc00()); //订单信息 订单描述信息，如：京东生鲜食品
    }
}
