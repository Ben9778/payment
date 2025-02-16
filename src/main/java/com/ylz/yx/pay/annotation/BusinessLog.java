package com.ylz.yx.pay.annotation;

import java.lang.annotation.*;

/**
 * 自定义操作日志记录注解
 */
@Target({ ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BusinessLog
{
    /**
     * 模块 
     */
    String title() default "";

    /**
     * 查询的方法
     */
    String sqlName() default "";

    /**
     * 查询的参数
     */
    String sqlParam() default "";

    /**
     * 功能
     */
    BusinessType businessType() default BusinessType.OTHER;

    /**
     * 是否保存请求的参数
     */
    boolean isSaveRequestData() default true;
}
