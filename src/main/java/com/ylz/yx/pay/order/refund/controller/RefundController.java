package com.ylz.yx.pay.order.refund.controller;

import com.ylz.svc.web.HttpResponse;
import com.ylz.yx.pay.order.refund.model.PayTkdd00Param;
import com.ylz.yx.pay.order.refund.model.SelectParam;
import com.ylz.yx.pay.order.refund.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/refundorder")
public class RefundController {
    @Autowired
    RefundService refundService;

    /**
     * 退款订单列表数据导出
     **/
    @GetMapping("/expList")
    public String expList(SelectParam param, HttpServletResponse response) {
        return refundService.expList(param, response);
    }

    /**
     * 重新退款
     **/
    @PostMapping("/orderRefund")
    public HttpResponse orderRefund(@RequestBody PayTkdd00Param param) {
        return refundService.orderRefund(param);
    }
}
