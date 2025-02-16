package com.ylz.yx.pay.payment.rqrs;

import com.ylz.yx.pay.core.constants.CS;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/*
* 自助机订单同步请求参数对象
*/
@Data
public class SelfSynchRQ extends AbstractMchAppRQ {

    /** 系统订单号 **/
    @NotBlank(message="系统订单号不能为空")
    private String payOrderId;

    /** 商户订单号 **/
    @NotBlank(message="商户订单号不能为空")
    private String mchOrderNo;

    /** 系统参考号 **/
    @NotBlank(message="系统参考号不能为空")
    private String systemRefNum;

    /** 凭证号 **/
    @NotBlank(message="凭证号不能为空")
    private String voucherNum;

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

    /** 构造函数 **/
    public SelfSynchRQ(){
        this.setWayCode(CS.PAY_WAY_CODE.SELF_BANK);
    }
}
