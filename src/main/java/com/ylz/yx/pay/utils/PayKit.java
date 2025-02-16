package com.ylz.yx.pay.utils;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.SecureUtil;
import com.alibaba.fastjson.JSONObject;
import com.ylz.core.logging.Logger;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.exception.BizException;
import liquibase.pro.packaged.J;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.regex.Pattern;

/*
* pay工具类
*/
public class PayKit {

    private static final Logger log = new Logger(PayKit.class.getName());

    public static String AES_KEY = "46b5a3a4c0e201b4bc7f4c6dd4e6f0d1";

    /** 加密 **/
    public static String aesEncode(String str){
        return SecureUtil.aes(HexUtil.decodeHex(PayKit.AES_KEY)).encryptHex(str);
    }
    //解密
    public static String aesDecode(String str){
        return SecureUtil.aes(HexUtil.decodeHex(PayKit.AES_KEY)).decryptStr(str);
    }

    private static final String encodingCharset = "UTF-8";

    /**
     * 计算签名摘要
     * @param map 参数Map
     * @param key 商户秘钥
     * @return
     */
    public static String getSign(Map<String,Object> map, String key){
        ArrayList<String> list = new ArrayList<String>();
        for(Map.Entry<String,Object> entry:map.entrySet()){
            if(null != entry.getValue() && !"".equals(entry.getValue())){
                list.add(entry.getKey() + "=" + entry.getValue() + "&");
            }
        }
        int size = list.size();
        String [] arrayToSort = list.toArray(new String[size]);
        Arrays.sort(arrayToSort, String.CASE_INSENSITIVE_ORDER);
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < size; i ++) {
            sb.append(arrayToSort[i]);
        }
        String result = sb.toString();
        result += "key=" + key;
        log.info("signStr:" + result);
        result = md5(result, encodingCharset).toUpperCase();
        log.info("sign:" + result);
        return result;
    }


    /**
     * MD5
     * @param value
     * @param charset
     * @return
     */
    public static String md5(String value, String charset) {
        MessageDigest md = null;
        try {
            byte[] data = value.getBytes(charset);
            md = MessageDigest.getInstance("MD5");
            byte[] digestData = md.digest(data);
            return toHex(digestData);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String toHex(byte[] input) {
        if (input == null) {
            return null;
        }
        StringBuffer output = new StringBuffer(input.length * 2);
        for (int i = 0; i < input.length; i++) {
            int current = input[i] & 0xff;
            if (current < 16) {
                output.append("0");
            }
            output.append(Integer.toString(current, 16));
        }

        return output.toString();
    }

    /** map 转换为  url参数 **/
    public static String genUrlParams(Map<String, Object> paraMap) {
        if(paraMap == null || paraMap.isEmpty()) {
            return "";
        }
        StringBuffer urlParam = new StringBuffer();
        Set<String> keySet = paraMap.keySet();
        int i = 0;
        for(String key:keySet) {
            urlParam.append(key).append("=").append( paraMap.get(key) == null ? "" : doEncode(paraMap.get(key).toString()) );
            if(++i == keySet.size()) {
                break;
            }
            urlParam.append("&");
        }
        return urlParam.toString();
    }

    static String doEncode(String str) {
        if(str.contains("+")) {
            return URLEncoder.encode(str);
        }
        return str;
    }

    /** 校验微信/支付宝二维码是否符合规范， 并根据支付类型返回对应的支付方式  **/
    public static String getPayWayCodeByBarCode(String barCode){

        if(StringUtils.isEmpty(barCode)) {
            throw new BizException("条码为空");
        }

        //微信 ： 用户付款码条形码规则：18位纯数字，以10、11、12、13、14、15开头
        //文档： https://pay.weixin.qq.com/wiki/doc/api/micropay.php?chapter=5_1
        if(barCode.length() == 18 && Pattern.matches("^(10|11|12|13|14|15)(.*)", barCode)){
            return CS.PAY_WAY_CODE.WX_BAR;
        }
        //支付宝： 25~30开头的长度为16~24位的数字
        //文档： https://docs.open.alipay.com/api_1/alipay.trade.pay/
        else if(barCode.length() >= 16 && barCode.length() <= 24 && Pattern.matches("^(25|26|27|28|29|30)(.*)", barCode)){
            return CS.PAY_WAY_CODE.ALI_BAR;
        }
        //银联： 二维码标准： 19位 + 62开头
        //文档：https://wenku.baidu.com/view/b2eddcd09a89680203d8ce2f0066f5335a8167fa.html
        else if(barCode.length() == 19 && Pattern.matches("^(62)(.*)", barCode)){
            return CS.PAY_WAY_CODE.UNION_BAR;
        }
        else{  //暂时不支持的条码类型
            throw new BizException("不支持的条码");
        }
    }

    public static String getIpAddr(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            if (ip.indexOf(",") != -1) {
                ip = ip.split(",")[0];
            }
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public static void main(String[] args) throws Exception {
        String ele = "898340280620111|20220701|20220701|084325|2192950|0.01|0.01|0|000026|消费|621226****8071|000427836586||借记卡 ";
        String[] line = ele.split("\\|", -1);
        //System.out.println(line[0]);

        String result = "amount=100&appId=BE2D29D55A97432F8507512473F7950B&cardNo=20220916&idCard=371311199011233119&mchOrderNo=ZZJ02020220919113001&operatorId=ZZJ020&operatorName=ZZJ020&orderType=01&payOrderId=ZZJ02020220919113001&phone=15618725123&reqTime=637991844788382899&signType=MD5&subject=自助机银行卡交易&systemRefNum=123456789&userName=何东武&version=1.0&voucherNum=123456789&key=E2A7368D17C64DD0A3682ACAB48E64EF8F8242CC8BEA4471BFE0AD800E92AD72B684F92940B04729B4D9E2AD8801B538B3EE1AB818BF4BAD9A5B56A2C5BAD7B7";
        log.info("signStr:" + result);
        result = md5(result, encodingCharset).toUpperCase();
        log.info("sign:" + result);

        Map map = new HashMap<>();
        map.put("transCode","orderRefund");
        map.put("serviceVersion","1.0");
        map.put("charset","UTF-8");
        map.put("chnlId","19022020");
        map.put("reqTime","2022-11-17 17:28:32.236");
        map.put("transSeqNo","1652061281");
        map.put("deviceNo","DY000001");
        map.put("devideType","01");
        map.put("spbillCreateIp","127.0.0.1");
        map.put("area","1");
        map.put("tmnNum","DY000001");

        map.put("merNo","2100810004311");
        map.put("operaNo","1");
        map.put("authOperaNo","1");
        map.put("oriOrderId","QP22111711886813972642816");
        map.put("refundAmt","0.01");
        map.put("merchOrderId","13524125214215236336");
        map.put("refundReason","1");
        map.put("loginName","1");



       /* map.put("orderRemark","1");
        map.put("ccy","CNY");
        map.put("orderAmt","0.01");
        map.put("authCode","132469146834257266");
        map.put("cashierId","2100800100059");
        map.put("reqTime","2022-11-17 10:58:32.236");
        map.put("transSeqNo","1652061280");
        map.put("deviceNo","1");
        map.put("devideType","1");
        map.put("spbillCreateIp","1");
        map.put("area","1");
        map.put("tmnNum","1");
        map.put("loginName","1");
        map.put("appVersion","1.0");*/
        System.out.println(getSign(map,"A7DF7EECD4254FDC8CCD5AE31C79E3D841C9CA7F2A8C4EB6AADFA00A720EAF52A294FDF558F243C7B81D8C8BBBE882025F384C5A83024D1D8C196CEB6028BA1C"));
        String total_amount = "1158.62";

//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("msg","未找到有效的数据!XTGZH0=28389 and BRID00=10039436");
//        if(jsonObject.getString("msg").contains("未找到有效的数据!")){
//            System.out.println(line[0]);
//        }
        //System.out.println(BigDecimal.valueOf(Double.valueOf(total_amount)).multiply(new BigDecimal(100)).longValue());
        //System.out.println(Double.valueOf(Double.valueOf(total_amount) * 100).longValue());
        //Map<String, String> map = new HashMap<>();
        //System.out.println(map.size());

        /*Map map = new HashMap<>();
        map.put("appId","76B6783E1B9746989C1F9611B720CD5B");
        map.put("mchOrderNo","Q12312312312317");
        map.put("wayCode","AUTO_BAR");
        map.put("amount","1");
        map.put("subject","预交金充值");
        map.put("channelExtra","{\"auth_code\":\"135063526553351449\",\"clientIp\":\"192.168.1.46\"}");
        map.put("userName","张三");
        map.put("phone","1");
        map.put("idCard","1");
        map.put("cardNo","1");
        map.put("orderType","01");
        map.put("operatorId","1201");
        map.put("operatorName","管理员");
        map.put("extParam","");
        map.put("reqTime","1652061280");
        map.put("version","1.0");
        map.put("signType","MD5");
        System.out.println(getSign(map,"A7DF7EECD4254FDC8CCD5AE31C79E3D841C9CA7F2A8C4EB6AADFA00A720EAF52A294FDF558F243C7B81D8C8BBBE882025F384C5A83024D1D8C196CEB6028BA1C"));
*/
        /*Map<String, Object> signMap = new HashMap<>();
        signMap.put("appId","1F4BCDF74F514A9F856DB16FDC44CBB6");
        signMap.put("mchOrderNo","Q12312312312317");
        signMap.put("wayCode","ALI_BAR");
        signMap.put("amount","1");
        signMap.put("subject","预交金充值");
        signMap.put("channelExtra","{\"authCode\":\"280812820366966512\"}");
        signMap.put("userName","患者01");
        signMap.put("phone","1");
        signMap.put("idCard","1");
        signMap.put("cardNo","123");
        signMap.put("orderType","01");
        signMap.put("operatorId","0024");
        signMap.put("operatorName","操作员01");
        signMap.put("extParam","");
        signMap.put("reqTime","20190723141000");
        signMap.put("version","1.0");
        signMap.put("signType","MD5");
        System.out.println(getSign(signMap,"0E67CF5D6CF04DBAA3B940531DD655F858ECD89BAEFE498BBF3619F33C148E734955B84C3A0749558FC206C8EBA75C9C8FA4D186B8034C86B2CB704638DDD2EC"));
        */
        /*String str = "009088622848*********8812 003420000000000001交易成功                                123456789900089123450890003101230102801000120402332      123000000000000000001                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      ";
        int pos = 0;
        System.out.println(getFromCompressedUnicode(str, pos,2));
        pos += 2;
        System.out.println(getFromCompressedUnicode(str, pos,4));
        pos += 4;
        System.out.println(getFromCompressedUnicode(str, pos,20));
        pos += 20;
        System.out.println(getFromCompressedUnicode(str, pos,6));
        pos += 6;
        System.out.println(getFromCompressedUnicode(str, pos,12));
        pos += 12;
        System.out.println(getFromCompressedUnicode(str, pos,40));
        pos += 40;
        System.out.println(getFromCompressedUnicode(str, pos,15));
        pos += 15;
        System.out.println(getFromCompressedUnicode(str, pos,8));
        pos += 8;
        System.out.println(getFromCompressedUnicode(str, pos,6));
        pos += 6;
        System.out.println(getFromCompressedUnicode(str, pos,4));
        pos += 4;
        System.out.println(getFromCompressedUnicode(str, pos,6));
        pos += 6;
        System.out.println(getFromCompressedUnicode(str, pos,12));
        pos += 12;
        System.out.println(getFromCompressedUnicode(str, pos,6));
        pos += 6;
        System.out.println(getFromCompressedUnicode(str, pos,4));
        pos += 4;
        System.out.println(getFromCompressedUnicode(str, pos,3));
        pos += 3;
        System.out.println(getFromCompressedUnicode(str, pos,12));
        pos += 12;
        System.out.println(getFromCompressedUnicode(str, pos,2));
        pos += 2;
        System.out.println(getFromCompressedUnicode(str, pos,200));
        pos += 200;
        System.out.println(getFromCompressedUnicode(str, pos,50));
        pos += 50;
        System.out.println(getFromCompressedUnicode(str, pos,50));
        pos += 50;
        System.out.println(getFromCompressedUnicode(str, pos,50));
        pos += 50;
        System.out.println(getFromCompressedUnicode(str, pos,1));
        pos += 1;
        System.out.println(getFromCompressedUnicode(str, pos,1));
        pos += 1;
        System.out.println(getFromCompressedUnicode(str, pos,50));
        pos += 50;
        System.out.println(getFromCompressedUnicode(str, pos,200));
        pos += 200;
        System.out.println(getFromCompressedUnicode(str, pos,8));
        pos += 8;
        System.out.println(getFromCompressedUnicode(str, pos,20));*/
        //appId=76B6783E1B9746989C1F9611B720CD5B&mchOrderNo=Q12312312312316&payOrderId=P1534712277916700674&reqTime=1652061280&signType=MD5&version=1.0
        /*List<String> fileList = FileUtils.getFiles("F:\\tmp-files\\needDownLoadFolder");
        for (String file : fileList) {
            System.out.println(file);
            // 读取⽂件内容到Stream流中，按⾏读取
            Stream<String> lines = Files.lines(Paths.get(file));
            // 随机⾏顺序进⾏数据处理
            lines.forEach(ele -> {
                System.out.println(ele);
            });
        }*/

        /*String billDate = "5笔待处理\n" +
                "6笔待处理\n" +
                "已处理";
        int clbs00 = 0;
        String[] bill = billDate.split("\n");
        for(int i = 0;i < bill.length; i++){
            String a = bill[i];
            System.out.println(a.contains("待处理"));
            if(a.contains("待处理")){
                clbs00 = clbs00 + Integer.valueOf(a.substring(0,a.indexOf("笔")));
            }
        }
        StringBuffer sb = new StringBuffer();
        if(clbs00 > 0){
            sb.append(clbs00 + "笔待处理\n");
        } else {
            sb.append("已处理\n");
        }

        System.out.println(sb);*/

        /*String uurl = "ftp://10.16.3.218:21/test";
        String url = uurl.replace("ftp://","");
        String ip = url.split(":")[0];
        String port = url.split(":")[1].split("/")[0];
        String dir = "/";
        if(url.split(":")[1].split("/").length > 1){
            dir = url.split(":")[1].split("/")[1];
        }

        // 帐号密码连接FTP服务器
        Ftp ftp = new Ftp(ip, Integer.valueOf(port), "ftptest", "ftptest", CharsetUtil.CHARSET_GBK);
        // 连接策略(Passive:模式被动;Active:主动模式 )
        ftp.setMode(FtpMode.Passive);
        // 进入FTP跟目录
//        ftp.cd(dir);
        // 下载远程文件(下载指定目录，到指定目录)
        ftp.download(dir,"20220429.txt", new File("F:\\tmp-files\\needDownLoadFolder\\20220429.txt"));
        // 下载远程文件(下载指定文件，到指定路径的指定文件)
        //ftp.recursiveDownloadFolder("/needDownLoadFolder/666.jpg", new File("E:\\tmp-files\\needDownLoadFolder\\777.jpg"));
        // 关闭连接
        ftp.close();*/

    }

    /**
     *string:字符串
     offset：从哪个字节开始
     len：从哪个字节结束
     */
    public static String getFromCompressedUnicode(String string,int offset,int len) throws UnsupportedEncodingException {
        byte[] bytes = string.getBytes("GBK");
        int len_to_use = Math.min(len,bytes.length - offset);
        System.out.println(offset+"------------------"+len_to_use);
        return new String(bytes,offset,len_to_use,"GBK");
    }
}
