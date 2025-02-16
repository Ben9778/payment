package com.ylz.yx.pay.interceptor;

import com.ylz.svc.web.HttpResponse;
import com.ylz.yx.pay.config.ApplicationProperty;
import com.ylz.yx.pay.core.exception.CustomException;
import com.ylz.yx.pay.core.exception.HttpStatus;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

@Aspect
@Component
public class IpAddressInterceptor {

    @Resource
    private ApplicationProperty applicationProperty;

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST_IP = "127.0.0.1";
    // 客户端与服务器同为一台机器，获取的 ip 有时候是 ipv6 格式
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    private static final String SEPARATOR = ",";

    /**
     * 定义拦截器规则
     */
//    @Pointcut("execution(* com.test.test.api.controller.test.test.*(..))")
    @Pointcut("@annotation(com.ylz.yx.pay.annotation.IntranetIp)")
    public void pointCut() {
    }

    /**
     * 拦截器具体实现
     *
     * @throws Throwable
     */
    @Before("pointCut()")
    public void doBefore(JoinPoint joinPoint) {
        String ip="";
        Boolean allowAccess = false;
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(attributes != null){
            HttpServletRequest request = attributes.getRequest();
            ip = request.getHeader("x-forwarded-for");
            if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Forwarded-For");
            }
            if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("WL-Proxy-Client-IP");
            }
            if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getHeader("X-Real-IP");
            }
            if (ip == null || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
                ip = request.getRemoteAddr();
                if (LOCALHOST_IP.equalsIgnoreCase(ip) || LOCALHOST_IPV6.equalsIgnoreCase(ip)) {
                    // 根据网卡取本机配置的 IP
                    InetAddress iNet = null;
                    try {
                        iNet = InetAddress.getLocalHost();
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    if (iNet != null)
                        ip = iNet.getHostAddress();
                }
            }
            // 对于通过多个代理的情况，分割出第一个 IP
            if (ip != null && ip.length() > 15) {
                if (ip.indexOf(SEPARATOR) > 0) {
                    ip = ip.substring(0, ip.indexOf(SEPARATOR));
                }
            }

        }

        if (!applicationProperty.getIsCheckUrl()) {
            allowAccess = true;
        } else {
            //如果IP地址在这个网段内，则允许访问
            if(ip.contains(applicationProperty.getIntranetIp())) {
                allowAccess = true;
            }
        }
        if(allowAccess) {

        } else {
            throw new CustomException(HttpStatus.BAD_REQUEST, "禁止访问！");
        }
    }

}
