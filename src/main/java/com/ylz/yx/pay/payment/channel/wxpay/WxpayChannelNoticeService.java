package com.ylz.yx.pay.payment.channel.wxpay;

import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.wxpay.bean.notify.SignatureHeader;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyV3Result;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.v3.auth.AutoUpdateCertificatesVerifier;
import com.github.binarywang.wxpay.v3.auth.PrivateKeySigner;
import com.github.binarywang.wxpay.v3.auth.WxPayCredentials;
import com.github.binarywang.wxpay.v3.util.PemUtils;
import com.ylz.core.logging.Logger;
import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.exception.BizException;
import com.ylz.yx.pay.core.exception.ResponseException;
import com.ylz.yx.pay.payment.channel.AbstractChannelNoticeService;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.model.WxServiceWrapper;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import com.ylz.yx.pay.payment.service.ConfigContextQueryService;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;

/*
 * 微信回调
 */
@Service
public class WxpayChannelNoticeService extends AbstractChannelNoticeService {

    private static final Logger log = new Logger(WxpayChannelNoticeService.class.getName());

    @Autowired
    private ConfigContextQueryService configContextQueryService;

    @Autowired
    private JdbcGateway jdbcGateway;
    //private PayOrderService payOrderService;

    @Override
    public String getIfCode() {
        return CS.IF_CODE.WXPAY;
    }

    @Override
    public MutablePair<String, Object> parseParams(HttpServletRequest request, String urlOrderId, NoticeTypeEnum noticeTypeEnum) {

        try {
            if (StringUtils.isNotBlank(urlOrderId)) {     // V3接口回调

                // 获取订单信息
                PayZfdd00 payZfdd00 = jdbcGateway.selectOne("pay.zfdd00.selectByXtddh0", urlOrderId);
                if (payZfdd00 == null) {
                    throw new BizException("订单不存在");
                }

                //获取支付参数 (缓存数据) 和 商户信息
                MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(payZfdd00.getFwqdid());
                if (mchAppConfigContext == null) {
                    throw new BizException("获取商户信息失败");
                }

                // 验签 && 获取订单回调数据
                WxPayOrderNotifyV3Result.DecryptNotifyResult result = parseOrderNotifyV3Result(request, mchAppConfigContext);

                return MutablePair.of(result.getOutTradeNo(), result);

            } else {     // V2接口回调
                String xmlResult = IOUtils.toString(request.getInputStream(), request.getCharacterEncoding());
                if (StringUtils.isEmpty(xmlResult)) {
                    return null;
                }

                WxPayOrderNotifyResult result = WxPayOrderNotifyResult.fromXML(xmlResult);
                String payOrderId = result.getOutTradeNo();
                return MutablePair.of(payOrderId, result);
            }
        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText("ERROR");
        }
    }

    @Override
    public ChannelRetMsg doNotice(HttpServletRequest request, Object params, PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext, NoticeTypeEnum noticeTypeEnum) {
        try {
            ChannelRetMsg channelResult = new ChannelRetMsg();
            channelResult.setChannelState(ChannelRetMsg.ChannelState.WAITING); // 默认支付中

            WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);

            if (CS.PAY_IF_VERSION.WX_V2.equals(wxServiceWrapper.getApiVersion())) { // V2
                // 获取回调参数
                WxPayOrderNotifyResult result = (WxPayOrderNotifyResult) params;

                WxPayService wxPayService = wxServiceWrapper.getWxPayService();

                // 验证参数
                verifyWxPayParams(wxPayService, result, payZfdd00);

                channelResult.setChannelOrderId(result.getTransactionId()); //渠道订单号
                channelResult.setChannelUserId(result.getOpenid()); //支付用户ID
                channelResult.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                channelResult.setResponseEntity(textResp(WxPayNotifyResponse.successResp("OK")));

            } else if (CS.PAY_IF_VERSION.WX_V3.equals(wxServiceWrapper.getApiVersion())) { // V3
                // 获取回调参数
                WxPayOrderNotifyV3Result.DecryptNotifyResult result = (WxPayOrderNotifyV3Result.DecryptNotifyResult) params;

                // 验证参数
                verifyWxPayParams(result, payZfdd00);

                String channelState = result.getTradeState();
                if ("SUCCESS".equals(channelState)) {
                    channelResult.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                } else if ("CLOSED".equals(channelState)
                        || "REVOKED".equals(channelState)
                        || "PAYERROR".equals(channelState)) {  //CLOSED—已关闭， REVOKED—已撤销, PAYERROR--支付失败
                    channelResult.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL); //支付失败
                }

                channelResult.setChannelOrderId(result.getTransactionId()); //渠道订单号
                WxPayOrderNotifyV3Result.Payer payer = result.getPayer();
                if (payer != null) {
                    channelResult.setChannelUserId(payer.getOpenid()); //支付用户ID
                }

                JSONObject resJSON = new JSONObject();
                resJSON.put("code", "SUCCESS");
                resJSON.put("message", "成功");

                ResponseEntity okResponse = jsonResp(resJSON);
                channelResult.setResponseEntity(okResponse); //响应数据

            } else {
                throw ResponseException.buildText("API_VERSION ERROR");
            }

            return channelResult;

        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText("ERROR");
        }
    }

    /**
     * V2接口验证微信支付通知参数
     *
     * @return
     */
    public void verifyWxPayParams(WxPayService wxPayService, WxPayOrderNotifyResult result, PayZfdd00 payZfdd00) {

        try {
            result.checkResult(wxPayService, WxPayConstants.SignType.MD5, true);

            // 核对金额
            Integer total_fee = result.getTotalFee();            // 总金额
            long wxPayAmt = new BigDecimal(total_fee).longValue();
            long dbPayAmt = payZfdd00.getZfje00().longValue();
            if (dbPayAmt != wxPayAmt) {
                throw ResponseException.buildText("AMOUNT ERROR");
            }
        } catch (Exception e) {
            throw ResponseException.buildText("ERROR");
        }
    }

    /**
     * V3校验通知签名
     *
     * @param request             请求信息
     * @param mchAppConfigContext 商户配置
     * @return true:校验通过 false:校验不通过
     */
    private WxPayOrderNotifyV3Result.DecryptNotifyResult parseOrderNotifyV3Result(HttpServletRequest request, MchAppConfigContext mchAppConfigContext) throws Exception {
        SignatureHeader header = new SignatureHeader();
        header.setTimeStamp(request.getHeader("Wechatpay-Timestamp"));
        header.setNonce(request.getHeader("Wechatpay-Nonce"));
        header.setSerial(request.getHeader("Wechatpay-Serial"));
        header.setSignature(request.getHeader("Wechatpay-Signature"));

        // 获取加密信息
        String params = getReqParamFromBody();

        log.info("\n【请求头信息】：" + header + "\n【加密数据】：" + params);

        WxPayService wxPayService = configContextQueryService.getWxServiceWrapper(mchAppConfigContext).getWxPayService();
        WxPayConfig wxPayConfig = wxPayService.getConfig();
        // 自动获取微信平台证书
        PrivateKey privateKey = PemUtils.loadPrivateKey(new FileInputStream(wxPayConfig.getPrivateKeyPath()));
        AutoUpdateCertificatesVerifier verifier = new AutoUpdateCertificatesVerifier(
                new WxPayCredentials(wxPayConfig.getMchId(), new PrivateKeySigner(wxPayConfig.getCertSerialNo(), privateKey)),
                wxPayConfig.getApiV3Key().getBytes(StandardCharsets.UTF_8));
        wxPayConfig.setVerifier(verifier);
        wxPayService.setConfig(wxPayConfig);

        WxPayOrderNotifyV3Result result = wxPayService.parseOrderNotifyV3Result(params, header);

        return result.getResult();
    }

    /**
     * V3接口验证微信支付通知参数
     *
     * @return
     */
    public void verifyWxPayParams(WxPayOrderNotifyV3Result.DecryptNotifyResult result, PayZfdd00 payZfdd00) {

        try {
            // 核对金额
            Integer total_fee = result.getAmount().getTotal();            // 总金额
            long wxPayAmt = new BigDecimal(total_fee).longValue();
            long dbPayAmt = payZfdd00.getZfje00().longValue();
            if (dbPayAmt != wxPayAmt) {
                throw ResponseException.buildText("AMOUNT ERROR");
            }
        } catch (Exception e) {
            throw ResponseException.buildText("ERROR");
        }
    }

}
