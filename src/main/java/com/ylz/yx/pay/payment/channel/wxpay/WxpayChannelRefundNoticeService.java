package com.ylz.yx.pay.payment.channel.wxpay;

import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.wxpay.bean.notify.SignatureHeader;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.github.binarywang.wxpay.bean.notify.WxPayRefundNotifyResult;
import com.github.binarywang.wxpay.bean.notify.WxPayRefundNotifyV3Result;
import com.github.binarywang.wxpay.config.WxPayConfig;
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
import com.ylz.yx.pay.core.model.params.wxpay.WxpayMchParams;
import com.ylz.yx.pay.payment.channel.AbstractChannelRefundNoticeService;
import com.ylz.yx.pay.core.entity.PayTkdd00;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.model.WxServiceWrapper;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
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
 * 微信支付 退款回调接口实现类
 */
@Service
public class WxpayChannelRefundNoticeService extends AbstractChannelRefundNoticeService {

    private static final Logger log = new Logger(WxpayChannelRefundNoticeService.class.getName());

    @Autowired
    private JdbcGateway jdbcGateway;
    //RefundOrderService refundOrderService;

    @Override
    public String getIfCode() {
        return CS.IF_CODE.WXPAY;
    }

    @Override
    public MutablePair<String, Object> parseParams(HttpServletRequest request, String urlOrderId, NoticeTypeEnum noticeTypeEnum) {

        try {
            // 获取订单信息
            PayTkdd00 payTkdd00 = jdbcGateway.selectOne("pay.tkdd00.selectByXtddh0", urlOrderId);
            if(payTkdd00 == null){
                throw new BizException("订单不存在");
            }

            //获取支付参数 (缓存数据) 和 商户信息
            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(payTkdd00.getFwqdid());
            if(mchAppConfigContext == null){
                throw new BizException("获取商户信息失败");
            }
            WxpayMchParams normalMchParams = (WxpayMchParams) configContextQueryService.queryMchParams(mchAppConfigContext.getFwqdid(), getIfCode());
            String apiVersion = normalMchParams.getApiVersion();
            String wxKey = CS.PAY_IF_VERSION.WX_V2.equals(apiVersion)?normalMchParams.getKey():normalMchParams.getApiV3Key();

            if(CS.PAY_IF_VERSION.WX_V3.equals(apiVersion)){     // V3接口回调
                // 验签 && 获取订单回调数据
                WxPayRefundNotifyV3Result.DecryptNotifyResult result = parseOrderNotifyV3Result(request, mchAppConfigContext);
                return MutablePair.of(urlOrderId, result);

            } else if (CS.PAY_IF_VERSION.WX_V2.equals(apiVersion)){     // V2接口回调
                String xmlResult = IOUtils.toString(request.getInputStream(), request.getCharacterEncoding());
                if(StringUtils.isEmpty(xmlResult)) {
                    return null;
                }
                WxPayRefundNotifyResult result = WxPayRefundNotifyResult.fromXML(xmlResult, wxKey);
                return MutablePair.of(urlOrderId, result.getReqInfo());
            }
            return null;
        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText("ERROR");
        }
    }

    @Override
    public ChannelRetMsg doNotice(HttpServletRequest request, Object params, PayTkdd00 payTkdd00, MchAppConfigContext mchAppConfigContext, NoticeTypeEnum noticeTypeEnum) {
        try {
            ChannelRetMsg result = new ChannelRetMsg();
            WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);

            result.setChannelState(ChannelRetMsg.ChannelState.WAITING); // 默认支付中
            if (CS.PAY_IF_VERSION.WX_V3.equals(wxServiceWrapper.getApiVersion())) {
                WxPayRefundNotifyV3Result.DecryptNotifyResult notifyResult = (WxPayRefundNotifyV3Result.DecryptNotifyResult) params;
                // 验证参数
                verifyWxPay3Params(notifyResult, payTkdd00);
                String refundStatus = notifyResult.getRefundStatus();
                if ("SUCCESS".equals(refundStatus)) {
                    result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                }else {  //CHANGE—退款异常， REFUNDCLOSE—退款关闭
                    result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL); //退款失败
                }
                result.setChannelOrderId(notifyResult.getTransactionId()); // 渠道订单号

                JSONObject resJSON = new JSONObject();
                resJSON.put("code", "SUCCESS");
                resJSON.put("message", "成功");

                ResponseEntity okResponse = jsonResp(resJSON);
                result.setResponseEntity(okResponse); //响应数据
            }else if (CS.PAY_IF_VERSION.WX_V2.equals(wxServiceWrapper.getApiVersion())) {
                // 获取回调参数
                WxPayRefundNotifyResult.ReqInfo notifyResult = (WxPayRefundNotifyResult.ReqInfo) params;
                // 验证参数
                verifyWxPay2Params(notifyResult, payTkdd00);

                result.setChannelOrderId(notifyResult.getTransactionId()); //渠道订单号
                if ("SUCCESS".equals(notifyResult.getRefundStatus())) {
                    result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                }else {  //CHANGE—退款异常， REFUNDCLOSE—退款关闭
                    result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL); //退款失败
                }
                result.setResponseEntity(textResp(WxPayNotifyResponse.successResp("OK")));
            }else {
                result.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL); //退款失败
            }
            return result;
        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText("ERROR");
        }
    }

    /**
     * V3校验通知签名
     * @param request 请求信息
     * @param mchAppConfigContext 商户配置
     * @return true:校验通过 false:校验不通过
     */
    private WxPayRefundNotifyV3Result.DecryptNotifyResult parseOrderNotifyV3Result(HttpServletRequest request, MchAppConfigContext mchAppConfigContext) throws Exception {
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

        WxPayRefundNotifyV3Result result = wxPayService.parseRefundNotifyV3Result(params, header);

        return result.getResult();
    }

    /**
     * V3接口验证微信支付通知参数
     * @return
     */
    public void verifyWxPay3Params(WxPayRefundNotifyV3Result.DecryptNotifyResult result, PayTkdd00 payTkdd00) {

        try {
            // 核对金额
            Integer total_fee = result.getAmount().getTotal();  // 退款金额
            long wxPayAmt = new BigDecimal(total_fee).longValue();
            long dbPayAmt = payTkdd00.getTkje00().longValue();
            if (dbPayAmt != wxPayAmt) {
                throw ResponseException.buildText("AMOUNT ERROR");
            }
        } catch (Exception e) {
            throw ResponseException.buildText("ERROR");
        }
    }

    /**
     * V2接口验证微信支付通知参数
     * @return
     */
    public void verifyWxPay2Params(WxPayRefundNotifyResult.ReqInfo result, PayTkdd00 payTkdd00) {

        try {
            // 核对金额
            Integer total_fee = result.getRefundFee();  // 退款金额
            long wxPayAmt = new BigDecimal(total_fee).longValue();
            long dbPayAmt = payTkdd00.getTkje00().longValue();
            if (dbPayAmt != wxPayAmt) {
                throw ResponseException.buildText("AMOUNT ERROR");
            }
        } catch (Exception e) {
            throw ResponseException.buildText("ERROR");
        }
    }
}
