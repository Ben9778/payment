package com.ylz.yx.pay.config;

import cn.hutool.core.util.URLUtil;
import com.alibaba.fastjson.JSONObject;
import com.ylz.yx.pay.utils.PayKit;
import lombok.Data;
import org.springframework.context.annotation.Configuration;

/**
 * 公共开关，key值 属性配置
 */
@Configuration
@Data
public class ApplicationProperty {

	/** 是否校验签名 **/
	public static boolean IS_USE_CACHE = false;

	/** 是否校验地址 **/
	private Boolean isCheckUrl = false;

	/** 允许访问地址网段 **/
	private String intranetIp;

	/** 支付网关前端地址 **/
	private String paySiteFrontUrl;

	/** 支付网关后端地址 **/
	private String paySiteBackUrl;

	/** 文件存放地址 **/
	private String fileStoragePath;

	/** 商户名称 **/
	private String mchName;

	/** 通用接口地址 **/
	private String ylzCommonUrl;

	/** HIS系统厂家 **/
	private String hisManufacturer;

	/** HIS系统操作员 **/
	private String hisOperator;

	/** 是否直接获取HIS对账数据 **/
	private Boolean isHisViewData = false;

	/** 请求HIS对账数据地址 **/
	private String hisBillFileUrl;

	/** 判断HIS是否存在订单 **/
	private Boolean isHisOrder = false;

	/** 是否需要走退款审批流程 **/
	private Boolean isRefundApproval = false;

	/** 生成  【jsapi统一收银台跳转地址】 **/
	public String genUniJsapiPayUrl(String payOrderId){
		return paySiteFrontUrl + "/#/paymentH5?token=" + PayKit.aesEncode(payOrderId);
	}

	/** 生成  【jsapi统一收银台】oauth2获取用户ID回调地址 **/
	public String genOauth2RedirectUrlEncode(String payOrderId){
		return URLUtil.encodeAll(paySiteFrontUrl + "/#/oauth2Callback?token=" + PayKit.aesEncode(payOrderId));
	}

	/** 生成  【商户获取渠道用户ID接口】oauth2获取用户ID回调地址 **/
	public String genMchChannelUserIdApiOauth2RedirectUrlEncode(JSONObject param){
		return URLUtil.encodeAll(paySiteBackUrl + "/api/channelUserId/oauth2Callback/" + PayKit.aesEncode(param.toJSONString()));
	}

	/** 生成  【jsapi统一收银台二维码图片地址】 **/
	public String genScanImgUrl(String url){
		return paySiteBackUrl + "/api/scan/imgs/" + PayKit.aesEncode(url) + ".png";
	}
}