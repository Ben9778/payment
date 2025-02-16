package com.ylz.yx.pay.payment.rqrs.payorder;

import com.ylz.yx.pay.core.constants.CS;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/*
* 通用支付数据RS
* 根据set的值，响应不同的payDataType
*/
@Data
public class CommonPayDataRS extends UnifiedOrderRS {

    /** 跳转地址 **/
    private String payUrl;

    /** 二维码地址 **/
    private String codeUrl;

    /** 二维码图片地址 **/
    private String codeImgUrl;

    /** 表单内容 **/
    private String formContent;

    @Override
    public String buildPayDataType(){

        if(StringUtils.isNotEmpty(payUrl)){
            return CS.PAY_DATA_TYPE.PAY_URL;
        }

        if(StringUtils.isNotEmpty(codeUrl)){
            return CS.PAY_DATA_TYPE.CODE_URL;
        }

        if(StringUtils.isNotEmpty(codeImgUrl)){
            return CS.PAY_DATA_TYPE.CODE_IMG_URL;
        }

        if(StringUtils.isNotEmpty(formContent)){
            return CS.PAY_DATA_TYPE.FORM;
        }

        return CS.PAY_DATA_TYPE.PAY_URL;
    }

    @Override
    public String buildPayData(){

        if(StringUtils.isNotEmpty(payUrl)){
            return payUrl;
        }

        if(StringUtils.isNotEmpty(codeUrl)){
            return codeUrl;
        }

        if(StringUtils.isNotEmpty(codeImgUrl)){
            return codeImgUrl;
        }

        if(StringUtils.isNotEmpty(formContent)){
            return formContent;
        }

        return "";
    }

}
