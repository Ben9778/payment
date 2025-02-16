package com.ylz.yx.pay.payment.controller.payorder;

import com.ylz.yx.pay.core.exception.BizException;
import com.ylz.yx.pay.core.model.ApiRes;
import com.ylz.yx.pay.payment.controller.ApiController;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.payment.rqrs.payorder.QueryPayOrderRQ;
import com.ylz.yx.pay.payment.rqrs.payorder.QueryPayOrderRS;
import com.ylz.yx.pay.payment.service.ConfigContextQueryService;
import com.ylz.yx.pay.payment.service.impl.PayOrderService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
* 商户查单controller
*/
@RestController
public class QueryOrderController extends ApiController {

    @Autowired private PayOrderService payOrderService;
    @Autowired private ConfigContextQueryService configContextQueryService;

    /**
     * 查单接口
     * **/
    @RequestMapping("/api/pay/query")
    public ApiRes queryOrder(){

        //获取参数 & 验签
        QueryPayOrderRQ rq = getRQByWithMchSign(QueryPayOrderRQ.class);

        if(StringUtils.isAllEmpty(rq.getMchOrderNo(), rq.getPayOrderId())){
            throw new BizException("mchOrderNo 和 payOrderId不能同时为空");
        }
        //如果商户订单号或支付订单号不为空，则进行数据库查询订单,PayZfdd00实体类接收数据库返回的数据
        PayZfdd00 payZfdd00 = payOrderService.queryMchOrder(rq.getPayOrderId(), rq.getMchOrderNo());
        if(payZfdd00 == null){
            throw new BizException("订单不存在");
        }
        //从payZfdd00订单参数实体类查询出来的数据转化成前端要的数据,用QueryPayOrderRS封装
        QueryPayOrderRS bizRes = QueryPayOrderRS.buildByPayOrder(payZfdd00);
        //返回数据包含参数和对data加密的签名
        return ApiRes.okWithSign(bizRes, configContextQueryService.queryMchApp(rq.getAppId()).getQdmy00());
    }

}
