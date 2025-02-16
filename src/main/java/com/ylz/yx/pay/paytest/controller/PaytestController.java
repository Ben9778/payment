package com.ylz.yx.pay.paytest.controller;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.JeepayClient;
import com.jeequan.jeepay.exception.JeepayException;
import com.jeequan.jeepay.model.PayOrderCloseReqModel;
import com.jeequan.jeepay.model.PayOrderCreateReqModel;
import com.jeequan.jeepay.model.RefundOrderCreateReqModel;
import com.jeequan.jeepay.request.PayOrderCloseRequest;
import com.jeequan.jeepay.request.PayOrderCreateRequest;
import com.jeequan.jeepay.request.RefundOrderCreateRequest;
import com.jeequan.jeepay.response.PayOrderCloseResponse;
import com.jeequan.jeepay.response.PayOrderCreateResponse;
import com.jeequan.jeepay.response.RefundOrderCreateResponse;
import com.ylz.core.logging.Logger;
import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.yx.pay.config.ApplicationProperty;
import com.ylz.yx.pay.core.ctrl.AbstractCtrl;
import com.ylz.yx.pay.core.entity.PayFwqd00;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.core.exception.BizException;
import com.ylz.yx.pay.core.exception.CustomException;
import com.ylz.yx.pay.core.exception.HttpStatus;
import com.ylz.yx.pay.paytest.model.QueryParam;
import com.ylz.yx.pay.utils.SeqKit;
import com.ylz.yx.pay.utils.ValidateUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/*
* 支付测试类
*/
@RestController
@RequestMapping("/api/paytest")
public class PaytestController extends AbstractCtrl {

    private static final Logger log = new Logger(PaytestController.class.getName());

    @Autowired
    JdbcGateway jdbcGateway;
    @Resource
    ApplicationProperty applicationProperty;

    /** 调起下单接口 **/
    @PostMapping("/payOrders")
    public Object doPay() {

        //获取请求参数
        String fwqdid = getValStringRequired("appId");
        String orderType = getValStringRequired("orderType");
//        String fwqdid = "76B6783E1B9746989C1F9611B720CD5B";
        PayOrderCreateRequest request = new PayOrderCreateRequest();
        PayOrderCreateReqModel model = new PayOrderCreateReqModel();
        request.setBizModel(model);

        model.setAppId(fwqdid);
        model.setMchOrderNo(SeqKit.genMhoOrderId());
        model.setWayCode(getValStringRequired("wayCode"));
        model.setAmount(getRequiredAmountL("amount"));
        model.setSubject("模拟测试");

        model.setUserName("张三");
        model.setIdCard("1");
        model.setCardNo("1");
        model.setOrderType(orderType);
        model.setOperatorId("1001");
        model.setOperatorName("测试账号");

        //设置扩展参数
        JSONObject extParams = new JSONObject();
        String authCode = getValString("authCode");
        if(StringUtils.isNotEmpty(authCode)) {
            extParams.put("authCode", authCode);
            extParams.put("clientIp", getClientIp());
        }
        model.setChannelExtra(extParams.toString());

        PayFwqd00 mchApp = jdbcGateway.selectOne("pay.fwqd00.selectByPrimaryKey", fwqdid);

        log.info("后端请求地址："+applicationProperty.getPaySiteBackUrl());

        JeepayClient jeepayClient = new JeepayClient(applicationProperty.getPaySiteBackUrl(), mchApp.getQdmy00());
        try {
            PayOrderCreateResponse response = jeepayClient.execute(request);
            if(response.getCode() != 0){
                throw new BizException(response.getMsg());
            }
            return response.get();
        } catch (JeepayException e) {
            throw new BizException(e.getMessage());
        }
    }

    /** 调起退款接口 **/
    @PostMapping("/refundOrders")
    public Object doRefund() {

        String xtddh0 = getValStringRequired("id0000");

        PayZfdd00 payZfdd00 = jdbcGateway.selectOne("pay.zfdd00.selectByXtddh0", xtddh0);
        if (payZfdd00 == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "订单不存在！");
        }

        if(!payZfdd00.getDdzt00().equals(PayZfdd00.STATE_SUCCESS)){
            throw new CustomException(HttpStatus.BAD_REQUEST, "订单状态不正确");
        }

        if(payZfdd00.getTkzje0() + payZfdd00.getZfje00() > payZfdd00.getZfje00()){
            throw new CustomException(HttpStatus.BAD_REQUEST, "退款金额超过订单可退款金额！");
        }

        RefundOrderCreateRequest request = new RefundOrderCreateRequest();
        RefundOrderCreateReqModel model = new RefundOrderCreateReqModel();
        request.setBizModel(model);

        model.setAppId(payZfdd00.getFwqdid());
        model.setPayOrderId(payZfdd00.getXtddh0());
        model.setMchRefundNo(SeqKit.genMhoOrderId());
        model.setRefundAmount(payZfdd00.getZfje00());
        model.setRefundReason("退款测试");
        model.setOperatorId("99999");
        model.setOperatorName("系统");

        PayFwqd00 mchApp = jdbcGateway.selectOne("pay.fwqd00.selectByPrimaryKey", payZfdd00.getFwqdid());

        JeepayClient jeepayClient = new JeepayClient(applicationProperty.getPaySiteBackUrl(), mchApp.getQdmy00());

        try {
            RefundOrderCreateResponse response = jeepayClient.execute(request);
            if(response.getCode() != 0){
                throw new CustomException(HttpStatus.BAD_REQUEST, response.getMsg());
            }
        } catch (JeepayException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        return "";
    }

    /** 调起关闭接口 **/
    @PostMapping("/closeOrders")
    public Object doClose() {

        String xtddh0 = getValStringRequired("id0000");

        PayZfdd00 payZfdd00 = jdbcGateway.selectOne("pay.zfdd00.selectByXtddh0", xtddh0);
        if (payZfdd00 == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "订单不存在！");
        }

        if(!PayZfdd00.STATE_INIT.equals(payZfdd00.getDdzt00()) && !PayZfdd00.STATE_ING.equals(payZfdd00.getDdzt00())){
            throw new CustomException(HttpStatus.BAD_REQUEST, "订单状态不正确");
        }


        PayOrderCloseRequest request = new PayOrderCloseRequest();
        PayOrderCloseReqModel model = new PayOrderCloseReqModel();
        request.setBizModel(model);

        model.setAppId(payZfdd00.getFwqdid());
        model.setPayOrderId(payZfdd00.getXtddh0());

        PayFwqd00 mchApp = jdbcGateway.selectOne("pay.fwqd00.selectByPrimaryKey", payZfdd00.getFwqdid());

        JeepayClient jeepayClient = new JeepayClient(applicationProperty.getPaySiteBackUrl(), mchApp.getQdmy00());

        try {
            PayOrderCloseResponse response = jeepayClient.execute(request);
            if(response.getCode() != 0){
                throw new CustomException(HttpStatus.BAD_REQUEST, response.getMsg());
            }
        } catch (JeepayException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        return "";
    }

}
