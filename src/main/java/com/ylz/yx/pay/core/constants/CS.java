package com.ylz.yx.pay.core.constants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CS {

    //登录图形验证码缓存时间，单位：s
    public static final int VERCODE_CACHE_TIME = 60;

    /** yes or no **/
    public static final String NO = "0";
    public static final String YES = "1";

    /** 通用 可用 / 禁用 **/
    public static final int PUB_USABLE = 1;
    public static final int PUB_DISABLE = 0;

    public static final Map<Integer, String> PUB_USABLE_MAP = new HashMap<>();
    static {
        PUB_USABLE_MAP.put(PUB_USABLE, "正常");
        PUB_USABLE_MAP.put(PUB_DISABLE, "停用");
    }

    //接口类型
    public interface IF_CODE{
        String ALIPAY = "alipay";   // 支付宝官方支付
        String WXPAY = "wxpay";     // 微信官方支付
        String YSFPAY = "ysfpay";   // 云闪付开放平台
        String UNIONPAY = "unionpay";   // 银联开放平台
        String POSPAY = "pospay";   // POS支付
    }


    //支付方式代码
    public interface PAY_WAY_CODE{

        // 特殊支付方式
        String QR_CASHIER = "QR_CASHIER"; //  ( 通过二维码跳转到收银台完成支付， 已集成获取用户ID的实现。  )
        String AUTO_BAR = "AUTO_BAR"; // 条码聚合支付（自动分类条码类型）

        String ALI_BAR = "ALI_BAR";  //支付宝条码支付
        String ALI_JSAPI = "ALI_JSAPI";  //支付宝服务窗支付
        String ALI_APP = "ALI_APP";  //支付宝 app支付
        String ALI_PC = "ALI_PC";  //支付宝 电脑网站支付
        String ALI_WAP = "ALI_WAP";  //支付宝 wap支付
        String ALI_QR = "ALI_QR";  //支付宝 二维码付款
        String ALI_FACE = "ALI_FACE";  //支付宝刷脸支付

        String YSF_BAR = "YSF_BAR";  //云闪付条码支付
        String YSF_JSAPI = "YSF_JSAPI";  //云闪付服务窗支付

        String UNION_BAR = "UNION_BAR";  //银联条码支付
        String UNION_QR = "UNION_QR";  //银联二维码付款

        String POS_BANK = "POS_BANK"; //POS银行卡
        String SELF_BANK = "SELF_BANK"; //自助机银行卡

        String WX_APP = "WX_APP";  //微信app支付
        String WX_FACE = "WX_FACE";  //支付宝刷脸支付
        String WX_JSAPI = "WX_JSAPI";  //微信jsapi支付
        String WX_LITE = "WX_LITE";  //微信小程序支付
        String WX_BAR = "WX_BAR";  //微信条码支付
        String WX_H5 = "WX_H5";  //微信H5支付
        String WX_NATIVE = "WX_NATIVE";  //微信扫码支付


    }

    //支付数据包 类型
    public interface PAY_DATA_TYPE {
        String PAY_URL = "payurl";  //跳转链接的方式  redirectUrl
        String FORM = "form";  //表单提交
        String WX_APP = "wxapp";  //微信app参数
        String ALI_APP = "aliapp";  //支付宝app参数
        String YSF_APP = "ysfapp";  //云闪付app参数
        String CODE_URL = "codeUrl";  //二维码URL
        String CODE_IMG_URL = "codeImgUrl";  //二维码图片显示URL
        String NONE = "none";  //无参数
//        String QR_CONTENT = "qrContent";  //二维码实际内容
    }


    //接口版本
    public interface PAY_IF_VERSION{
        String WX_V2 = "V2";  //微信接口版本V2
        String WX_V3 = "V3";  //微信接口版本V3
    }
}
