package com.ylz.yx.pay.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ylz.yx.pay.config.ApplicationProperty;
import com.ylz.yx.pay.core.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.WebUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 统一支付网关
 */
@RestController
@RequestMapping("/api")
public class OpenApiController {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenApiController.class);

    @Resource
    private ApplicationProperty applicationProperty;

    /**
     * 统一网关入口
     * @return
     */
    @PostMapping("/onepay")
    public ModelAndView gateway(HttpServletRequest request) {
        //String reqTime = "";
        String method = "";
        String contentType = request.getContentType();
        if(contentType.toLowerCase().indexOf("application/json") >= 0){
            RequestWrapper requestWrapper = new RequestWrapper(request);
            String body = requestWrapper.getBody();
            JSONObject jsonObject = JSON.parseObject(body);
            method = jsonObject.getString("method");
        } else if(contentType.toLowerCase().indexOf("application/x-www-form-urlencoded") >= 0) {
            Map<String, Object> params = WebUtils.getParametersStartingWith(request, "");
            //reqTime = String.valueOf(params.get("reqTime"));
            method = String.valueOf(params.get("method"));
        }
        //LOGGER.info("【{}】>> 执行开始 >> method=【{}】 params = 【{}】", reqTime, method, JSON.toJSONString(params));

        //验证请求地址
        //checkUrl(params, reqTime, request);

        //请求接口
        if("api.facepay.init".equals(method)){ //刷脸初始化
            ModelAndView view = new ModelAndView();
            view.setViewName("forward:/api/facepay/init");//疑问？
            return view;
        }
        if("api.pay.unifiedOrder".equals(method)){ //支付接口
            ModelAndView view = new ModelAndView();
            view.setViewName("forward:/api/pay/unifiedOrder");
            return view;
        }
        if("api.pay.query".equals(method)){ //支付查询接口
            ModelAndView view = new ModelAndView();
            view.setViewName("forward:/api/pay/query");
            return view;
        }
        if("api.pay.close".equals(method)){ //关闭接口
            ModelAndView view = new ModelAndView();
            view.setViewName("forward:/api/pay/close");
            return view;
        }
        if("api.refund.refundOrder".equals(method)){ //退款接口
            ModelAndView view = new ModelAndView();
            view.setViewName("forward:/api/refund/refundOrder");
            return view;
        }
        if("api.refund.query".equals(method)){ //退款查询接口
            ModelAndView view = new ModelAndView();
            view.setViewName("forward:/api/refund/query");
            return view;
        }
        if("api.pos.settle".equals(method)){ //POS结算
            ModelAndView view = new ModelAndView();
            view.setViewName("forward:/api/pos/settle");
            return view;
        }
        if("api.self.synch".equals(method)){ //自助机订单同步
            ModelAndView view = new ModelAndView();
            view.setViewName("forward:/api/self/synch");
            return view;
        }
        throw new BizException("不支持的方法");
    }

    //application/json 方式读取数据
    public static String ReadAsChars(HttpServletRequest request) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder("");
        try {
            br = request.getReader();
            String str;
            while ((str = br.readLine()) != null) {
                sb.append(str);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }

    /**
     * 验证白名单
     *
     * @param request          HTTP请求
     */
    public void checkUrl(Map<String, Object> params, String reqTime, HttpServletRequest request) {

        //校验地址开关
        if (!applicationProperty.getIsCheckUrl()) {
            LOGGER.warn("【{}】>> 验证白名单开关关闭", reqTime);
            return;
        }

        Map<String, Object> map = new HashMap<String, Object>();

        String app_id = params.get("app_id").toString();
        // 获取当前接入ID的白名单信息

        String uri = request.getRequestURI();// 返回请求行中的资源名称
        String url = request.getRequestURL().toString();// 获得客户端发送请求的完整url
        String host = request.getRemoteHost();// 返回发出请求的客户机的主机名
        int port = request.getRemotePort();// 返回发出请求的客户机的端口号。
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            if (ip.indexOf(",") != -1) {
                ip = ip.split(",")[0];
            }
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
            System.out.println("Proxy-Client-IP ip: " + ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
            System.out.println("WL-Proxy-Client-IP ip: " + ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
            System.out.println("HTTP_CLIENT_IP ip: " + ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
            System.out.println("HTTP_X_FORWARDED_FOR ip: " + ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
            System.out.println("X-Real-IP ip: " + ip);
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
            System.out.println("getRemoteAddr ip: " + ip);
        }
        map.put("app_id", app_id);
        map.put("host", host);
        map.put("params", params);
        map.put("url", url);
        map.put("uri", uri);
        map.put("port", String.valueOf(port));
        map.put("ip", ip);
        LOGGER.info("白名单地址 >> map = 【{}】", JSON.toJSONString(map));
    }
}
