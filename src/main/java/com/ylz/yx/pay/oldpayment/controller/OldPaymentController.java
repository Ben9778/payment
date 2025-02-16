package com.ylz.yx.pay.oldpayment.controller;

import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.JeepayClient;
import com.jeequan.jeepay.exception.JeepayException;
import com.jeequan.jeepay.model.*;
import com.jeequan.jeepay.request.PayOrderCloseRequest;
import com.jeequan.jeepay.request.PayOrderCreateRequest;
import com.jeequan.jeepay.request.RefundOrderCreateRequest;
import com.jeequan.jeepay.request.RefundOrderQueryRequest;
import com.jeequan.jeepay.response.PayOrderCloseResponse;
import com.jeequan.jeepay.response.PayOrderCreateResponse;
import com.jeequan.jeepay.response.RefundOrderCreateResponse;
import com.jeequan.jeepay.response.RefundOrderQueryResponse;
import com.ylz.core.logging.Logger;
import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.yx.pay.config.ApplicationProperty;
import com.ylz.yx.pay.core.entity.PayFwqd00;
import com.ylz.yx.pay.core.entity.PayTkdd00;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.oldpayment.model.Refund;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import com.ylz.yx.pay.payment.service.ChannelOrderReissueService;
import com.ylz.yx.pay.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
public class OldPaymentController {

    private final Logger logger = new Logger("pay", "globalException", OldPaymentController.class.getName());

    @Autowired
    private JdbcGateway jdbcGateway;
    @Resource
    private ApplicationProperty applicationProperty;
    @Autowired
    private ChannelOrderReissueService channelOrderReissueService;

    /**
     * 条码付
     **/
    @RequestMapping("/ylzUP/zhifu/pay")
    public Map<String, Object> pay(HttpServletRequest request) throws Exception {
        logger.info("条码付请求：" + request.getRequestURL() + "?" + MapUtils.getMapToString(request.getParameterMap()));

        String authCode = request.getParameter("authCode");
        String password = request.getParameter("password");
        String userid = request.getParameter("userid");
        String channel_type = request.getParameter("channel_type");
        String account_type = request.getParameter("account_type");
        String id_card = request.getParameter("id_card");
        String p_name = request.getParameter("p_name");
        String card_no = request.getParameter("card_no");
        String operator_id = request.getParameter("operator_id");
        String operator_name = request.getParameter("operator_name");
        String WIDsubject = "手机支付"+request.getParameter("WIDsubject");
        String wayCode = "";
        if (channel_type.equals("2")) {
            authCode = getJieMiAuthCode(authCode, userid, "2");
            wayCode = "ALI_BAR";
        } else if (channel_type.equals("3")) {
            authCode = getJieMiAuthCode(authCode, userid, "3");
            wayCode = "WX_BAR";
        }
        Map<String, Object> map2 = new HashMap<String, Object>();

        String Version = "";
        if (request.getParameter("Version") != null) {
            Version = request.getParameter("Version").toString();
            map2.put("Version", Version);
        }
        //(必填)调用系统流水号
        String system_id = request.getParameter("system_id");
        if (account_type == null || "".equals(account_type) || p_name == null || "".equals(p_name) || card_no == null || "".equals(card_no)) {
            map2.put("result", "fail");
            map2.put("faildes", "门诊/住院账户类型或者病人姓名或者卡号不能为空!");
            map2.put("id", null);
            map2.put("system_id", system_id);
            map2.put("channel_id", null);
            map2.put("channel_type", 2);
            //his充值状态，待充值
            map2.put("his_status", 2);
            map2.put("status", 0);
            logger.info("zyx:pay:" + account_type + ":" + p_name + ":" + card_no + ":门诊/住院账户类型或者病人姓名或者卡号为空");
            return map2;
        }
        //付款金额，必填
        String total_amount = request.getParameter("amount");
        //操作员ID
        PayFwqd00 mchApp = jdbcGateway.selectOne("pay.fwqd00.selectByPrimaryKey", userid);
        String user_key = mchApp.getQdmy00();
        String MD5password = MD5Util.md5(user_key + system_id + total_amount);
        if (password.equals(MD5password)) {
            PayOrderCreateRequest orderCreateRequest = new PayOrderCreateRequest();
            PayOrderCreateReqModel orderCreateReqModel = new PayOrderCreateReqModel();
            orderCreateRequest.setBizModel(orderCreateReqModel);

            orderCreateReqModel.setAppId(userid);
            orderCreateReqModel.setMchOrderNo(system_id);
            orderCreateReqModel.setWayCode(wayCode);
            orderCreateReqModel.setAmount(BigDecimal.valueOf(Double.valueOf(total_amount)).multiply(new BigDecimal(100)).longValue());
            orderCreateReqModel.setSubject(WIDsubject);

            orderCreateReqModel.setUserName(p_name);
            orderCreateReqModel.setIdCard(id_card);
            orderCreateReqModel.setCardNo(card_no);
            orderCreateReqModel.setOrderType(account_type);
            orderCreateReqModel.setOperatorId(operator_id);
            orderCreateReqModel.setOperatorName(operator_name);

            //设置扩展参数
            JSONObject extParams = new JSONObject();
            if (StringUtils.isNotEmpty(authCode)) {
                extParams.put("authCode", authCode);
                if("WX_BAR".equals(wayCode)){
                    extParams.put("clientIp", PayKit.getIpAddr(request));
                }
            }
            orderCreateReqModel.setChannelExtra(extParams.toString());

            JeepayClient jeepayClient = new JeepayClient(applicationProperty.getPaySiteBackUrl(), mchApp.getQdmy00());
            try {
                PayOrderCreateResponse orderCreateResponse = jeepayClient.execute(orderCreateRequest);
                if (orderCreateResponse.getCode() != 0) {
                    Map<String, Object> resultMap = new HashMap<String, Object>();
                    PayOrderCreateResModel resModel = orderCreateResponse.get();
                    resultMap.put("result", "fail");
                    resultMap.put("faildes", "支付失败!!!");
                    resultMap.put("id", resModel.getPayOrderId());
                    resultMap.put("system_id", system_id);
                    //map2.put("channel_id", result.getResponse().getTradeNo());
                    resultMap.put("channel_id", "");
                    resultMap.put("channel_type", 2);
                    resultMap.put("his_status", 2);//
                    resultMap.put("status", 0);
                    return resultMap;
                } else {
                    Map<String, Object> resultMap = new HashMap<String, Object>();
                    PayOrderCreateResModel resModel = orderCreateResponse.get();
                    if(resModel.getOrderState() == 1){
                        int querycount = 7;
                        boolean booleanquery = false;
                        Thread.sleep(4000);
                        while(querycount>0){
                            Thread.sleep(3000);
                            PayZfdd00 payZfdd00 = jdbcGateway.selectOne("pay.zfdd00.selectByXtddh0", resModel.getPayOrderId());
                            ChannelRetMsg channelRetMsg = channelOrderReissueService.processPayOrder(payZfdd00);
                            // 查询成功
                            if(channelRetMsg.getChannelState() == ChannelRetMsg.ChannelState.CONFIRM_SUCCESS) {
                                querycount = 0;
                                booleanquery = true;
                                resultMap.put("result", "success");
                                resultMap.put("faildes", "");
                                resultMap.put("id", resModel.getPayOrderId());
                                resultMap.put("system_id", system_id);
                                resultMap.put("channel_id", "");
                                resultMap.put("channel_type", channel_type);
                                resultMap.put("his_status", 2);
                                resultMap.put("status", 1);
                            }else {
                                querycount = querycount - 1;
                                booleanquery = false;
                            }
                        }
                        if(booleanquery==false&&querycount==0){

                            PayOrderCloseRequest closeRequest = new PayOrderCloseRequest();
                            PayOrderCloseReqModel closeReqModel = new PayOrderCloseReqModel();
                            closeRequest.setBizModel(closeReqModel);

                            closeReqModel.setAppId(userid);
                            closeReqModel.setPayOrderId(resModel.getPayOrderId());

                            jeepayClient = new JeepayClient(applicationProperty.getPaySiteBackUrl(), mchApp.getQdmy00());
                            try {
                                PayOrderCloseResponse response = jeepayClient.execute(closeRequest);
                                if(response.getCode() != 0){
                                    resultMap.put("result", "fail");
                                    resultMap.put("faildes", "支付异常，患者输入密码超时，已尝试撤销订单");
                                    resultMap.put("id", resModel.getPayOrderId());
                                    resultMap.put("system_id", system_id);
                                    resultMap.put("channel_id", "");
                                    resultMap.put("channel_type", channel_type);
                                    resultMap.put("his_status", 2);
                                    resultMap.put("status", 6);
                                }
                            } catch (JeepayException e) {
                                resultMap.put("result", "unknown");
                                resultMap.put("faildes", "支付异常，患者输入密码超时，已尝试撤销订单");
                                resultMap.put("id", resModel.getPayOrderId());
                                resultMap.put("system_id", system_id);
                                resultMap.put("channel_id", "");
                                resultMap.put("channel_type", channel_type);
                                resultMap.put("his_status", 2);
                                resultMap.put("status", 2);
                            }
                            resultMap.put("result", "unknown");
                            resultMap.put("faildes", "支付异常，患者输入密码超时，已尝试撤销订单");
                            resultMap.put("id", resModel.getPayOrderId());
                            resultMap.put("system_id", system_id);
                            resultMap.put("channel_id", "");
                            resultMap.put("channel_type", channel_type);
                            resultMap.put("his_status", 2);
                            resultMap.put("status", 2);
                        }
                    } else if(resModel.getOrderState() == 2){
                        resultMap.put("result", "success");
                        resultMap.put("faildes", "");
                        resultMap.put("id", resModel.getPayOrderId());
                        resultMap.put("system_id", system_id);
                        resultMap.put("channel_id", "");
                        resultMap.put("channel_type", channel_type);
                        resultMap.put("his_status", 2);
                        resultMap.put("status", 1);
                    } else {
                        resultMap.put("result", "fail");
                        resultMap.put("faildes", "支付失败");
                        resultMap.put("id", resModel.getPayOrderId());
                        resultMap.put("system_id", system_id);
                        resultMap.put("channel_id", "");
                        resultMap.put("channel_type", channel_type);
                        resultMap.put("his_status", 2);
                        resultMap.put("status", 0);
                    }
                    return resultMap;
                }
            } catch (JeepayException e) {
                //throw new BizException(e.getMessage());
                Map<String, Object> resultMap = new HashMap<String, Object>();
                resultMap.put("result", "fail");
                resultMap.put("faildes", "系统异常!");
                resultMap.put("id", "");
                resultMap.put("system_id", system_id);
                resultMap.put("channel_id", "");
                resultMap.put("channel_type", 2);
                resultMap.put("his_status", 2);//
                resultMap.put("status", 0);
                return resultMap;
            }
        } else {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap = MapTool.setFailResult(resultMap, "数据传输异常，交易信息可能被篡改!");
            map2.put("id", "");
            map2.put("system_id", system_id);
            map2.put("channel_id", "");
            map2.put("channel_type", 2);
            map2.put("his_status", 2);//
            map2.put("status", 0);
            return resultMap;
        }
    }

    /**
     * 统一退款接口
     *
     * @throws Exception
     */
    @RequestMapping("/ylzUP/pay/refund")
    public Map<String, Object> refund(Refund refund, HttpServletRequest request) throws Exception {
        logger.info("退款请求：" + request.getRequestURL() + "?" + MapUtils.getMapToString(request.getParameterMap()));

        Long tkje00 = BigDecimal.valueOf(Double.valueOf(refund.getRefund_free())).multiply(new BigDecimal(100)).longValue();

        PayZfdd00 payZfdd00 = jdbcGateway.selectOne("pay.zfdd00.selectByXtddh0", refund.getId());
        if (payZfdd00 == null) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap = MapTool.setFailResult(resultMap, "该订单不是本系统的订单!");
            return resultMap;
        }

        if (!payZfdd00.getDdzt00().equals(PayZfdd00.STATE_SUCCESS)) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap = MapTool.setFailResult(resultMap, "订单状态不正确!");
            return resultMap;
        }

        if (payZfdd00.getTkzje0() + tkje00 > payZfdd00.getZfje00()) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap = MapTool.setFailResult(resultMap, "退款金额超过订单可退款金额!");
            return resultMap;
        }

        // 操作员id
        String userid = refund.getUserid();
        if (userid == null || userid.equals("")) {
            userid = request.getParameter("userid");
            refund.setUserid(userid);
        }
        // 流水号
        String system_id = refund.getSystem_id();
        if (system_id == null || system_id.equals("")) {
            system_id = request.getParameter("system_id");
            refund.setSystem_id(system_id);
        }

        RefundOrderCreateRequest orderCreateRequest = new RefundOrderCreateRequest();
        RefundOrderCreateReqModel orderCreateReqModel = new RefundOrderCreateReqModel();
        orderCreateRequest.setBizModel(orderCreateReqModel);

        orderCreateReqModel.setAppId(userid);
        orderCreateReqModel.setPayOrderId(payZfdd00.getXtddh0());
        orderCreateReqModel.setMchRefundNo(refund.getSystem_id());
        orderCreateReqModel.setRefundAmount(tkje00);
        orderCreateReqModel.setRefundReason(refund.getRefund_reason());
        orderCreateReqModel.setOperatorId(refund.getOperator_id());
        orderCreateReqModel.setOperatorName(refund.getOperator_name());

        // 需要退款的金额
        String refund_amount = request.getParameter("refund_free");
        // 操作员ID
        PayFwqd00 mchApp = jdbcGateway.selectOne("pay.fwqd00.selectByPrimaryKey", userid);
        String user_key = mchApp.getQdmy00();
        String password = request.getParameter("password");
        String MD5password = MD5Util.md5(user_key + system_id + refund_amount);
        if (password.equals(MD5password)) {
            JeepayClient jeepayClient = new JeepayClient(applicationProperty.getPaySiteBackUrl(), mchApp.getQdmy00());
            try {
                RefundOrderCreateResponse response = jeepayClient.execute(orderCreateRequest);
                if (response.getCode() != 0) {
                    RefundOrderCreateResModel resModel = response.get();
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("result", "fail");
                    resultMap.put("faildes", response.getMsg());
                    resultMap.put("status", 0);//返回退款失败状态
                    resultMap.put("system_id", refund.getSystem_id());
                    resultMap.put("id", resModel.getRefundOrderId());
                    resultMap.put("his_status", "1");//his退款成功标记
                    Date date = new Date();
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String time = format.format(date);
                    resultMap.put("log_date", time);
                    String Version = "";
                    if (request.getParameter("Version") != null) {
                        Version = request.getParameter("Version").toString();
                        resultMap.put("Version", Version);
                    }
                    if (!Version.equals("") && resultMap.get("status").toString().equals("4")) {
                        resultMap.put("result", "unknown");
                    }
                    return resultMap;
                } else {
                    RefundOrderCreateResModel resModel = response.get();
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("result", "success");
                    resultMap.put("faildes", null);
                    resultMap.put("status", 1);//返回退款失败状态
                    resultMap.put("system_id", refund.getSystem_id());
                    resultMap.put("id", resModel.getRefundOrderId());
                    resultMap.put("his_status", "1");//his退款成功标记
                    Date date = new Date();
                    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String time = format.format(date);
                    resultMap.put("log_date", time);
                    String Version = "";
                    if (request.getParameter("Version") != null) {
                        Version = request.getParameter("Version").toString();
                        resultMap.put("Version", Version);
                    }
                    if (!Version.equals("") && resultMap.get("status").toString().equals("4")) {
                        resultMap.put("result", "unknown");
                    }
                    return resultMap;
                }

            } catch (JeepayException e) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("result", "fail");
                resultMap.put("faildes", "系统异常");
                resultMap.put("status", 4);//返回退款失败状态
                resultMap.put("his_status", "1");//his退款成功标记
                Date date = new Date();
                DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String time = format.format(date);
                resultMap.put("log_date", time);
                String Version = "";
                if (request.getParameter("Version") != null) {
                    Version = request.getParameter("Version").toString();
                    resultMap.put("Version", Version);
                }
                if (!Version.equals("") && resultMap.get("status").toString().equals("4")) {
                    resultMap.put("result", "unknown");
                }
                return resultMap;
            }
        } else {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap = MapTool.setFailResult(resultMap, "数据传输异常，退款信息可能被篡改!");
            resultMap.put("status", 0);
            String Version = "";
            if (request.getParameter("Version") != null) {
                Version = request.getParameter("Version").toString();
                resultMap.put("Version", Version);
            }
            return resultMap;
        }
    }

    /**
     * 撤销接口
     *
     * @param channel_type//渠道 2.支付宝3.微信4.银联
     * @param amount//订单金额
     * @param id//商家订单号
     * @return
     */
    @RequestMapping("/ylzUP/pay/cancel")
    public Map<String, Object> cancel(String channel_type, String amount, String id) {
        Map<String, Object> resultmap = new HashMap<String, Object>();

        PayZfdd00 payZfdd00 = jdbcGateway.selectOne("pay.zfdd00.selectByXtddh0", id);
        if (payZfdd00 == null) {
            resultmap.put("result", "fail");
            resultmap.put("faildes", "订单不存在");
            resultmap.put("channel_type", channel_type);
            resultmap.put("id", id);
            return resultmap;
        }

        if (!PayZfdd00.STATE_INIT.equals(payZfdd00.getDdzt00()) && !PayZfdd00.STATE_ING.equals(payZfdd00.getDdzt00())) {
            resultmap.put("result", "fail");
            resultmap.put("faildes", "订单状态不正确");
            resultmap.put("channel_type", channel_type);
            resultmap.put("id", id);
            return resultmap;
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
            if (response.getCode() != 0) {
                resultmap.put("result", "fail");
                resultmap.put("faildes", response.getMsg());
            } else {
                resultmap.put("result", "success");
            }
        } catch (JeepayException e) {
            resultmap.put("result", "fail");
            resultmap.put("faildes", e.getMessage());
        }
        resultmap.put("channel_type", channel_type);
        resultmap.put("id", id);
        return resultmap;
    }

    /**
     * 统一付款订单查询
     *
     * @throws UnsupportedEncodingException
     */
    @ResponseBody
    @RequestMapping("/ylzUP/pay/querystatus")
    public Map<String, Object> querystatus(HttpServletRequest request) throws UnsupportedEncodingException {
        logger.info("付款查询请求：" + request.getRequestURL() + "?" + MapUtils.getMapToString(request.getParameterMap()));
        Map<String, Object> resultMap = new HashMap<>();
        String Version = "";
        if (request.getParameter("Version") != null) {
            Version = request.getParameter("Version").toString();
            resultMap.put("Version", Version);
        }
        Map<String, Object> map = new HashMap<String, Object>();
        String system_id = request.getParameter("system_id");
        String userid = request.getParameter("userid");

        PayZfdd00 payZfdd00 = jdbcGateway.selectOne("pay.zfdd00.selectByFwddh0", system_id);

        if (payZfdd00 != null && system_id != null && !"".equals(system_id) && userid != null && !"".equals(userid)) {

            String ddzt00 = payZfdd00.getDdzt00();
            Integer status = null;
            if ("1".equals(ddzt00)) {
                status = 2;
            } else if ("2".equals(ddzt00)) {
                status = 1;
            } else if ("3".equals(ddzt00)) {
                status = 0;
            } else if ("4".equals(ddzt00)) {
                status = 6;
            } else if ("5".equals(ddzt00)) {
                status = 3;
            } else if ("6".equals(ddzt00)) {
                status = 6;
            }
            Integer his_status = null;
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String log_date = sdf.format(payZfdd00.getDdzfsj());
            resultMap.put("log_date", log_date);
            resultMap.put("status", status);
            resultMap.put("his_status", his_status);
            resultMap.put("id", payZfdd00.getXtddh0());
            resultMap.put("channel_id", payZfdd00.getZfddh0());
            String channel_type = "";
            if ("alipay".equals(payZfdd00.getZfqd00())) {
                channel_type = "2";
            } else if ("wxpay".equals(payZfdd00.getZfqd00())) {
                channel_type = "3";
            }
            resultMap.put("channel_type", channel_type);
            resultMap = MapTool.setSuccessResult(resultMap);
            if (!Version.equals("")) {
                if (status == 0) {
                    resultMap.put("result", "fail");
                } else if (status == 2) {
                    resultMap.put("result", "unknown");
                }
            }
        } else {
            resultMap = MapTool.setFailResult(resultMap, "订单不存在");
        }
        logger.info("system_id" + system_id + "|查询结果:" + resultMap);
        return resultMap;
    }

    /**
     * 统一退款订单查询
     *
     * @throws UnsupportedEncodingException
     */
    @RequestMapping("/ylzUP/pay/refund_querystatus")
    public Map<String, Object> refund_querystatus(String system_id, String userid, String out_request_no, Integer channel_type,
                                                  HttpServletRequest request) throws UnsupportedEncodingException {
        logger.info("退款查询请求：" + request.getRequestURL() + "?" + MapUtils.getMapToString(request.getParameterMap()));

        Map<String, Object> resultMap = new HashMap<String, Object>();

        String Version = "";
        if (request.getParameter("Version") != null) {
            Version = request.getParameter("Version").toString();
            resultMap.put("Version", Version);
        }


        PayTkdd00 payTkdd00 = jdbcGateway.selectOne("pay.tkdd00.selectByFwddh0", system_id);

        RefundOrderQueryRequest queryRequest = new RefundOrderQueryRequest();
        RefundOrderQueryReqModel queryReqModel = new RefundOrderQueryReqModel();
        queryRequest.setBizModel(queryReqModel);

        queryReqModel.setAppId(payTkdd00.getFwqdid());
        queryReqModel.setRefundOrderId(payTkdd00.getXtddh0());

        PayFwqd00 mchApp = jdbcGateway.selectOne("pay.fwqd00.selectByPrimaryKey", payTkdd00.getFwqdid());

        JeepayClient jeepayClient = new JeepayClient(applicationProperty.getPaySiteBackUrl(), mchApp.getQdmy00());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String log_date = sdf.format(payTkdd00.getTkcgsj());
        resultMap.put("log_date", log_date);
        try {
            RefundOrderQueryResponse response = jeepayClient.execute(queryRequest);
            if (response.getCode() != 0) {
                String sub_msg = null;
                if (response.getMsg() != null) {
                    sub_msg = response.getMsg();
                } else {
                    sub_msg = "订单状态异常";
                }
                resultMap.put("status", 0);
                resultMap = MapTool.setFailResult(resultMap, sub_msg);
            } else {
                RefundOrderQueryResModel queryResModel = response.get();
                if("1".equals(queryResModel.getState())){
                    resultMap.put("result","success");
                    resultMap.put("faildes","退款处理中！");
                    resultMap.put("status", 4);
                } else if("2".equals(queryResModel.getState())){
                    resultMap = MapTool.setSuccessResult(resultMap);
                    resultMap.put("status", 1);//退款成功
                } else {
                    resultMap = MapTool.setFailResult(resultMap, queryResModel.getErrMsg());
                    resultMap.put("status", 0);
                }
            }
        } catch (JeepayException e) {
            resultMap.put("result", "fail");
            resultMap.put("status", 0);
            resultMap.put("faildes", e.getMessage());
        }
        resultMap.put("his_status", "1");
        resultMap.put("out_trade_no", payTkdd00.getXtddh0());
        resultMap.put("trade_no", payTkdd00.getZfddh0());
        return resultMap;
    }

    public String getJieMiAuthCode(String authCode, String userid, String channel_type) {
        logger.info("进入条码解密：");
        PayFwqd00 mchApp = jdbcGateway.selectOne("pay.fwqd00.selectByPrimaryKey", userid);
        String user_key = mchApp.getQdmy00();
        DesString ds = new DesString();
        String jiemicode = ds.decrypt(authCode, user_key);
        if ("2".equals(channel_type) || "3".equals(channel_type)) {
            return jiemicode.substring(0, jiemicode.length() - 6);
        } else {
            return jiemicode.substring(0, jiemicode.length() - 5);
        }
    }
}
