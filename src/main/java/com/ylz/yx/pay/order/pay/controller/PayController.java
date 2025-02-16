package com.ylz.yx.pay.order.pay.controller;

import com.ylz.svc.web.HttpResponse;
import com.ylz.yx.pay.order.pay.PayService;
import com.ylz.yx.pay.order.pay.model.PayZfdd00Param;
import com.ylz.yx.pay.order.pay.model.SelectParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@RestController
@RequestMapping("/api/payorder")
public class PayController {
    @Autowired
    PayService payService;

    /**
     * 充值订单列表数据导出
     **/
    @GetMapping("/expList")
    public String expList(SelectParam param, HttpServletResponse response) {
        return payService.expList(param, response);
    }

    /**
     * 重新退款
     **/
    /*@PostMapping("/orderRefund")
    public Map<String, Object> orderRefund(@RequestBody PayZfdd00Param param) {
        return payService.orderRefund(param);
    }*/

    /**
     * 申请退款
     **/
    @PostMapping("/applyRefund")
    public HttpResponse applyRefund(@RequestBody PayZfdd00Param param) {
        return payService.applyRefund(param);
    }
}
