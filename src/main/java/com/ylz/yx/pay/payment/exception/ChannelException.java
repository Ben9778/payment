package com.ylz.yx.pay.payment.exception;

import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import lombok.Getter;

/*
* 请求渠道侧异常 exception
* 抛出此异常： 仅支持：  未知状态（需查单） 和 系统内异常
*/
@Getter
public class ChannelException extends RuntimeException{

	private static final long serialVersionUID = 1L;

	private final ChannelRetMsg channelRetMsg;

	/** 业务自定义异常 **/
	private ChannelException(ChannelRetMsg channelRetMsg) {
		super(channelRetMsg != null ? channelRetMsg.getChannelErrMsg() : null);
		this.channelRetMsg = channelRetMsg;
	}

	/** 未知状态 **/
	public static ChannelException unknown(String channelErrMsg){
		return new ChannelException(ChannelRetMsg.unknown(channelErrMsg));
	}

	/** 系统内异常 **/
	public static ChannelException sysError(String channelErrMsg){
		return new ChannelException(ChannelRetMsg.sysError(channelErrMsg));
	}


}
