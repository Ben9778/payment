package com.ylz.yx.pay.payment.util;

import com.ylz.yx.pay.payment.rqrs.AbstractRS;

/*
* api响应结果构造器
*/
public class ApiResBuilder {

    /** 构建自定义响应对象, 默认响应成功 **/
    public static <T extends AbstractRS> T buildSuccess(Class<? extends AbstractRS> T){

        try {
            T result = (T)T.newInstance();
            return result;

        } catch (Exception e) { return null; }
    }

}
