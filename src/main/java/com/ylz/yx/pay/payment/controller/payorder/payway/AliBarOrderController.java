package com.ylz.yx.pay.payment.controller.payorder.payway;

import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.model.ApiRes;
import com.ylz.yx.pay.payment.controller.payorder.AbstractPayOrderController;
import com.ylz.yx.pay.payment.rqrs.payorder.payway.AliBarOrderRQ;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/*
* 支付宝 条码支付 controller
*/
@RestController
public class AliBarOrderController extends AbstractPayOrderController {
    /**
     * 统一下单接口
     * **/
    @PostMapping("/api/pay/aliBarOrder")
    public ApiRes aliBarOrder(){

        //获取参数 & 验证
        AliBarOrderRQ bizRQ = getRQByWithMchSign(AliBarOrderRQ.class);

        // 统一下单接口
        return unifiedOrder(CS.PAY_WAY_CODE.ALI_BAR, bizRQ);

    }


}
