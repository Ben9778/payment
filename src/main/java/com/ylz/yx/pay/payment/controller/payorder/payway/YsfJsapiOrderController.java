package com.ylz.yx.pay.payment.controller.payorder.payway;

import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.model.ApiRes;
import com.ylz.yx.pay.payment.controller.payorder.AbstractPayOrderController;
import com.ylz.yx.pay.payment.rqrs.payorder.payway.YsfJsapiOrderRQ;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * 云闪付 jsapi支付 controller
 */
@RestController
public class YsfJsapiOrderController extends AbstractPayOrderController {


    /**
     * 统一下单接口
     * **/
    @PostMapping("/api/pay/ysfJsapiOrder")
    public ApiRes aliJsapiOrder(){

        //获取参数 & 验证
        YsfJsapiOrderRQ bizRQ = getRQByWithMchSign(YsfJsapiOrderRQ.class);

        // 统一下单接口
        return unifiedOrder(CS.PAY_WAY_CODE.YSF_JSAPI, bizRQ);

    }


}
