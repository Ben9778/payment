package com.ylz.yx.pay.payment.channel.alipay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.request.ZolozAuthenticationCustomerSmilepayInitializeRequest;
import com.alipay.api.response.ZolozAuthenticationCustomerSmilepayInitializeResponse;
import com.ylz.core.logging.Logger;
import com.ylz.yx.pay.core.constants.ApiCodeEnum;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.payment.channel.IFaceService;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.AbstractRS;
import com.ylz.yx.pay.payment.rqrs.FaceInitRS;
import com.ylz.yx.pay.payment.service.ConfigContextQueryService;
import com.ylz.yx.pay.payment.util.ApiResBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/*
 * 支付宝： 刷脸设备初始化实现类
 */
@Service
public class AlipayFaceService implements IFaceService {

    private static final Logger log = new Logger(AlipayFaceService.class.getName());

    @Autowired
    private ConfigContextQueryService configContextQueryService;

    @Override
    public String getIfCode() {
        return CS.IF_CODE.ALIPAY;
    }

    @Override
    public AbstractRS init(JSONObject reqParams, MchAppConfigContext mchAppConfigContext) {
        Map<String, Object> map = new HashMap<>();
        ZolozAuthenticationCustomerSmilepayInitializeRequest request = new ZolozAuthenticationCustomerSmilepayInitializeRequest();
        JSONObject zimmetainfo = JSON.parseObject(reqParams.getString("metaInfo"));
        /*JSONObject extInfo = new JSONObject();
        *//* start: 如果是姓名＋身份证号方式的刷脸支付，请加入以下代码 *//*
        extInfo.put("certNo", reqParams.getString("certNo"));  //必填，当前被认证用户的身份证号
        extInfo.put("certName", reqParams.getString("certName")); //必填，当前被认证用户的姓名（保持和身份证一致）
        extInfo.put("certType", "IDCARD");  //必填，写为IDCARD，表明身份证
        extInfo.put("bizType", "8");  //必填，固定写为8，表明基于姓名和身份证号的刷脸支付场景
        *//* end: -------------------------------------------- *//*
        zimmetainfo.put("extInfo", extInfo);*/
        request.setBizContent(zimmetainfo.toJSONString());

        ZolozAuthenticationCustomerSmilepayInitializeResponse zolozResponse = configContextQueryService.getAlipayClientWrapper(mchAppConfigContext).execute(request);

        FaceInitRS res = ApiResBuilder.buildSuccess(FaceInitRS.class);

        if (zolozResponse != null && "10000".equals(zolozResponse.getCode())) {
            String result = zolozResponse.getResult();
            JSONObject resultJson = JSON.parseObject(result);
            String zimId = resultJson.getString("zimId");
            String zimInitClientData = resultJson.getString("zimInitClientData");

            res.setZimId(zimId);
            res.setZimInitClientData(zimInitClientData);
            res.setCode(ApiCodeEnum.SUCCESS.getCode());
            log.error("人脸初始化成功：" + zimInitClientData);
        } else {
            res.setCode(ApiCodeEnum.CUSTOM_FAIL.getCode());
            res.setMsg(zolozResponse.getSubMsg());
            log.error("人脸初始化失败：" + zolozResponse.getSubMsg());
        }
        return res;

    }

}
