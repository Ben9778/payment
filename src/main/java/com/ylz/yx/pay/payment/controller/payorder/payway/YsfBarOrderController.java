package com.ylz.yx.pay.payment.controller.payorder.payway;

import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.model.ApiRes;
import com.ylz.yx.pay.payment.controller.payorder.AbstractPayOrderController;
import com.ylz.yx.pay.payment.rqrs.payorder.payway.YsfBarOrderRQ;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * 云闪付 条码支付 controller
 */
@RestController
public class YsfBarOrderController extends AbstractPayOrderController {

    /**
     * 统一下单接口
     * **/
    @PostMapping("/api/pay/ysfBarOrder")
    public ApiRes aliBarOrder(){

        //获取参数 & 验证
        YsfBarOrderRQ bizRQ = getRQByWithMchSign(YsfBarOrderRQ.class);

        // 统一下单接口
        return unifiedOrder(CS.PAY_WAY_CODE.YSF_BAR, bizRQ);

    }
}
