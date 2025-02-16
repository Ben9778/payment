package com.ylz.yx.pay.payment.channel.unionpay;

import com.alibaba.fastjson.JSONObject;
import com.ylz.core.logging.Logger;
import com.ylz.yx.pay.config.ApplicationProperty;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.core.exception.ResponseException;
import com.ylz.yx.pay.core.model.params.union.UnionpayMchParams;
import com.ylz.yx.pay.payment.channel.AbstractChannelNoticeService;
import com.ylz.yx.pay.payment.channel.unionpay.utils.UnionSignUtils;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * 银联回调
 */
@Service
public class UnionpayChannelNoticeService extends AbstractChannelNoticeService {

    private static final Logger log = new Logger(UnionpayChannelNoticeService.class.getName());

    @Resource
    protected ApplicationProperty applicationProperty;

    @Override
    public String getIfCode() {
        return CS.IF_CODE.UNIONPAY;
    }

    @Override
    public MutablePair<String, Object> parseParams(HttpServletRequest request, String urlOrderId, NoticeTypeEnum noticeTypeEnum) {

        try {

            JSONObject params = getReqParamJSON();
            String payOrderId = params.getString("orderId");
            return MutablePair.of(payOrderId, params);

        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText("ERROR");
        }
    }

    @Override
    public ChannelRetMsg doNotice(HttpServletRequest request, Object params, PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext, NoticeTypeEnum noticeTypeEnum) {
        try {

            ChannelRetMsg result = ChannelRetMsg.confirmSuccess(null);

            String logPrefix = "【处理银联支付回调】";

            // 获取请求参数
            Map<String, String> reqParam = getAllRequestParam(request);
            log.info(logPrefix+" 回调参数, jsonParams："+ reqParam);
            Map<String, String> valideData = null;
            if (null != reqParam && !reqParam.isEmpty()) {
                Iterator<Entry<String, String>> it = reqParam.entrySet().iterator();
                valideData = new HashMap<String, String>(reqParam.size());
                while (it.hasNext()) {
                    Entry<String, String> e = it.next();
                    String key = e.getKey();
                    String value = e.getValue();
                    valideData.put(key, value);
                }
            }
            // 校验支付回调
            boolean verifyResult = verifyParams(reqParam, mchAppConfigContext);
            // 验证参数失败
            if(!verifyResult){
                throw ResponseException.buildText("ERROR");
            }
            log.info(logPrefix+"验证支付通知数据及签名通过");

            //验签成功后判断上游订单状态
            ResponseEntity okResponse = textResp("success");
            result.setResponseEntity(okResponse);
            result.setChannelOrderId(valideData.get("queryId"));
            return result;
        } catch (Exception e) {
            log.error("error", e);
            throw ResponseException.buildText("ERROR");
        }
    }

    /**
     * 验证银联支付通知参数
     * @return
     */
    public boolean verifyParams(Map<String, String> valideData, MchAppConfigContext mchAppConfigContext) {

        UnionpayMchParams unionpayMchParams = (UnionpayMchParams)configContextQueryService.queryMchParams(mchAppConfigContext.getFwqdid(), getIfCode());

        return UnionSignUtils.validate(valideData, applicationProperty.getFileStoragePath(), unionpayMchParams, "UTF-8");
    }

    /**
     * 获取请求参数中所有的信息
     *
     * @param request
     * @return
     */
    public static Map<String, String> getAllRequestParam(final HttpServletRequest request) {
        Map<String, String> res = new HashMap<String, String>();
        Enumeration<?> temp = request.getParameterNames();
        if (null != temp) {
            while (temp.hasMoreElements()) {
                String en = (String) temp.nextElement();
                String value = request.getParameter(en);
                res.put(en, value);
                //在报文上送时，如果字段的值为空，则不上送<下面的处理为在获取所有参数数据时，判断若值为空，则删除这个字段>
                //System.out.println("ServletUtil类247行  temp数据的键=="+en+"     值==="+value);
                if (null == res.get(en) || "".equals(res.get(en))) {
                    res.remove(en);
                }
            }
        }
        return res;
    }
}
