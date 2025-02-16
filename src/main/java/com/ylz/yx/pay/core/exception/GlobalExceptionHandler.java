package com.ylz.yx.pay.core.exception;

import com.ylz.core.logging.Logger;
import com.ylz.svc.web.HttpResponse;
import com.ylz.yx.pay.core.model.ApiRes;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.StringJoiner;

/**
* @Description   全局异常处理
*/
@RestControllerAdvice
public class GlobalExceptionHandler {

    private final Logger logger = new Logger("pay", "globalException", GlobalExceptionHandler.class.getName());


    @ExceptionHandler(BizException.class)
    @ResponseBody //在返回自定义相应类的情况下必须有，这是@ControllerAdvice注解的规定
    public ApiRes bizExceptionHandler(Exception e) {
        ApiRes apiRes = ApiRes.ok();
        BizException customException = (BizException) e;
        apiRes = customException.getApiRes();
        return apiRes;
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseBody //在返回自定义相应类的情况下必须有，这是@ControllerAdvice注解的规定
    public HttpResponse exceptionHandler(Exception e) {
        HttpResponse resp = new HttpResponse();
        if(StringUtils.isBlank(e.getMessage())){
            CustomException customException = (CustomException) e;
            resp.setStatus(customException.getStatus());
            resp.setTitle(customException.getTitle());
            logger.error("系统异常：("+e.getCause()+"):"+e.getMessage(),e);
        }else if(e.getMessage().contains("Connection refused")
                || e.getMessage().contains("connect timed out")
                || e.getMessage().contains("Load balancer does not have available server for client")){
            resp.setStatus(500);
            resp.setTitle("系统繁忙，请稍后在试");
            logger.error("访问其他服务异常："+e.getMessage(),e);
        }else if(e.getMessage().contains("The Token has expired")){
            resp.setStatus(403);
            resp.setTitle("权限不足，请获取token");
            logger.error("登录过期："+e.getMessage(),e);
        }else{
            resp.setStatus(500);
            resp.setTitle("系统异常：" + e);
            logger.error("系统异常：("+e.getCause()+"):"+e.getMessage(),e);
        }
        return resp;
    }

    /**
     * 这里统一处理参数为空异常，将异常信息放到title中返回给前端
     * @param e 
     * @return
     */
    @ExceptionHandler(BindException.class)
    @ResponseBody
    public HttpResponse bindExceptionHandler(BindException e) {
        HttpResponse resp = new HttpResponse();
        // 从异常对象中拿到ObjectError对象
        List<ObjectError> errorDefaultMessage = e.getBindingResult().getAllErrors();
        StringJoiner stringJoiner = new StringJoiner("!");
        for(ObjectError objectError : errorDefaultMessage){
            stringJoiner.add(objectError.getDefaultMessage());
        }
        resp.setStatus(500);
        resp.setTitle("参数不足!"+stringJoiner);
        // 然后提取错误提示信息进行返回
        return resp;
    }

}