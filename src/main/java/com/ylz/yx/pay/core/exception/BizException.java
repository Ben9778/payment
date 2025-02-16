package com.ylz.yx.pay.core.exception;

import com.ylz.yx.pay.core.model.ApiRes;
import com.ylz.yx.pay.core.constants.ApiCodeEnum;
import lombok.Getter;

/*
* 自定义业务异常
*/
@Getter
public class BizException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	private final ApiRes apiRes;

	/** 业务自定义异常 **/
	public BizException(String msg) {
		super(msg);
		this.apiRes = ApiRes.customFail(msg);
	}

	public BizException(ApiCodeEnum apiCodeEnum, String... params) {
		super();
		apiRes = ApiRes.fail(apiCodeEnum, params);
	}

	public BizException(ApiRes apiRes) {
		super(apiRes.getMsg());
		this.apiRes = apiRes;
	}
}
