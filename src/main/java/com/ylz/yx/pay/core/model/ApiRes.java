package com.ylz.yx.pay.core.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ylz.yx.pay.core.constants.ApiCodeEnum;
import com.ylz.yx.pay.utils.JsonKit;
import com.ylz.yx.pay.utils.PayKit;
import lombok.AllArgsConstructor;
import lombok.Data;

/*
* 接口返回对象
*/
@Data
@AllArgsConstructor
public class ApiRes {

    /** 业务响应码 **/
    private Integer code;

    /** 业务响应信息 **/
    private String msg;

    /** 数据对象 **/
    private Object data;

    /** 签名值 **/
    private String sign;

    /** 输出json格式字符串 **/
    public String toJSONString(){
        return JSON.toJSONString(this);
    }

    /** 业务处理成功 **/
    public static ApiRes ok(){
        return ok(null);
    }

    /** 业务处理成功 **/
    public static ApiRes ok(Object data){
        return new ApiRes(ApiCodeEnum.SUCCESS.getCode(), ApiCodeEnum.SUCCESS.getMsg(), data, null);
    }

    /** 业务处理成功, 自动签名 **/
    public static ApiRes okWithSign(Object data, String mchKey){

        if(data == null){
            return new ApiRes(ApiCodeEnum.SUCCESS.getCode(), ApiCodeEnum.SUCCESS.getMsg(), null, null);
        }

        JSONObject jsonObject = (JSONObject)JSONObject.toJSON(data);
        String sign = PayKit.getSign(jsonObject, mchKey);//对data和商户秘钥mchkey进行加密签名
        return new ApiRes(ApiCodeEnum.SUCCESS.getCode(), ApiCodeEnum.SUCCESS.getMsg(), data, sign);
    }

    /** 业务处理成功, 返回简单json格式 **/
    public static ApiRes ok4newJson(String key, Object val){
        return ok(JsonKit.newJson(key, val));
    }

    /** 业务处理失败 **/
    public static ApiRes fail(ApiCodeEnum apiCodeEnum, String... params){

        if(params == null || params.length <= 0){
            return new ApiRes(apiCodeEnum.getCode(), apiCodeEnum.getMsg(), null, null);
        }
        return new ApiRes(apiCodeEnum.getCode(), String.format(apiCodeEnum.getMsg(), params), null, null);
    }

    /** 自定义错误信息, 原封不用的返回输入的错误信息 **/
    public static ApiRes customFail(String customMsg){
        return new ApiRes(ApiCodeEnum.CUSTOM_FAIL.getCode(), customMsg, null, null);
    }

}
