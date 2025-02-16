package com.ylz.yx.pay.payment.rqrs;

import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.io.Serializable;

/*
* 接口抽象RS对象, 本身无需实例化
*/
@Data
public abstract class AbstractRS implements Serializable {

    public String toJSONString(){
        return JSON.toJSONString(this);
    }

}
