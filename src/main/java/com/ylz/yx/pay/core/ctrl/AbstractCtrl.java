package com.ylz.yx.pay.core.ctrl;

import com.alibaba.fastjson.JSONObject;
import com.ylz.yx.pay.core.beans.RequestKitBean;
import com.ylz.yx.pay.core.constants.ApiCodeEnum;
import com.ylz.yx.pay.core.exception.BizException;
import com.ylz.yx.pay.utils.DateKit;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*
* 抽象公共Ctrl
*/
public abstract class AbstractCtrl {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractCtrl.class);

    @Autowired
    protected HttpServletRequest request;   //自动注入request

    @Autowired
    protected HttpServletResponse response;  //自动注入response

    @Autowired
    protected RequestKitBean requestKitBean;

    /** 获取json格式的请求参数 **/
    protected JSONObject getReqParamJSON(){
        return requestKitBean.getReqParamJSON();
    }

    /** 获取请求参数值 [ T 类型 ], [ 非必填 ] **/
    protected <T> T getVal(String key, Class<T> cls) {
        return getReqParamJSON().getObject(key, cls);
    }

    /** 获取请求参数值 [ T 类型 ], [ 必填 ] **/
    protected <T> T getValRequired(String key, Class<T> cls) {
        T value = getVal(key, cls);
        if(ObjectUtils.isEmpty(value)) {
            throw new BizException(ApiCodeEnum.PARAMS_ERROR, genParamRequiredMsg(key));
        }
        return value;
    }

    /** 获取请求参数值 [ T 类型 ], [ 如为null返回默认值 ] **/
    protected  <T> T getValDefault(String key, T defaultValue, Class<T> cls) {
        T value = getVal(key, cls);
        if(value == null) {
            return defaultValue;
        }
        return value;
    }

    /** 获取请求参数值 String 类型相关函数 **/
    protected String getValString(String key) {
        return getVal(key, String.class);
    }
    protected String getValStringRequired(String key) {
        return getValRequired(key, String.class);
    }
    protected String getValStringDefault(String key, String defaultValue) {
        return getValDefault(key, defaultValue, String.class);
    }

    /** 获取请求参数值 Byte 类型相关函数 **/
    protected Byte getValByte(String key) {
        return getVal(key, Byte.class);
    }
    protected Byte getValByteRequired(String key) {
        return getValRequired(key, Byte.class);
    }
    protected Byte getValByteDefault(String key, Byte defaultValue) {
        return getValDefault(key, defaultValue, Byte.class);
    }

    /** 获取请求参数值 Integer 类型相关函数 **/
    protected Integer getValInteger(String key) {
        return getVal(key, Integer.class);
    }
    protected Integer getValIntegerRequired(String key) {
        return getValRequired(key, Integer.class);
    }
    protected Integer getValIntegerDefault(String key, Integer defaultValue) {
        return getValDefault(key, defaultValue, Integer.class);
    }

    /** 获取请求参数值 Long 类型相关函数 **/
    protected Long getValLong(String key) {
        return getVal(key, Long.class);
    }
    protected Long getValLongRequired(String key) {
        return getValRequired(key, Long.class);
    }
    protected Long getValLongDefault(String key, Long defaultValue) {
        return getValDefault(key, defaultValue, Long.class);
    }

    /** 获取请求参数值 BigDecimal 类型相关函数 **/
    protected BigDecimal getValBigDecimal(String key) {
        return getVal(key, BigDecimal.class);
    }
    protected BigDecimal getValBigDecimalRequired(String key) {
        return getValRequired(key, BigDecimal.class);
    }
    protected BigDecimal getValBigDecimalDefault(String key, BigDecimal defaultValue) {
        return getValDefault(key, defaultValue, BigDecimal.class);
    }

    /** 获取对象类型 **/
    protected <T> T getObject(Class<T> clazz) {

        JSONObject params = getReqParamJSON();
        T result = params.toJavaObject(clazz);

        return result;
    }

    /** 生成参数必填错误信息 **/
    private String genParamRequiredMsg(String key) {
        return "参数" + key + "必填";
    }

    /** 校验参数值不能为空 */
    protected void checkRequired(String... keys) {

        for(String key : keys) {
            String value = getReqParamJSON().getString(key);
            if(StringUtils.isEmpty(value)) {
                throw new BizException(ApiCodeEnum.PARAMS_ERROR, genParamRequiredMsg(key));
            }
        }
    }

    /** 得到前端传入的金额元,转换成长整型分 **/
    public Long getRequiredAmountL(String name) {
        String amountStr = getValStringRequired(name);  // 前端填写的为元,可以为小数点2位
        Long amountL = new BigDecimal(amountStr.trim()).multiply(new BigDecimal(100)).longValue(); // // 转成分
        return amountL;
    }

    /** 得到前端传入的金额元,转换成长整型分 (非必填) **/
    public Long getAmountL(String name) {
        String amountStr = getValString(name);  // 前端填写的为元,可以为小数点2位
        if(StringUtils.isEmpty(amountStr)) {
            return null;
        }
        Long amountL = new BigDecimal(amountStr.trim()).multiply(new BigDecimal(100)).longValue(); // // 转成分
        return amountL;
    }

    /**
     * 处理参数中的金额(将前端传入金额元转成分)
     * modify: 20181206 添加JSON对象中的对象属性转换为分 格式[xxx.xxx]
     * @param names
     */
    public void handleParamAmount(String... names) {
        for(String name : names) {
            String amountStr = getValString(name);  // 前端填写的为元,可以为小数点2位
            if(StringUtils.isNotBlank(amountStr)) {
                Long amountL = new BigDecimal(amountStr.trim()).multiply(new BigDecimal(100)).longValue(); // // 转成分
                if(name.indexOf(".") < 0 ){
                    getReqParamJSON().put(name, amountL);
                    continue;
                }
                getReqParamJSON().getJSONObject(name.substring(0, name.indexOf("."))).put(name.substring(name.indexOf(".")+1), amountL);
            }
        }
    }

    /**
     * 获取查询的时间范围
     * @return
     */
    protected Date[] getQueryDateRange(){
     return DateKit.getQueryDateRange(getReqParamJSON().getString("queryDateRange")); //默认参数为 queryDateRange
    }

    /** 请求参数转换为map格式 **/
    public Map<String, Object> request2payResponseMap(HttpServletRequest request, String[] paramArray) {
        Map<String, Object> responseMap = new HashMap<>();
        for (int i = 0;i < paramArray.length; i++) {
            String key = paramArray[i];
            String v = request.getParameter(key);
            if (v != null) {
                responseMap.put(key, v);
            }
        }
        return responseMap;
    }

    /** 将上传的文件进行保存 - 公共函数 **/
    protected void saveFile(MultipartFile file, String savePath) throws Exception {

        File saveFile = new File(savePath);

        //如果文件夹不存在则创建文件夹
        File dir = saveFile.getParentFile();
        if(!dir.exists()) {
            dir.mkdirs();
        }
        file.transferTo(saveFile);
    }

    /** 获取客户端ip地址 **/
    public String getClientIp() {
        return requestKitBean.getClientIp();
    }

    public String getUserAgent(){
        String userAgent = request.getHeader("User-Agent");
        return StringUtils.isNotEmpty(userAgent) ? userAgent: "未知";
    }
}
