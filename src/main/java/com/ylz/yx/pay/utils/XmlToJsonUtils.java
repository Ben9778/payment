package com.ylz.yx.pay.utils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.QuickWriter;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.xml.PrettyPrintWriter;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
import com.thoughtworks.xstream.io.xml.XppDriver;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.*;

import java.io.Writer;
import java.util.List;


/**
 * xml转成json，支持xml多个子节点的情况
 */
public class XmlToJsonUtils {



    public static void main(String[] args) throws DocumentException {


        //String original_payload = "<Response><status>500</status><resultnum><status>200</status><msg>成功</msg></resultnum><msg>多点结算确认失败,系统号=7511 错误原因:找不到该病人医保返回的单据号!</msg></Response>";

        String original_payload  = "<Response><status>200</status><msg>成功</msg><resultnum>1</resultnum><result><serialno>1</serialno><ghid00>3479509</ghid00><xtgzh0>40378</xtgzh0><ykfje0>0.00</ykfje0><msgid>H35098100101202206010906471514</msgid><insuplc_admdvs>350102</insuplc_admdvs><insutype>310</insutype><mdtrt_cert_type></mdtrt_cert_type><mdtrt_cert_no>A99203977</mdtrt_cert_no><certno>352231199510150614</certno><psn_name>凌仙云</psn_name><gend>1</gend><age>26</age><med_type>11</med_type><chrg_bchno>40378</chrg_bchno><ipt_otp_no>158581</ipt_otp_no><dise_codg></dise_codg><dise_name></dise_name><psn_setlway>01</psn_setlway><acct_used_flag>1</acct_used_flag><invono></invono><pub_hosp_rfom_flag></pub_hosp_rfom_flag><dept_code>222</dept_code><dept_name>口腔科</dept_name><caty>222</caty><psn_no>3500000135223119951015061401</psn_no><main_cond_dscr></main_cond_dscr><begntime>2022-06-01 09:46:47</begntime><endtime>2022-06-01 09:46:47</endtime><birctrl_type></birctrl_type><matn_type></matn_type><birctrl_matn_date></birctrl_matn_date><geso_val></geso_val><dr_name>外科主治</dr_name><atddr_no>D190</atddr_no><mdtrt_id></mdtrt_id><setl_id></setl_id><psn_cert_type>01</psn_cert_type><naty>01</naty><brdy>1995-10-15</brdy><psn_type>1101</psn_type><cvlserv_flag>0</cvlserv_flag><medfee_sumamt>1.50</medfee_sumamt><fund_pay_sumamt></fund_pay_sumamt><hifp_pay></hifp_pay><psn_cash_pay></psn_cash_pay><acct_pay></acct_pay><cvlserv_pay></cvlserv_pay><hifes_pay></hifes_pay><hifmi_pay></hifmi_pay><hifob_pay></hifob_pay><maf_pay></maf_pay><oth_pay></oth_pay><hifdm_pay></hifdm_pay><pool_prop_selfpay></pool_prop_selfpay><act_pay_dedc></act_pay_dedc><psn_part_amt></psn_part_amt><hosp_part_amt></hosp_part_amt><balc></balc><acct_mulaid_pay></acct_mulaid_pay><fulamt_ownpay_amt></fulamt_ownpay_amt><overlmt_selfpay></overlmt_selfpay><preselfpay_amt></preselfpay_amt><inscp_scp_amt></inscp_scp_amt><medins_setl_id></medins_setl_id><clr_optins></clr_optins><clr_way></clr_way><clr_type></clr_type><setl_time></setl_time><adm_diag_dscr></adm_diag_dscr><adm_dept_codg></adm_dept_codg><adm_dept_name></adm_dept_name><adm_bed></adm_bed><dscg_maindiag_code></dscg_maindiag_code><dscg_maindiag_name></dscg_maindiag_name><latechb_flag></latechb_flag><pret_flag></pret_flag><dscg_dept_codg></dscg_dept_codg><dscg_dept_name></dscg_dept_name><dscg_way></dscg_way><mxlistnum>1</mxlistnum><zdlistnum>1</zdlistnum><fplist><aka063>01</aka063><aka063_mc>西药费</aka063_mc><aka063_amount>1.5</aka063_amount></fplist><mxlist><serialno>1</serialno><xtgzh0>40378</xtgzh0><feedetl_sn>7845327</feedetl_sn><init_feedetl_sn>158581</init_feedetl_sn><mdtrt_id></mdtrt_id><drord_no></drord_no><psn_no>3500000135223119951015061401</psn_no><chrg_bchno>40378</chrg_bchno><dise_codg></dise_codg><rxno>7845327</rxno><rx_circ_flag>0</rx_circ_flag><med_type>11</med_type><fee_ocur_time>2022-06-01 09:46:47</fee_ocur_time><med_list_codg>XB05XAL211B002020400318</med_list_codg><medins_list_codg>9000000017</medins_list_codg><prcunt>瓶</prcunt><det_item_fee_sumamt>1.50</det_item_fee_sumamt><cnt>1.00</cnt><pric>1.5000</pric><sin_dos_dscr>1</sin_dos_dscr><used_frqu_dscr>0240</used_frqu_dscr><prd_days></prd_days><medc_way_dscr></medc_way_dscr><bilg_dept_codg>222</bilg_dept_codg><bilg_dept_name>口腔科</bilg_dept_name><bilg_dr_codg>D190</bilg_dr_codg><bilg_dr_name>外科主治</bilg_dr_name><acord_dept_codg></acord_dept_codg><acord_dept_name></acord_dept_name><orders_dr_code></orders_dr_code><orders_dr_name></orders_dr_name><hosp_appr_flag>1</hosp_appr_flag><tcmdrug_used_way>2</tcmdrug_used_way><etip_flag>0</etip_flag><etip_hosp_code></etip_hosp_code><dscg_tkdrug_flag>0</dscg_tkdrug_flag><matn_fee_flag>0</matn_fee_flag><memo>氯化钠注射液</memo><comb_no></comb_no><expContent></expContent><bke045>XB05XAL211B002020400318</bke045><bke046>氯化钠注射液</bke046><aka063>01</aka063><aka063_mc>西药费</aka063_mc></mxlist><zdlist><serialno>1</serialno><xtgzh0>40378</xtgzh0><chrg_bchno>40378</chrg_bchno><diag_type>01</diag_type><diag_srt_no>01</diag_srt_no><diag_code>I10.x00x002</diag_code><diag_name>高血压</diag_name><dise_dor_no>D190</dise_dor_no><dise_dor_name>外科主治</dise_dor_name><diag_dept>222</diag_dept><diag_time>2022-06-01 09:46:47</diag_time><vali_flag>1</vali_flag></zdlist></result></Response>";
        System.out.println(documentToJSONObject(original_payload));

        JSONObject jsonObject = documentToJSONObject(original_payload);


        JSONObject result =  JSONObject.parseObject(jsonObject.get("result").toString());
        JSONObject input = (JSONObject) result.get("input");
        System.out.println(input);
        System.out.println(result);

    }


    /**
     * String 转 org.dom4j.Document
     *
     * @param xml
     * @return
     * @throws DocumentException
     */
    private static Document strToDocument(String xml) throws DocumentException {
        return DocumentHelper.parseText(xml);
    }

    /**
     * xml 转  com.alibaba.fastjson.JSONObject
     *
     * @param xml
     * @return
     * @throws DocumentException
     */
    public static JSONObject documentToJSONObject(String xml) {
        JSONObject jsonObject = null;
        try {
            jsonObject = elementToJSONObject(strToDocument(xml).getRootElement());
        } catch (DocumentException e) {
            e.printStackTrace();
        } finally {
            return jsonObject;
        }
    }

    /**
     * org.dom4j.Element 转  com.alibaba.fastjson.JSONObject
     *
     * @param node
     * @return
     */
    public static JSONObject elementToJSONObject(Element node) {
        JSONObject result = new JSONObject();
        // 当前节点的名称、文本内容和属性
        List<Attribute> listAttr = node.attributes();// 当前节点的所有属性的list
        for (Attribute attr : listAttr) {// 遍历当前节点的所有属性
            result.put(attr.getName(), attr.getValue());
        }
        // 递归遍历当前节点所有的子节点
        List<Element> listElement = node.elements();// 所有一级子节点的list
        if (!listElement.isEmpty()) {
            for (Element e : listElement) {// 遍历所有一级子节点
                if (e.attributes().isEmpty() && e.elements().isEmpty()) // 判断一级节点是否有属性和子节点
                {
                    result.put(e.getName(), e.getTextTrim());// 沒有则将当前节点作为上级节点的属性对待
                } else {
                    if (!result.containsKey(e.getName())) // 判断父节点是否存在该一级节点名称的属性
                    {
                        result.put(e.getName(), new JSONArray());// 没有则创建
                    }
                    ((JSONArray) result.get(e.getName())).add(elementToJSONObject(e));// 将该一级节点放入该节点名称的属性对应的值中
                }
            }
        }
        return result;
    }

    /**
     * 创建XStream
     */
    private static XStream createXstream() {
        XStream xstream = new XStream(new MyXppDriver(false));
        xstream.autodetectAnnotations(true);
        return xstream;
    }

    /**
     * 支持注解转化XML
     */
    public static String toXML(Object obj, Class<?> cls) {
        if (obj == null) {
            return null;
        }
        XStream xstream = createXstream();
        xstream.processAnnotations(cls);
        return getDefaultXMLHeader() + xstream.toXML(obj);
    }

    /**
     * Object 转化 XML
     */
    public static String toXML(Object obj) {
        if (obj == null) {
            return null;
        }
        XStream xstream = createXstream();
        return getDefaultXMLHeader() + xstream.toXML(obj);
    }

    /**
     * XML转化为JAVA对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T xml2Obj(String xml, Class<?> cls) {
        if (StringUtils.isBlank(xml)) {
            return null;
        }
        XStream xstream = createXstream();
        if (cls != null) {
            xstream.processAnnotations(cls);
        }
        return (T) xstream.fromXML(xml);
    }

    /**
     * XML转化为JAVA对象
     */
    public static <T> T xml2Obj(String xml) {
        return xml2Obj(xml, null);
    }

    private static String getDefaultXMLHeader() {
        return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n";
    }

    /**
     * @author lixining
     * @version $Id: XmlUtils.java, v 0.1 2015年8月18日 上午9:46:57 lixining Exp $
     * @description XppDriver
     */
    public static class MyXppDriver extends XppDriver {
        boolean useCDATA = false;

        MyXppDriver(boolean useCDATA) {
            super(new XmlFriendlyNameCoder("__", "_"));
            this.useCDATA = useCDATA;
        }

        @Override
        public HierarchicalStreamWriter createWriter(Writer out) {
            if (!useCDATA) {
                return super.createWriter(out);
            }
            return new PrettyPrintWriter(out) {
                boolean cdata = true;

                @Override
                public void startNode(String name, @SuppressWarnings("rawtypes") Class clazz) {
                    super.startNode(name, clazz);
                }

                @Override
                protected void writeText(QuickWriter writer, String text) {
                    if (cdata) {
                        writer.write(cDATA(text));
                    } else {
                        writer.write(text);
                    }
                }

                private String cDATA(String text) {
                    return "<![CDATA[" + text + "]]>";
                }
            };
        }
    }
}

