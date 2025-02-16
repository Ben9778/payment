package com.ylz.yx.pay.system.log.aspectj;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.svc.web.token.JwtUtils;
import com.ylz.yx.pay.annotation.BusinessLog;
import com.ylz.yx.pay.annotation.BusinessStatus;
import com.ylz.yx.pay.annotation.BusinessType;
import com.ylz.yx.pay.core.exception.CustomException;
import com.ylz.yx.pay.system.log.model.XtCzrzjl;
import com.ylz.yx.pay.system.log.utils.ServletUtils;
import com.ylz.yx.pay.utils.StringKit;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import com.ylz.core.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.HandlerMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 操作日志记录处理
 */
@Aspect
@Component
public class LogAspect {

    private static final Logger log = new Logger(LogAspect.class.getName());

    @Autowired
    JdbcGateway jdbcGateway;
    /**
     * 异步处理线程池
     */
    private final static ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(10);


    // 配置织入点
    @Pointcut("@annotation(com.ylz.yx.pay.annotation.BusinessLog)")
    public void logPointCut() {
    }

    /**
     * 处理完请求后执行
     *
     * @param joinPoint 切点
     */
    //@AfterReturning(pointcut = "logPointCut()", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, Object jsonResult) {
        handleLog(joinPoint, null, jsonResult);
    }

    /**
     * 拦截异常操作
     *
     * @param joinPoint 切点
     * @param e         异常
     */
    //@AfterThrowing(value = "logPointCut()", throwing = "e")
    public void doAfterThrowing(JoinPoint joinPoint, Exception e) {
        handleLog(joinPoint, e, null);
    }

    @Around("logPointCut()")
    public Object around(ProceedingJoinPoint joinPoint) {
        Object object = null;
        Map<String, Object> oldMap = new HashMap<>();
        Map<String, Object> newMap = new HashMap<>();
        try {
            // 获得注解
            BusinessLog controllerLog = getAnnotationLog(joinPoint);
            if (controllerLog == null) {
                return object;
            }
            // *========数据库日志=========*//
            XtCzrzjl xtCzrzjl = new XtCzrzjl();
            xtCzrzjl.setId0000(StringKit.getUUID());
            xtCzrzjl.setCzzt00(BusinessStatus.SUCCESS.ordinal());
            xtCzrzjl.setQqurl0(ServletUtils.getRequest().getRequestURI());
            String token = ServletUtils.getRequest().getParameter("token");
            String account = JwtUtils.validateToken(token);
            if (account != null) {
                xtCzrzjl.setYgbh00(account);
            }
            // 设置方法名称
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            xtCzrzjl.setQqff00(className + "." + methodName + "()");
            // 设置请求方式
            xtCzrzjl.setQqfs00(ServletUtils.getRequest().getMethod());
            // 处理设置注解上的参数
            getControllerMethodDescription(joinPoint, controllerLog, xtCzrzjl);
            if(BusinessType.UPDATE.equals(controllerLog.businessType()) && StringUtils.isNotBlank(controllerLog.sqlName())){
                Map mapType = JSON.parseObject(xtCzrzjl.getQqcs00(),Map.class);
                Object obj = jdbcGateway.selectOne(controllerLog.sqlName(),mapType.get(controllerLog.sqlParam()));
                oldMap = (Map<String, Object>) objectToMap(obj);
            }

            try {
                //执行页面请求模块方法，并返回
                object = joinPoint.proceed();
                // 返回参数
                xtCzrzjl.setFhcs00(JSON.toJSONString(object));
                if(BusinessType.UPDATE.equals(controllerLog.businessType()) && StringUtils.isNotBlank(controllerLog.sqlName())){
                    Map mapType = JSON.parseObject(xtCzrzjl.getQqcs00(),Map.class);
                    newMap = jdbcGateway.selectOne(controllerLog.sqlName(),mapType.get(controllerLog.sqlParam()));
                }
                if(BusinessType.UPDATE.equals(controllerLog.businessType())){
                    // 操作内容
                    xtCzrzjl.setCznr00(defaultDealUpdate(newMap, oldMap));
                }
            } catch (Throwable e) {
                if (e != null) {
                    xtCzrzjl.setCzzt00(BusinessStatus.FAIL.ordinal());
                    xtCzrzjl.setCwxx00(StringUtils.substring(e.getMessage(), 0, 2000));
                }
                if(StringUtils.isBlank(e.getMessage())) {
                    CustomException customException = (CustomException) e;
                    throw new CustomException(customException.getStatus(), customException.getTitle());
                }
            }
            // 保存数据库
            scheduledThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    jdbcGateway.insert("system.czrzjl.insertSelective", xtCzrzjl);
                }
            });
        } catch (Exception exp) {
            // 记录本地异常日志
            log.error("==前置通知异常==");
            log.info("异常信息:" + exp.getMessage());
            //exp.printStackTrace();
            if(StringUtils.isBlank(exp.getMessage())) {
                CustomException customException = (CustomException) exp;
                throw new CustomException(customException.getStatus(), customException.getTitle());
            }
        }
        return object;
    }

    private String defaultDealUpdate(Map<String, Object> newMap, Map<String, Object> oldMap){
        try {
            StringBuilder str = new StringBuilder();
            oldMap.forEach((k, v) -> {
                Object newResult = newMap.get(k);
                if (null!=v && !v.equals(newResult)) {
                    str.append(k).append(" 由：").append(v).append("，").append("变更为：").append(newResult).append("。\n");
                }
            });
            return str.toString();
        } catch (Exception e) {
            log.error("比较异常", e);
            throw new RuntimeException("比较异常",e);
        }
    }

    private Map<?, ?> objectToMap(Object obj) {
        if (obj == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 如果使用JPA请自己打开这条配置
        // mapper.addMixIn(Object.class, IgnoreHibernatePropertiesInJackson.class);
        Map<?, ?> mappedObject = mapper.convertValue(obj, Map.class);
        return mappedObject;
    }

    protected void handleLog(final JoinPoint joinPoint, final Exception e, Object jsonResult) {
        try {
            // 获得注解
            BusinessLog controllerLog = getAnnotationLog(joinPoint);
            if (controllerLog == null) {
                return;
            }

            // *========数据库日志=========*//
            XtCzrzjl xtCzrzjl = new XtCzrzjl();
            xtCzrzjl.setId0000(StringKit.getUUID());
            xtCzrzjl.setCzzt00(BusinessStatus.SUCCESS.ordinal());
            // 返回参数
            xtCzrzjl.setFhcs00(JSON.toJSONString(jsonResult));

            xtCzrzjl.setQqurl0(ServletUtils.getRequest().getRequestURI());

            String token = ServletUtils.getRequest().getParameter("token");
            String account = JwtUtils.validateToken(token);

            if (account != null) {
                xtCzrzjl.setYgbh00(account);
            }

            if (e != null) {
                xtCzrzjl.setCzzt00(BusinessStatus.FAIL.ordinal());
                xtCzrzjl.setCwxx00(StringUtils.substring(e.getMessage(), 0, 2000));
            }
            // 设置方法名称
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = joinPoint.getSignature().getName();
            xtCzrzjl.setQqff00(className + "." + methodName + "()");
            // 设置请求方式
            xtCzrzjl.setQqfs00(ServletUtils.getRequest().getMethod());
            // 处理设置注解上的参数
            getControllerMethodDescription(joinPoint, controllerLog, xtCzrzjl);
            // 保存数据库
            scheduledThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    jdbcGateway.insert("system.czrzjl.insertSelective", xtCzrzjl);
                }
            });
        } catch (Exception exp) {
            // 记录本地异常日志
            log.error("==前置通知异常==");
            log.error("异常信息:" + exp.getMessage());
            exp.printStackTrace();
        }
    }

    /**
     * 获取注解中对方法的描述信息 用于Controller层注解
     *
     * @param log      日志
     * @param xtCzrzjl 操作日志
     * @throws Exception
     */
    public void getControllerMethodDescription(JoinPoint joinPoint, BusinessLog log, XtCzrzjl xtCzrzjl) throws Exception {
        // 设置action动作
        xtCzrzjl.setYwlx00(log.businessType().ordinal());
        // 设置标题
        xtCzrzjl.setCzmk00(log.title());
        // 是否需要保存request，参数和值
        if (log.isSaveRequestData()) {
            // 获取参数的信息，传入到数据库中。
            setRequestValue(joinPoint, xtCzrzjl);
        }
    }

    /**
     * 获取请求的参数，放到log中
     *
     * @param xtCzrzjl 操作日志
     * @throws Exception 异常
     */
    private void setRequestValue(JoinPoint joinPoint, XtCzrzjl xtCzrzjl) throws Exception {
        String requestMethod = xtCzrzjl.getQqfs00();
        if (HttpMethod.PUT.name().equals(requestMethod) || HttpMethod.POST.name().equals(requestMethod)) {
            String params = argsArrayToString(joinPoint.getArgs());
            xtCzrzjl.setQqcs00(StringUtils.substring(params, 0, 2000));
        } else {
            Map<?, ?> paramsMap = (Map<?, ?>) ServletUtils.getRequest().getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
            xtCzrzjl.setQqcs00(StringUtils.substring(paramsMap.toString(), 0, 2000));
        }
    }

    /**
     * 是否存在注解，如果存在就获取
     */
    private BusinessLog getAnnotationLog(JoinPoint joinPoint) throws Exception {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();

        if (method != null) {
            return method.getAnnotation(BusinessLog.class);
        }
        return null;
    }

    /**
     * 参数拼装
     */
    private String argsArrayToString(Object[] paramsArray) {
        String params = "";
        if (paramsArray != null && paramsArray.length > 0) {
            for (int i = 0; i < paramsArray.length; i++) {
                if (!isFilterObject(paramsArray[i])) {
                    Object jsonObj = JSON.toJSON(paramsArray[i]);
                    params += jsonObj.toString() + " ";
                }
            }
        }
        return params.trim();
    }

    /**
     * 判断是否需要过滤的对象。
     *
     * @param o 对象信息。
     * @return 如果是需要过滤的对象，则返回true；否则返回false。
     */
    public boolean isFilterObject(final Object o) {
        return o instanceof MultipartFile || o instanceof HttpServletRequest || o instanceof HttpServletResponse;
    }
}
