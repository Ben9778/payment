package com.ylz.yx.pay.utils;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/*
* 序列号生成 工具类
*/
public class SeqKit {

	private static final AtomicLong PAY_ORDER_SEQ = new AtomicLong(0L);
	private static final AtomicLong REFUND_ORDER_SEQ = new AtomicLong(0L);
	private static final AtomicLong MHO_ORDER_SEQ = new AtomicLong(0L);

	private static final String PAY_ORDER_SEQ_PREFIX = "P";
	private static final String REFUND_ORDER_SEQ_PREFIX = "R";
	private static final String MHO_ORDER_SEQ_PREFIX = "M";

	/** 是否使用MybatisPlus生成分布式ID **/
	private static final boolean IS_USE_MP_ID = true;

	/** 生成支付订单号 **/
	public static String genPayOrderId() {
		if(IS_USE_MP_ID) {
			return PAY_ORDER_SEQ_PREFIX + IdWorker.getIdStr();
		}
		return String.format("%s%s%04d",PAY_ORDER_SEQ_PREFIX,
				DateUtil.format(new Date(), DatePattern.PURE_DATETIME_MS_PATTERN),
				(int) PAY_ORDER_SEQ.getAndIncrement() % 10000);
	}

	/** 生成退款订单号 **/
	public static String genRefundOrderId() {
		if(IS_USE_MP_ID) {
			return REFUND_ORDER_SEQ_PREFIX + IdWorker.getIdStr();
		}
		return String.format("%s%s%04d",REFUND_ORDER_SEQ_PREFIX,
				DateUtil.format(new Date(), DatePattern.PURE_DATETIME_MS_PATTERN),
				(int) REFUND_ORDER_SEQ.getAndIncrement() % 10000);
	}

	/** 模拟生成商户订单号 **/
	public static String genMhoOrderId() {
		if(IS_USE_MP_ID) {
			return MHO_ORDER_SEQ_PREFIX + IdWorker.getIdStr();
		}
		return String.format("%s%s%04d", MHO_ORDER_SEQ_PREFIX,
				DateUtil.format(new Date(), DatePattern.PURE_DATETIME_MS_PATTERN),
				(int) MHO_ORDER_SEQ.getAndIncrement() % 10000);
	}

	public static void main(String[] args) throws Exception {
		System.out.println(genPayOrderId());
		System.out.println(PayKit.aesEncode("P1523551878851465217"));
		Thread.sleep(1000);
	}

}

