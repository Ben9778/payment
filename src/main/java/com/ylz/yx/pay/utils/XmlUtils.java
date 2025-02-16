package com.ylz.yx.pay.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;


public class XmlUtils {
    public static JSONObject xml2Json(String xmlStr) throws JDOMException, IOException {
        if (StringUtils.isEmpty(xmlStr)) {
            return null;
        }
        xmlStr = xmlStr.replaceAll("\\\n", "");
        byte[] xml = xmlStr.getBytes(StandardCharsets.UTF_8);
        JSONObject json = new JSONObject();
        InputStream is = new ByteArrayInputStream(xml);
        SAXBuilder sb = new SAXBuilder();
        Document doc = sb.build(is);
        Element root = doc.getRootElement();
        json.put(root.getName(), iterateElement(root));

        return json;
    }

    private static JSONObject iterateElement(Element element) {
        List<Element> node = element.getChildren();
        JSONObject obj = new JSONObject();
        List list = null;
        for (Element child : node) {
            list = new LinkedList();
            String text = child.getTextTrim();
            if (StringUtils.isBlank(text)) {
                if (child.getChildren().size() == 0) {
                    continue;
                }
                if (obj.containsKey(child.getName())) {
                    list = (List) obj.get(child.getName());
                }
                list.add(iterateElement(child)); //遍历child的子节点
                obj.put(child.getName(), list);
            } else {
                if (obj.containsKey(child.getName())) {
                    Object value = obj.get(child.getName());
                    try {
                        list = (List) value;
                    } catch (ClassCastException e) {
                        list.add(value);
                    }
                }
                if (child.getChildren().size() == 0) { //child无子节点时直接设置text
                    obj.put(child.getName(), text);
                } else {
                    list.add(text);
                    obj.put(child.getName(), list);
                }
            }
        }
        return obj;
    }
    
    
    public static JSONObject xmlToJSON(String xml) throws JDOMException, IOException {
    	
    	String sendInfo =  xml.substring(xml.indexOf("<Body>"), xml.indexOf("</Body>")+7);
    	JSONObject json = xml2Json(sendInfo);
    	return json;
		
	}
    
    public static void main(String[] args) throws JDOMException, IOException {
    	
    	String xml = "<Response><status>200</status><msg>成功</msg><resultnum>1</resultnum><result><zyh000>00182808</zyh000><zyid00>26609</zyid00><brid00>58892</brid00><xming0>叶于晨</xming0><xbiebm>1</xbiebm><xbiemc>男</xbiemc><csrq00>19540108</csrq00><fzxbh0>350100</fzxbh0><fzxmc0>福州市医保中心</fzxmc0><lxdh00>15959151097</lxdh00><sfzha0></sfzha0><yjjye0>-40020.55</yjjye0><identitytype>3</identitytype><ryrq00>20180531</ryrq00><rysj00>09:18:11</rysj00><cydjrq></cydjrq><cydjsj></cydjsj><cyrq00></cyrq00><cysj00></cysj00><brzt00>20</brzt00><dqbq00>2265</dqbq00><dqbqmc>分院3-8F</dqbqmc><dqks00>2246</dqks00><dqksmc>心血管二科(病区)</dqksmc><rycwh0>40</rycwh0><dbzbm0></dbzbm0><dbzmc0></dbzmc0><ybzyh0>1055026</ybzyh0><psn_no></psn_no><aae140></aae140><aae140_mc></aae140_mc><psn_cert_type></psn_cert_type><certno>350102531101005</certno><psn_name>叶于晨</psn_name><card_sn></card_sn><coner_name></coner_name><tel>15959151097</tel><begntime>2018-05-31 09:18:11</begntime><ipt_no>00182808</ipt_no><medrcdno>00182808</medrcdno><insutype></insutype><med_type></med_type><atddr_no></atddr_no><chfpdr_name></chfpdr_name><adm_diag_dscr></adm_diag_dscr><dscg_maindiag_code>test_I24.801</dscg_maindiag_code><dscg_maindiag_name>test_急性冠状动脉供血不足</dscg_maindiag_name><insuplc_admdvs>359900</insuplc_admdvs><mdtrtarea_admvs>359900</mdtrtarea_admvs><matn_type></matn_type><birctrl_type></birctrl_type><latechb_flag></latechb_flag><pret_flag></pret_flag><yjj_sumamt>56000.00</yjj_sumamt><Diseinfo><zdlist><psn_no></psn_no><diag_type>1</diag_type><maindiag_flag>1</maindiag_flag><diag_srt_no>1</diag_srt_no><diag_code>test_I24.801</diag_code><diag_name>test_急性冠状动脉供血不足</diag_name><adm_cond></adm_cond><diag_dept>国家科室=2246</diag_dept><dise_dor_no>医师:2291</dise_dor_no><dise_dor_name>郑泳</dise_dor_name><diag_time>2013-07-12 15:42:11</diag_time></zdlist></diseinfo></result></Response>";
    	//String xml="<?xml version=\\\"1.0\\\" encoding=\\\"gbk\\\" ?><request><Header><funid></funid><sign>0000000000000000</sign></Header><Body><cardno></cardno><card_type>04</card_type><name></name><business_type>02</business_type><inpatient_id></inpatient_id><patient_id>0</patient_id><operator_id></operator_id><operator_name></operator_name><dkfs00>01</dkfs00><yljzfs>20</yljzfs><yllb00>21</yllb00></Body></request>";
    	
    	
    	//String sendInfo =  xml.substring(xml.indexOf("<Body>"), xml.indexOf("</Body>")+7);
    	
    	
    	//System.out.println(sendInfo);
    	
    	JSONObject json = xml2Json(xml);
//    	JSONObject jsonBody = json.getJSONObject("Body");
//    	System.out.println(jsonBody.get("grzhye"));
        System.out.println(json.getJSONObject("Response").toJSONString());
    	
    	//json2
        
    	
    	//System.out.println(json.getJSONObject("Body").get("zftkhj"));
		
	}
}