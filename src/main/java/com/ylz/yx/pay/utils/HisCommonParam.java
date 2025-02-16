package com.ylz.yx.pay.utils;

import cn.hutool.core.date.DateUtil;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HisCommonParam {

    // 门诊预交金交易查询（SP_SF_TYJK_YJJ_JYCX）
    public static Map SP_SF_TYJK_YJJ_JYCX(String czy000, String cardno) {
        Map param = new HashMap();
        StringBuilder original_payload = new StringBuilder();
        original_payload.append("<request>")
                .append("<funcname>SP_SF_TYJK_YJJ_JYCX</funcname>")
                .append("<hiskey></hiskey>")
                .append("<cjlybm>YX</cjlybm>")
                .append("<qdcjbm>99</qdcjbm>")
                .append("<zdbh00></zdbh00>")
                .append("<czy000>" + czy000 + "</czy000>")
                .append("<cardno>" + cardno + "</cardno>")
                .append("<brid00>0</brid00>")
                .append("<startdate></startdate>")
                .append("<enddate></enddate>")
                .append("<zffsbh>24|25</zffsbh>")
                .append("</request>");
        param.put("original_payload", original_payload.toString());
        return param;
    }

    // 住院预交金交易查询（SP_ZY_TYJK_YJJ_JYCX）

    // 查询住院预交金（SP_TYJK_ZYYJJCX）
    public static Map SP_TYJK_ZYYJJCX(String czy000, String zyh000) {
        Map param = new HashMap();
        StringBuilder original_payload = new StringBuilder();
        original_payload.append("<request>")
                .append("<funcname>SP_TYJK_ZYYJJCX</funcname>")
                .append("<hiskey></hiskey>")
                .append("<cjlybm>YX</cjlybm>")
                .append("<qdcjbm>99</qdcjbm>")
                .append("<zdbh00></zdbh00>")
                .append("<czy000>" + czy000 + "</czy000>")
                .append("<zyh000>" + zyh000 + "</zyh000>")
                .append("<ksrq00>" + DateUtil.format(new Date(), "yyyyMMdd") + "</ksrq00>")
                .append("<jsrq00>" + DateUtil.format(new Date(), "yyyyMMdd") + "</jsrq00>")
                .append("<brxm00></brxm00>")
                .append("</request>");
        param.put("original_payload", original_payload.toString());
        return param;
    }


    // 门诊或住院支付交易明细（对账）（SP_TYJK_PAYMENT_DETAILS）
    public static Map SP_TYJK_PAYMENT_DETAILS(String czy000, String zyh000) {
        Map param = new HashMap();
        StringBuilder original_payload = new StringBuilder();
        original_payload.append("<request>")
                .append("<funcname>SP_TYJK_PAYMENT_DETAILS</funcname>")
                .append("<hiskey></hiskey>")
                .append("<cjlybm>YX</cjlybm>")
                .append("<qdcjbm>99</qdcjbm>")
                .append("<zdbh00></zdbh00>")
                .append("<czy000>" + czy000 + "</czy000>")
                .append("<cardno>" + zyh000 + "</cardno>")
                .append("<brid00>0</brid00>")
                .append("<mzzybz>1</mzzybz>")
                .append("<startdate></startdate>")
                .append("<enddate></enddate>")
                .append("<zffsbh>24|25</zffsbh>")
                .append("</request>");
        param.put("original_payload", original_payload.toString());
        return param;
    }

    // 门诊获取病人基本信息 SP_SF_TYJK_GETBRJBXX
    public static Map SP_SF_TYJK_GETBRJBXX(String brid00) {
        Map param = new HashMap();
        StringBuilder original_payload = new StringBuilder();
        original_payload.append("<request>")
                .append("<funcname>SP_SF_TYJK_GETBRJBXX</funcname>")
                .append("<cardno></cardno>")
                .append("<brid00>" + brid00 + "</brid00>")
                .append("<brxm00></brxm00>")
                .append("<brzjbh></brzjbh>")
                .append("<brlxdh></brlxdh>")
                .append("</request>");
        param.put("original_payload", original_payload.toString());
        return param;
    }

    // 住院获取病人基本信息
    public static Map SP_ZY_TYJK_GETBRJBXX(String zyid00) {
        Map param = new HashMap();
        StringBuilder original_payload = new StringBuilder();
        original_payload.append("<request>")
                .append("<funcname>SP_ZY_TYJK_GETBRJBXX</funcname>")
                .append("<zyh000></zyh000>")
                .append("<zyid00>" + zyid00 + "</zyid00>")
                .append("<brxm00></brxm00>")
                .append("<brzjlx></brzjlx>")
                .append("<brzjbh></brzjbh>")
                .append("<brlxdh></brlxdh>")
                .append("</request>");
        param.put("original_payload", original_payload.toString());
        return param;
    }

    // 门诊线上获取待结算处方明细 SP_SF_TYJK_ONLINE_JSMX
    public static Map SP_SF_TYJK_ONLINE_JSMX(String czy000, String cardno, String brid00, String ghh000) {
        Map param = new HashMap();
        StringBuilder original_payload = new StringBuilder();
        original_payload.append("<request>")
                .append("<funcname>SP_SF_TYJK_ONLINE_JSMX</funcname>")
                .append("<hiskey></hiskey>")
                .append("<cjlybm>YX</cjlybm>")
                .append("<qdcjbm>99</qdcjbm>")
                .append("<zdbh00></zdbh00>")
                .append("<czy000>" + czy000 + "</czy000>")
                .append("<cardno>" + cardno + "</cardno>")
                .append("<brid00>" + brid00 + "</brid00>")
                .append("<jzdh00>0</jzdh00>")
                .append("<ksrq00></ksrq00>")
                .append("<jsrq00></jsrq00>")
                .append("<yyid00></yyid00>")
                .append("<ghh000>" + ghh000 + "</ghh000>")
                .append("<ybbz00>2</ybbz00>")
                .append("</request>");
        param.put("original_payload", original_payload.toString());
        return param;
    }

    // 门诊线上结算确认 SP_SF_TYJK_ONLINE_JSQR
    public static Map SP_SF_TYJK_ONLINE_JSQR(String czy000, String kh0000, String brid00, String ghh000, String xtgzh0, String czje00, String zfqd00, String jylsh0) {
        Map param = new HashMap();
        String zfmxbh = null;// 支付明细编码
        switch (zfqd00) {
            case "alipay":
                zfmxbh = "G";
                break;
            case "wxpay":
                zfmxbh = "E";
                break;
            case "unionpay":
                zfmxbh = "V";
                break;
            case "pospay":
                zfmxbh = "K";
                break;
            default:
                break;
        }
        StringBuilder original_payload = new StringBuilder();
        original_payload.append("<request>")
                .append("<funcname>SP_SF_TYJK_ONLINE_JSQR</funcname>")
                .append("<hiskey></hiskey>")
                .append("<cjlybm>YX</cjlybm>")
                .append("<qdcjbm>99</qdcjbm>")
                .append("<zdbh00></zdbh00>")
                .append("<czy000>" + czy000 + "</czy000>")
                .append("<cardno>" + kh0000 + "</cardno>")
                .append("<brid00>" + brid00 + "</brid00>")
                .append("<jzdh00>0</jzdh00>")
                .append("<ksrq00></ksrq00>")
                .append("<jsrq00></jsrq00>")
                .append("<yyid00></yyid00>")
                .append("<ghh000>" + ghh000 + "</ghh000>")
                .append("<ybbz00></ybbz00>")
                .append("<xtgzh0>" + xtgzh0 + "</xtgzh0>")
                .append("<jsqrtype>1</jsqrtype>")
                .append("<ybjszt>M</ybjszt>")
                .append("<payinfo>")
                .append("<czje00>" + czje00 + "</czje00>")
                .append("<zfmxbh>" + zfmxbh + "</zfmxbh>")
                .append("<jylsh0>" + jylsh0 + "</jylsh0>")
                .append("<qdlsh0></qdlsh0>")
                .append("</payinfo>")
                .append("</request>");
        param.put("original_payload", original_payload.toString());
        return param;
    }

    // 门诊通用接口结算结果查询 SP_SF_TYJK_JSJGCX
    public static Map SP_SF_TYJK_JSJGCX(String czy000, String kh0000, String xtgzh0) {
        Map param = new HashMap();
        StringBuilder original_payload = new StringBuilder();
        original_payload.append("<request>")
                .append("<funcname>SP_SF_TYJK_JSJGCX</funcname>")
                .append("<hiskey></hiskey>")
                .append("<cjlybm>YX</cjlybm>")
                .append("<qdcjbm>99</qdcjbm>")
                .append("<zdbh00></zdbh00>")
                .append("<czy000>" + czy000 + "</czy000>")
                .append("<cardno>" + kh0000 + "</cardno>")
                .append("<jzdh00></jzdh00>")
                .append("<xtgzh0>" + xtgzh0 + "</xtgzh0>")
                .append("</request>");
        param.put("original_payload", original_payload.toString());
        return param;
    }

    // 门诊通用接口预交金账户充值
    public static Map SP_SF_TYJK_YJJCZ(String brid00, String brxm00, String czy000, String czje00, String zfqd00, String jylsh0) {
        Map param = new HashMap();
        String zfmxbh = null;// 支付明细编码
        switch (zfqd00) {
            case "alipay":
                zfmxbh = "G";
                break;
            case "wxpay":
                zfmxbh = "E";
                break;
            case "unionpay":
                zfmxbh = "V";
                break;
            case "pospay":
                zfmxbh = "K";
                break;
            default:
                break;
        }
        StringBuilder original_payload = new StringBuilder();
        original_payload.append("<request>")
                .append("<funcname>SP_SF_TYJK_YJJCZ</funcname>")
                .append("<cardno></cardno>")
                .append("<brid00>" + brid00 + "</brid00>")
                .append("<brxm00>" + brxm00 + "</brxm00>")
                .append("<brzjbh></brzjbh>")
                .append("<brlxdh></brlxdh>")
                .append("<hiskey></hiskey>")
                .append("<cjlybm>YX</cjlybm>")
                .append("<qdcjbm>99</qdcjbm>")
                .append("<zdbh00></zdbh00>")
                .append("<czy000>" + czy000 + "</czy000>")
                .append("<czje00>" + czje00 + "</czje00>")
                .append("<zfmxbh>" + zfmxbh + "</zfmxbh>")
                .append("<jylsh0>" + jylsh0 + "</jylsh0>")
                .append("<qdlsh0></qdlsh0>")
                .append("<yltkbz>1</yltkbz>")
                .append("</request>");
        param.put("original_payload", original_payload.toString());
        return param;
    }

    // 住院通用接口预交金账户充值
    public static Map SP_ZY_TYJK_YJJCZ(String zyh000, String brxm00, String czy000, String czje00, String zfqd00, String jylsh0) {
        Map param = new HashMap();
        String zfmxbh = null;// 支付明细编码
        switch (zfqd00) {
            case "alipay":
                zfmxbh = "G";
                break;
            case "wxpay":
                zfmxbh = "E";
                break;
            case "unionpay":
                zfmxbh = "V";
                break;
            case "pospay":
                zfmxbh = "K";
                break;
            default:
                break;
        }
        StringBuilder original_payload = new StringBuilder();
        original_payload.append("<request>")
                .append("<funcname>SP_ZY_TYJK_YJJCZ</funcname>")
                .append("<zyh000>" + zyh000 + "</zyh000>")
                .append("<zyid00></zyid00>")
                .append("<brxm00>" + brxm00 + "</brxm00>")
                .append("<brzjbh></brzjbh>")
                .append("<brlxdh></brlxdh>")
                .append("<hiskey></hiskey>")
                .append("<cjlybm>YX</cjlybm>")
                .append("<qdcjbm>99</qdcjbm>")
                .append("<zdbh00></zdbh00>")
                .append("<czy000>" + czy000 + "</czy000>")
                .append("<czje00>" + czje00 + "</czje00>")
                .append("<zfmxbh>" + zfmxbh + "</zfmxbh>")
                .append("<jylsh0>" + jylsh0 + "</jylsh0>")
                .append("<qdlsh0></qdlsh0>")
                .append("<yltkbz>1</yltkbz>")
                .append("</request>");
        param.put("original_payload", original_payload.toString());
        return param;
    }

}
