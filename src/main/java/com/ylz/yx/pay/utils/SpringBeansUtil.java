package com.ylz.yx.pay.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * @description: Spring 框架下, 获取Beans静态函数方法。
 */
@Component
public class SpringBeansUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext = null;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if(SpringBeansUtil.applicationContext == null){
            SpringBeansUtil.applicationContext  = applicationContext;
        }
    }

    /** 获取applicationContext */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

     /** 通过name获取 Bean. */
    public static Object getBean(String name){

        if(!getApplicationContext().containsBean(name)){
            return null;
        }

        return getApplicationContext().getBean(name);

    }

     /** 通过class获取Bean. */
    public static <T> T getBean(Class<T> clazz){
        try {
            return getApplicationContext().getBean(clazz);
        } catch (BeansException e) {
            return null;
        }
    }

     /** 通过name,以及Clazz返回指定的Bean */
    public static <T> T getBean(String name, Class<T> clazz){
        if(!getApplicationContext().containsBean(name)){
            return null;
        }
        return getApplicationContext().getBean(name, clazz);
    }

}
