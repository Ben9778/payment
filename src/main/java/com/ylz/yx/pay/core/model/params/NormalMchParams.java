package com.ylz.yx.pay.core.model.params;

import com.alibaba.fastjson.JSONObject;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.model.params.alipay.AlipayMchParams;
import com.ylz.yx.pay.core.model.params.pospay.PospayMchParams;
import com.ylz.yx.pay.core.model.params.union.UnionpayMchParams;
import com.ylz.yx.pay.core.model.params.wxpay.WxpayMchParams;
import com.ylz.yx.pay.core.model.params.ysf.YsfpayMchParams;

/*
 * 抽象类 普通商户参数定义
 */
public abstract class NormalMchParams {

    public static NormalMchParams factory(String ifCode, String paramsStr){

        if(CS.IF_CODE.WXPAY.equals(ifCode)){
            return JSONObject.parseObject(paramsStr, WxpayMchParams.class);
        }else if(CS.IF_CODE.ALIPAY.equals(ifCode)){
            return JSONObject.parseObject(paramsStr, AlipayMchParams.class);
        }else if(CS.IF_CODE.YSFPAY.equals(ifCode)){
            return JSONObject.parseObject(paramsStr, YsfpayMchParams.class);
        }else if(CS.IF_CODE.UNIONPAY.equals(ifCode)){
            return JSONObject.parseObject(paramsStr, UnionpayMchParams.class);
        }else if(CS.IF_CODE.POSPAY.equals(ifCode)){
            return JSONObject.parseObject(paramsStr, PospayMchParams.class);
        }
        return null;
    }

    /**
     *  敏感数据脱敏
     */
    public abstract String deSenData();

}
