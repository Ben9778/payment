package com.ylz.yx.pay.payment.controller.refund;

import com.ylz.yx.pay.core.exception.BizException;
import com.ylz.yx.pay.core.model.ApiRes;
import com.ylz.yx.pay.payment.controller.ApiController;
import com.ylz.yx.pay.core.entity.PayTkdd00;
import com.ylz.yx.pay.payment.rqrs.refund.QueryRefundOrderRQ;
import com.ylz.yx.pay.payment.rqrs.refund.QueryRefundOrderRS;
import com.ylz.yx.pay.payment.service.ConfigContextQueryService;
import com.ylz.yx.pay.payment.service.impl.RefundOrderService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
* 商户退款单查询controller
*/
@RestController
public class QueryRefundOrderController extends ApiController {

    @Autowired private RefundOrderService refundOrderService;
    @Autowired private ConfigContextQueryService configContextQueryService;

    /**
     * 查单接口
     * **/
    @RequestMapping("/api/refund/query")
    public ApiRes queryRefundOrder(){

        //获取参数 & 验签
        QueryRefundOrderRQ rq = getRQByWithMchSign(QueryRefundOrderRQ.class);

        if(StringUtils.isAllEmpty(rq.getMchRefundNo(), rq.getRefundOrderId())){
            throw new BizException("mchRefundNo 和 refundOrderId不能同时为空");
        }

        PayTkdd00 payTkdd00 = refundOrderService.queryMchOrder(rq.getMchRefundNo(), rq.getRefundOrderId());
        if(payTkdd00 == null){
            throw new BizException("订单不存在");
        }

        QueryRefundOrderRS bizRes = QueryRefundOrderRS.buildByRefundOrder(payTkdd00);
        return ApiRes.okWithSign(bizRes, configContextQueryService.queryMchApp(rq.getAppId()).getQdmy00());
    }
}
