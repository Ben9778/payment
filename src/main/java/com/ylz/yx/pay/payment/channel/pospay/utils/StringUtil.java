package com.ylz.yx.pay.payment.channel.pospay.utils;

import java.io.UnsupportedEncodingException;

public class StringUtil {
    /**
     *string:字符串
     offset：从哪个字节开始
     len：从哪个字节结束
     */
    public static String getFromCompressedUnicode(String string,int offset,int len)  {
        try {
            byte[] bytes = string.getBytes("GBK");
            int len_to_use = Math.min(len,bytes.length - offset);
            System.out.println(offset+"------------------"+len_to_use);
            return new String(bytes,offset,len_to_use,"GBK");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
