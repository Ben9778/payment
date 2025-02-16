package com.ylz.yx.pay.payment.model;

import com.ylz.yx.pay.core.entity.PayFwqd00;
import com.ylz.yx.pay.core.model.params.NormalMchParams;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/*
* 商户应用支付参数信息
* 放置到内存， 避免多次查询操作
*/
@Data
public class MchAppConfigContext {


    /** 商户信息缓存 */
    private String fwqdid;
    private PayFwqd00 payFwqd00;

    /** 商户支付配置信息缓存,  <接口代码, 支付参数>  */
    private Map<String, Map<String, NormalMchParams>> normalMchParamsMap = new HashMap<>();

    /** 缓存支付宝client 对象 **/
    private AlipayClientWrapper alipayClientWrapper;

    /** 缓存 wxServiceWrapper 对象 **/
    private WxServiceWrapper wxServiceWrapper;

    /** 获取商户配置信息 **/
    public NormalMchParams getNormalMchParamsByIfCode(String fwqdid, String ifCode){
        return normalMchParamsMap.get(fwqdid).get(ifCode);
    }

    /** 获取商户配置信息 **/
    public <T> T getNormalMchParamsByIfCode(String ifCode, Class<? extends NormalMchParams> cls){
        return (T)normalMchParamsMap.get(ifCode);
    }

    public AlipayClientWrapper getAlipayClientWrapper(){
        return alipayClientWrapper;
    }

    public WxServiceWrapper getWxServiceWrapper(){
        return wxServiceWrapper;
    }

}
