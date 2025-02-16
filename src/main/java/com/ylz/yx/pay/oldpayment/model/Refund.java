package com.ylz.yx.pay.oldpayment.model;

import java.io.Serializable;

/**
 * 支付宝退款参数实体类
 * @author wsk
 *
 */
public class Refund implements Serializable{
	private static final long serialVersionUID = 1L;
	/**** 退款信息 ****/
	/** 商户订单号,不能和 trade_no同时为空。*/
	private String id;//商户订单号(out_trade_no,orderId)
	private String system_id;//关联系统流水号
	private String channel_id;//支付宝交易号(trade_no,origQryId)，和商户订单号不能同时为空 
	private String out_request_no;//标识一次退款请求，同一笔交易多次退款需要保证唯一，如需部分退款，则此参数必传。
	private Double amount;//总金额
	private Double refund_free;//必选 需要退款的金额(refund_amount,txnAmt)，该金额不能大于订单金额,单位为元，支持两位小数
	private String order_name;//原订单名称
	private String refund_reason;//退款的原因说明
	private Integer channel_type;//退款渠道 1现金  2支付宝 3微信 4银联 5POS
	private Long p_id;//病人id
	private String p_name;//病人姓名
	private String id_card;
	private String card_no;
	private String card_type;
	private String account_type;
	private String userid;//系统id
	private String operator_id;//操作员id
	private String operator_name;//操作员姓名
	
	public Double getRefund_free() {
		return refund_free;
	}
	public void setRefund_free(Double refund_free) {
		this.refund_free = refund_free;
	}
	public String getSystem_id() {
		return system_id;
	}
	public void setSystem_id(String system_id) {
		this.system_id = system_id;
	}
	public String getUserid() {
		return userid;
	}
	public void setUserid(String userid) {
		this.userid = userid;
	}
	public String getOrder_name() {
		return order_name;
	}
	public void setOrder_name(String order_name) {
		this.order_name = order_name;
	}
	public String getId_card() {
		return id_card;
	}
	public void setId_card(String id_card) {
		this.id_card = id_card;
	}
	public Long getP_id() {
		return p_id;
	}
	public void setP_id(Long p_id) {
		this.p_id = p_id;
	}
	public String getP_name() {
		return p_name;
	}
	public void setP_name(String p_name) {
		this.p_name = p_name;
	}
	public String getCard_no() {
		return card_no;
	}
	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}
	public String getCard_type() {
		return card_type;
	}
	public void setCard_type(String card_type) {
		this.card_type = card_type;
	}
	public String getAccount_type() {
		return account_type;
	}
	public void setAccount_type(String account_type) {
		this.account_type = account_type;
	}
	/**** 退款信息 ****/
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getChannel_id() {
		return channel_id;
	}
	public void setChannel_id(String channel_id) {
		this.channel_id = channel_id;
	}
	public Double getAmount() {
		return amount;
	}
	public void setAmount(Double amount) {
		this.amount = amount;
	}
	public String getRefund_reason() {
		return refund_reason;
	}
	public void setRefund_reason(String refund_reason) {
		this.refund_reason = refund_reason;
	}
	public String getOut_request_no() {
		return out_request_no;
	}
	public void setOut_request_no(String out_request_no) {
		this.out_request_no = out_request_no;
	}
	public Integer getChannel_type() {
		return channel_type;
	}
	public void setChannel_type(Integer channel_type) {
		this.channel_type = channel_type;
	}
	public String getOperator_id() {
		return operator_id;
	}
	public void setOperator_id(String operator_id) {
		this.operator_id = operator_id;
	}
	public String getOperator_name() {
		return operator_name;
	}
	public void setOperator_name(String operator_name) {
		this.operator_name = operator_name;
	}
	
}
