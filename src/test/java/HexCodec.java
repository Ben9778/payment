public final class HexCodec {

	static final char[] HEX = "0123456789ABCDEF".toCharArray();

    private HexCodec(){}

    /**
     * 二进制转换成十六进制
     * @param buffer
     * @param start
     * @param length
     * @return
     */
	public static String hexEncode(byte[] buffer, int start, int length) {
		if (null == buffer || length < start || length > buffer.length) {
			return "";
		}
		
		if (buffer.length == 0) {
			return "";
		}
		
		int holder = 0;
		char[] chars = new char[length * 2];
        int pos = -1;
		for (int i = start; i < start + length; i++) {
			holder = (buffer[i] & 0xf0) >> 4;
			chars[++pos * 2] = HEX[holder];
			holder = buffer[i] & 0x0f;
			chars[(pos * 2) + 1] = HEX[holder];
		}
		return new String(chars);
	}
	
	
	/**
     * 二进制转换成十六进制字符串
     * @param buffer
     * @param start
     * @param length
     * @return
     */
	public static String hexEncode(byte[] buffer) {
		return hexEncode(buffer, 0, buffer.length);
	}

	public static byte[] hexDecode(String hex) {
		//A null string returns an empty array
		if (hex == null || hex.length() == 0) {
			return new byte[0];
		} else if (hex.length() < 3) {
			return new byte[]{ (byte)(Integer.parseInt(hex, 16) & 0xff) };
		}
		//Adjust accordingly for odd-length strings
		int count = hex.length();
		int nibble = 0;
		if (count % 2 != 0) {
			count++;
			nibble = 1;
		}
		byte[] buf = new byte[count / 2];
		char c = 0;
		int holder = 0;
		int pos = 0;
		for (int i = 0; i < buf.length; i++) {
		    for (int z = 0; z < 2 && pos < hex.length(); z++) {
		        c = hex.charAt(pos++);
		        if (c >= 'A' && c <= 'F') {
		            c -= 55;
		        } else if (c >= '0' && c <= '9') {
		            c -= 48;
		        } else if (c >= 'a' && c <= 'f') {
		            c -= 87;
		        }
		        if (nibble == 0) {
		            holder = c << 4;
		        } else {
		            holder |= c;
		            buf[i] = (byte)holder;
		        }
		        nibble = 1 - nibble;
		    }
		}
		return buf;
	}
	/**
	 * 十六进制字符串高低位转换
	 * @param hexStr 十六进制字符串
	 * @return
	 * @throws Exception
	 */
	public static String hexReverse(String hexStr) {
		byte[] bb = HexStrToByteArray(hexStr);
		byte[] bt = new byte[bb.length];
		int j = 0;
		for (int i = bb.length-1; i >= 0; i--) {
			bt[j] = bb[i];
			j++;
		}
		return HexCodec.hexEncode(bt);
	}
	
	/**
	 * 将十六进制格式字符串转化为二进制数组，“3F”=〉0x3F
	 * 
	 * @param source
	 * @return
	 * @throws Exception
	 */
	public static byte[] HexStrToByteArray(String source)  {
		if (source.length() % 2 == 1)
			source = "0" + source;
		byte[] result = new byte[source.length() / 2];
		String tmp = null;
		for (int i = 0; i < source.length(); i = i + 2) {
			tmp = source.substring(i, i + 1);
			if (!isHexChar(tmp))
				System.out.println("字符串中含有非法字符\"" + source.substring(i, i + 1)
						+ "\"");
			result[i / 2] = (byte) (getIntValueFromHexChar(tmp) * 16);
			tmp = source.substring(i + 1, i + 2);
			if (!isHexChar(tmp))
				System.out.println("字符串中含有非法字符\"" + source.substring(i, i + 1)
						+ "\"");
			result[i / 2] += (byte) getIntValueFromHexChar(tmp);
		}
		return result;
	}
	
	/**
	 * 
	 * @param source
	 * @return
	 */
	private static int getIntValueFromHexChar(String source) {
		int result = 0;
		if (source.getBytes()[0] >= 0x30 && source.getBytes()[0] <= 0x39) {
			result = source.getBytes()[0] - 0x30;
		} else if (source.getBytes()[0] >= 0x61 && source.getBytes()[0] <= 0x66) {
			result = 10 + source.getBytes()[0] - 0x61;
		} else if (source.getBytes()[0] >= 0x41 && source.getBytes()[0] <= 0x46) {
			result = 10 + source.getBytes()[0] - 0x41;
		}
		// System.out.println(result);
		return result;
	}
	
	/**
	 * 判断是否是16进制字符串
	 * 
	 * @param source
	 * @return
	 */
	private static boolean isHexChar(String source) {
		if (source == null || source.length() != 1)
			return false;
		if ((source.getBytes()[0] >= 0x30 && source.getBytes()[0] <= 0x39)
				|| (source.getBytes()[0] >= 0x61 && source.getBytes()[0] <= 0x66)
				|| (source.getBytes()[0] >= 0x41 && source.getBytes()[0] <= 0x46))
			return true;
		else
			return false;
	}
}