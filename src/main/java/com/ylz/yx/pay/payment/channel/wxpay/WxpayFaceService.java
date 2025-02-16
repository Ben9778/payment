package com.ylz.yx.pay.payment.channel.wxpay;

import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.wxpay.bean.request.WxPayFaceAuthInfoRequest;
import com.github.binarywang.wxpay.bean.result.WxPayFaceAuthInfoResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.ylz.yx.pay.core.constants.ApiCodeEnum;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.payment.channel.IFaceService;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.model.WxServiceWrapper;
import com.ylz.yx.pay.payment.rqrs.AbstractRS;
import com.ylz.yx.pay.payment.rqrs.FaceInitRS;
import com.ylz.yx.pay.payment.service.ConfigContextQueryService;
import com.ylz.yx.pay.payment.util.ApiResBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/*
* 微信支付 刷脸设备初始化实现类
*/
@Service
public class WxpayFaceService implements IFaceService {

    @Autowired private ConfigContextQueryService configContextQueryService;

    @Override
    public String getIfCode() {
        return CS.IF_CODE.WXPAY;
    }

    @Override
    public AbstractRS init(JSONObject reqParams, MchAppConfigContext mchAppConfigContext) {
        Map<String, Object> map = new HashMap<>();
        WxPayFaceAuthInfoRequest request = new WxPayFaceAuthInfoRequest();
        request.setStoreId("");//门店编号， 由商户定义， 各门店唯一。
        request.setStoreName("");//门店名称，由商户定义。（可用于展示）
        request.setDeviceId("");//终端设备编号，由商户定义。
        request.setRawdata(reqParams.getString("rawdata"));//初始化数据。由微信人脸SDK的接口返回。
        request.setNow("");//取当前时间，10位unix时间戳。 例如：1239878956
        request.setVersion("1");//版本号。固定为1

        WxServiceWrapper wxServiceWrapper = configContextQueryService.getWxServiceWrapper(mchAppConfigContext);

        FaceInitRS res = ApiResBuilder.buildSuccess(FaceInitRS.class);

        try {
            WxPayFaceAuthInfoResult result = wxServiceWrapper.getWxPayService().getWxPayFaceAuthInfo(request);
            res.setAuthinfo(result.getAuthinfo());
            res.setCode(ApiCodeEnum.SUCCESS.getCode());
        } catch (WxPayException e) {
            //e.printStackTrace();
            res.setCode(ApiCodeEnum.CUSTOM_FAIL.getCode());
            res.setMsg(e.getCustomErrorMsg());
        }
        return res;
    }

}
