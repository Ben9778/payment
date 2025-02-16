package com.ylz.yx.pay.payment.rqrs.payorder;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.payment.rqrs.AbstractMchAppRQ;
import com.ylz.yx.pay.payment.rqrs.payorder.payway.*;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.BeanUtils;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.lang.reflect.Type;

/*
* 创建订单请求参数对象
* 聚合支付接口（统一下单）
*/
@Data
public class UnifiedOrderRQ extends AbstractMchAppRQ {

    /** 商户订单号 **/
    @NotBlank(message="商户订单号不能为空")
    private String mchOrderNo;

    /** 支付方式  如： wxpay_jsapi,alipay_wap等   **/
    @NotBlank(message="支付方式不能为空")
    private String wayCode;

    /** 支付金额， 单位：分 **/
    @NotNull(message="支付金额不能为空")
    @Min(value = 1, message = "支付金额不能为空")
    private Long amount;

    /** 商品标题 **/
    @NotBlank(message="商品标题不能为空")
    private String subject;

    /** 订单失效时间, 单位：秒 **/
    private Integer expiredTime;

    /** 异步通知地址 **/
    private String notifyUrl;

    /** 跳转通知地址 **/
    private String returnUrl;

    /** 特定渠道发起额外参数 **/
    private String channelExtra;

    /** 商户扩展参数 **/
    private String extParam;

    /** 患者姓名 **/
    //@NotBlank(message="患者姓名不能为空")
    private String userName;
    /** 患者手机号 **/
    private String phone;
    /** 患者身份证号 **/
    //@NotBlank(message="患者身份证号不能为空")
    private String idCard;
    /** 就诊卡号/住院号 **/
    //@NotBlank(message="就诊卡号/住院号不能为空")
    private String cardNo;
    /** 订单类型 **/
    //@NotBlank(message="订单类型不能为空")
    private String orderType;

    /** 操作员ID **/
    @NotBlank(message="操作员ID不能为空")
    private String operatorId;
    /** 操作员姓名 **/
    @NotBlank(message="操作员姓名不能为空")
    private String operatorName;

    /** 医院ID **/
    private String hospitalId;

    /** 返回真实的bizRQ **/
    public UnifiedOrderRQ buildBizRQ(){

        if(CS.PAY_WAY_CODE.ALI_BAR.equals(wayCode)){
            AliBarOrderRQ bizRQ = JSONObject.parseObject(StringUtils.defaultIfEmpty(this.channelExtra, "{}"), AliBarOrderRQ.class);
            BeanUtils.copyProperties(this, bizRQ);
            return bizRQ;
        }else if(CS.PAY_WAY_CODE.ALI_JSAPI.equals(wayCode)){
            AliJsapiOrderRQ bizRQ = JSONObject.parseObject(StringUtils.defaultIfEmpty(this.channelExtra, "{}"), AliJsapiOrderRQ.class);
            BeanUtils.copyProperties(this, bizRQ);
            return bizRQ;
        }else if(CS.PAY_WAY_CODE.QR_CASHIER.equals(wayCode)){
            QrCashierOrderRQ bizRQ = JSONObject.parseObject(StringUtils.defaultIfEmpty(this.channelExtra, "{}"), QrCashierOrderRQ.class);
            BeanUtils.copyProperties(this, bizRQ);
            return bizRQ;
        }else if(CS.PAY_WAY_CODE.WX_JSAPI.equals(wayCode) || CS.PAY_WAY_CODE.WX_LITE.equals(wayCode)){
            WxJsapiOrderRQ bizRQ = JSONObject.parseObject(StringUtils.defaultIfEmpty(this.channelExtra, "{}"), WxJsapiOrderRQ.class);
            BeanUtils.copyProperties(this, bizRQ);
            return bizRQ;
        }else if(CS.PAY_WAY_CODE.WX_BAR.equals(wayCode)){
            WxBarOrderRQ bizRQ = JSONObject.parseObject(StringUtils.defaultIfEmpty(this.channelExtra, "{}"), WxBarOrderRQ.class);
            BeanUtils.copyProperties(this, bizRQ);
            return bizRQ;
        }else if(CS.PAY_WAY_CODE.WX_NATIVE.equals(wayCode)){
            WxNativeOrderRQ bizRQ = JSONObject.parseObject(StringUtils.defaultIfEmpty(this.channelExtra, "{}"), WxNativeOrderRQ.class);
            BeanUtils.copyProperties(this, bizRQ);
            return bizRQ;
        }else if(CS.PAY_WAY_CODE.WX_H5.equals(wayCode)){
            WxH5OrderRQ bizRQ = JSONObject.parseObject(StringUtils.defaultIfEmpty(this.channelExtra, "{}"), WxH5OrderRQ.class);
            BeanUtils.copyProperties(this, bizRQ);
            return bizRQ;
        }else if(CS.PAY_WAY_CODE.YSF_BAR.equals(wayCode)){
            YsfBarOrderRQ bizRQ = JSONObject.parseObject(StringUtils.defaultIfEmpty(this.channelExtra, "{}"), YsfBarOrderRQ.class);
            BeanUtils.copyProperties(this, bizRQ);
            return bizRQ;
        }else if(CS.PAY_WAY_CODE.YSF_JSAPI.equals(wayCode)){
            YsfJsapiOrderRQ bizRQ = JSONObject.parseObject(StringUtils.defaultIfEmpty(this.channelExtra, "{}"), YsfJsapiOrderRQ.class);
            BeanUtils.copyProperties(this, bizRQ);
            return bizRQ;
        }else if(CS.PAY_WAY_CODE.AUTO_BAR.equals(wayCode)){
            AutoBarOrderRQ bizRQ = JSONObject.parseObject(StringUtils.defaultIfEmpty(this.channelExtra, "{}"), AutoBarOrderRQ.class);
            BeanUtils.copyProperties(this, bizRQ);
            return bizRQ;
        }else if(CS.PAY_WAY_CODE.ALI_APP.equals(wayCode)){
            AliAppOrderRQ bizRQ = JSONObject.parseObject(StringUtils.defaultIfEmpty(this.channelExtra, "{}"), AliAppOrderRQ.class);
            BeanUtils.copyProperties(this, bizRQ);
            return bizRQ;
        }else if(CS.PAY_WAY_CODE.ALI_WAP.equals(wayCode)){
            AliWapOrderRQ bizRQ = JSONObject.parseObject(StringUtils.defaultIfEmpty(this.channelExtra, "{}"), AliWapOrderRQ.class);
            BeanUtils.copyProperties(this, bizRQ);
            return bizRQ;
        }else if(CS.PAY_WAY_CODE.ALI_PC.equals(wayCode)){
            AliPcOrderRQ bizRQ = JSONObject.parseObject(StringUtils.defaultIfEmpty(this.channelExtra, "{}"), AliPcOrderRQ.class);
            BeanUtils.copyProperties(this, bizRQ);
            return bizRQ;
        }else if(CS.PAY_WAY_CODE.ALI_QR.equals(wayCode)){
            AliQrOrderRQ bizRQ = JSONObject.parseObject(StringUtils.defaultIfEmpty(this.channelExtra, "{}"), AliQrOrderRQ.class);
            BeanUtils.copyProperties(this, bizRQ);
            return bizRQ;
        }else if(CS.PAY_WAY_CODE.UNION_BAR.equals(wayCode)){
            UnionBarOrderRQ bizRQ = JSONObject.parseObject(StringUtils.defaultIfEmpty(this.channelExtra, "{}"), UnionBarOrderRQ.class);
            BeanUtils.copyProperties(this, bizRQ);
            return bizRQ;
        }else if(CS.PAY_WAY_CODE.UNION_QR.equals(wayCode)){
            UnionQrOrderRQ bizRQ = JSONObject.parseObject(StringUtils.defaultIfEmpty(this.channelExtra, "{}"), UnionQrOrderRQ.class);
            BeanUtils.copyProperties(this, bizRQ);
            return bizRQ;
        }else if(CS.PAY_WAY_CODE.POS_BANK.equals(wayCode)){
            PosBankOrderRQ bizRQ = JSONObject.parseObject(StringUtils.defaultIfEmpty(this.channelExtra, "{}"), PosBankOrderRQ.class);
            BeanUtils.copyProperties(this, bizRQ);
            return bizRQ;
        }
        return this;
    }

    /** 获取渠道用户ID **/
    @JSONField(serialize = false)
    public String getChannelUserId(){
        return null;
    }

}
