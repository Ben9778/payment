package com.ylz.yx.pay.utils;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Base64Util {

	public static String encode(byte[] s) {
		if (s == null) {
			return null;
		}
		BASE64Encoder encoder = new BASE64Encoder();
		return encoder.encode(s);
	}

	public static byte[] decode(String s) {
		if (s == null) {
			return null;
		}
		BASE64Decoder decoder = new BASE64Decoder();
		try {
			return decoder.decodeBuffer(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

}