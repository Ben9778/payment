package com.ylz.yx.pay.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @version 1.00.00
 * @description: DES加密工具类
 * @history:
 */
public class DesString {

	private static Log log = LogFactory.getLog(DesString.class);

	public DES getDes(String strKey) {
		DES des = null;
		try {
			des = new DES(strKey);
		} catch (Exception e) {
			log.error("创建des错误" + e);
		}
		return des;
	}

	/**
	 * 加密
	 * 
	 * @param te
	 *            （明文）
	 * @return 密文
	 */
	public String encrypt(String te, String strKey) {
		return getDes(strKey).encrypt(te);
	}

	/**
	 * 解密
	 * 
	 * @param te
	 *            密文
	 * @return 明文
	 */
	public String decrypt(String te, String strKey) {
		return getDes(strKey).decrypt(te);
	}
	
	public static void main(String[] args) throws Exception {
		DesString ds = new DesString();
		System.out.println(ds.encrypt("133340008744407636000000","1200"));
		System.out.println(ds.decrypt("E2863DBF0DB200CD847AA5D5E79C994603BFEED481E8AB13","1000"));
	}

}
