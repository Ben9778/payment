package com.ylz.yx.pay.wristband.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.JeepayClient;
import com.jeequan.jeepay.exception.JeepayException;
import com.jeequan.jeepay.model.PayOrderCreateReqModel;
import com.jeequan.jeepay.model.RefundOrderCreateReqModel;
import com.jeequan.jeepay.request.PayOrderCreateRequest;
import com.jeequan.jeepay.request.RefundOrderCreateRequest;
import com.jeequan.jeepay.response.PayOrderCreateResponse;
import com.jeequan.jeepay.response.RefundOrderCreateResponse;
import com.ylz.core.logging.Logger;
import com.ylz.svc.apic.ApiExpose;
import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.yx.pay.config.ApplicationProperty;
import com.ylz.yx.pay.core.entity.PayFwqd00;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.core.exception.CustomException;
import com.ylz.yx.pay.core.exception.HttpStatus;
import com.ylz.yx.pay.utils.*;
import com.ylz.yx.pay.wristband.WristbandService;
import com.ylz.yx.pay.wristband.model.PayOrderParam;
import org.jdom2.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiExpose("wristband")
@Service("wristbandService")
public class WristbandServiceImpl implements WristbandService {

    private final Logger logger = new Logger("pay", "wristband", WristbandServiceImpl.class.getName());

    @Resource
    private ApplicationProperty applicationProperty;
    @Autowired
    private JdbcGateway jdbcGateway;

    /**
     * 腕带付-获取患者信息
     */
    @Override
    @ApiExpose("getPatientInfo")
    public Object getPatientInfo(PayOrderParam orderParam) {
        Map<String, Object> resultMap = new HashMap<>();
        Map param = HisCommonParam.SP_ZY_TYJK_GETBRJBXX(orderParam.getZyid00());
        logger.info("HIS获取住院病人信息入参—>param:" + param.toString());
        JSONObject hisResultInfo = JSONObject.parseObject(HttpUtil.post(applicationProperty.getYlzCommonUrl(), param, 4000));
        logger.info("HIS获取住院病人信息结果—>hisResultInfo:" + hisResultInfo.toString());
        String payload = hisResultInfo.getString("payload");
        JSONObject jsonObject = XmlToJsonUtils.documentToJSONObject(payload);
        /*JSONObject resp = null;
        try {
            resp = XmlUtils.xml2Json(payload);
        } catch (IOException | JDOMException e) {

        }*/
        if ("200".equals(hisResultInfo.getString("status"))) {
            JSONArray resultList = (JSONArray) jsonObject.get("result");
            JSONObject result = (JSONObject) resultList.get(0);
            resultMap.put("yymc00", applicationProperty.getMchName());
            resultMap.put("ryrq00", result.getString("ryrq00"));
            resultMap.put("dqksmc", result.getString("dqksmc"));
            resultMap.put("xming0", result.getString("xming0"));
            resultMap.put("xbiemc", result.getString("xbiemc"));
            resultMap.put("zyh000", result.getString("zyh000"));
            resultMap.put("rycwh0", result.getString("rycwh0"));
            resultMap.put("yjjye0", result.getString("yjjye0"));
        } else {
            throw new CustomException(HttpStatus.BAD_REQUEST, "HIS获取住院病人信息失败，原因：" + hisResultInfo.getString("msg"));
        }
        return resultMap;
    }

    /**
     * 腕带付-下单
     */
    @Override
    @ApiExpose("payOrder")
    public Object payOrder(PayOrderParam param) {
        PayOrderCreateRequest request = new PayOrderCreateRequest();
        PayOrderCreateReqModel model = new PayOrderCreateReqModel();
        request.setBizModel(model);

        // 腕带付服务渠道ID
        String fwqdid = "34D92AC67C8D4A25A9C9DF044CDCC4A5";

        model.setAppId(fwqdid);
        model.setMchOrderNo(SeqKit.genMhoOrderId());
        model.setWayCode("QR_CASHIER");
        String amountStr = param.getZfje00();  // 前端填写的为元,可以为小数点2位
        Long amountL = new BigDecimal(amountStr.trim()).multiply(new BigDecimal(100)).longValue(); // // 转成分
        model.setAmount(amountL);
        model.setSubject("腕带付预交金充值");

        model.setUserName(param.getBrxm00());
        model.setCardNo(param.getZyh000());
        model.setOrderType("02");
        model.setOperatorId("99998");
        model.setOperatorName("腕带付");

        //设置扩展参数
        JSONObject extParams = new JSONObject();
        model.setChannelExtra(extParams.toString());

        PayFwqd00 mchApp = jdbcGateway.selectOne("pay.fwqd00.selectByPrimaryKey", fwqdid);

        JeepayClient jeepayClient = new JeepayClient(applicationProperty.getPaySiteBackUrl(), mchApp.getQdmy00());
        try {
            PayOrderCreateResponse response = jeepayClient.execute(request);
            if (response.getCode() != 0) {
                throw new CustomException(HttpStatus.BAD_REQUEST, response.getMsg());
            }
            return response.get();
        } catch (JeepayException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * 腕带付-自动充值预交金
     */
    @Override
    public void payOrderRecharge(PayZfdd00 payZfdd00) {
        String cjbm00 = applicationProperty.getHisManufacturer();
        if ("YX".equals(cjbm00)) {
            // 调用预交金充值接口
            boolean state = YXRechargeZY(payZfdd00);
            if (!state) {
                // 调用退款接口
                //refundOrder(payZfdd00, "住院");
            }
        }
    }

    /**
     * 电子病历-获取待结算信息
     */
    @Override
    @ApiExpose("getSettleInfo")
    public Object getSettleInfo(PayOrderParam orderParam) {
        Map<String, Object> resultMap = new HashMap();
        List<Map> mapList = new ArrayList<>();
        String czy000 = applicationProperty.getHisOperator();
        // 门诊获取病人基本信息
        Map patiMap = HisCommonParam.SP_SF_TYJK_GETBRJBXX(orderParam.getBrid00());
        logger.info("HIS获取门诊病人基本信息入参—>param:" + patiMap.toString());
        JSONObject patiInfo = JSONObject.parseObject(HttpUtil.post(applicationProperty.getYlzCommonUrl(), patiMap, 4000));
        logger.info("HIS获取门诊病人基本信息结果—>patiInfo:" + patiInfo.toString());
        try {
            JSONObject patiPayload = XmlUtils.xml2Json(patiInfo.get("payload").toString()).getJSONObject("Response");
            if ("200".equals(patiPayload.getString("status"))) {
                JSONArray patiResultList = patiPayload.getJSONArray("result");
                JSONObject patiResult = patiResultList.getJSONObject(0);
                // 门诊线上获取待结算处方明细
                Map param = HisCommonParam.SP_SF_TYJK_ONLINE_JSMX(czy000, patiResult.getString("cardno"), orderParam.getBrid00(), orderParam.getGhh000());
                logger.info("HIS获取待结算信息入参—>param:" + param.toString());
                JSONObject hisResultInfo = JSONObject.parseObject(HttpUtil.post(applicationProperty.getYlzCommonUrl(), param, 4000));
                logger.info("HIS获取待结算信息结果—>hisResultInfo:" + hisResultInfo.toString());
                JSONObject payload = null;
                try {
                    payload = XmlUtils.xml2Json(hisResultInfo.get("payload").toString()).getJSONObject("Response");
                } catch (IOException | JDOMException e) {
                    resultMap.put("code", "1");
                    logger.info("His获取待结算信息失败，原因：" + e.getMessage());
                    //throw new CustomException(HttpStatus.BAD_REQUEST, "His获取待结算信息失败，原因：" + e.getMessage());
                    return resultMap;
                }
                if ("200".equals(payload.getString("status"))) {
                    logger.info("His获取待结算信息成功" + payload.toJSONString());
                    JSONArray resultList = payload.getJSONArray("result");
                    JSONObject result = resultList.getJSONObject(0);
                    resultMap.put("code", "0");
                    resultMap.put("yymc00", applicationProperty.getMchName());
                    resultMap.put("kh0000", patiResult.getString("cardno"));
                    resultMap.put("jzsj00", result.getString("begntime"));
                    resultMap.put("jzrxm0", result.getString("psn_name"));
                    resultMap.put("ghh000", orderParam.getGhh000());
                    resultMap.put("jzks00", result.getString("dept_name"));
                    resultMap.put("djsje0", result.getString("medfee_sumamt"));
                    resultMap.put("xtgzh0", result.getString("xtgzh0"));
                    JSONArray mxList = result.getJSONArray("mxlist");
                    Integer mxlistnum = result.getInteger("mxlistnum");

                    if (mxlistnum > 0) {
                        for (int i = 0; i < mxlistnum; i++){
                            JSONObject list = mxList.getJSONObject(i);
                            Map<String, Object> mxListMap = new HashMap();
                            mxListMap.put("xmmc00", list.getString("bke182")); // 原先取 memo，修改为 bke182
                            mxListMap.put("sl0000", list.getString("cnt"));
                            mxListMap.put("dj0000", String.format("%.2f", list.getDouble("pric")));
                            mxListMap.put("je0000", list.getString("det_item_fee_sumamt"));
                            mapList.add(mxListMap);
                        }
                    }
                    resultMap.put("mxList", mapList);
                } else {
                    logger.info("His获取待结算信息失败，原因：" + payload.getString("msg"));
                    resultMap.put("code", "2");
                    //throw new CustomException(HttpStatus.BAD_REQUEST, "His获取待结算信息失败，原因：" + payload.getString("msg"));
                }
            } else {
                logger.info("HIS获取门诊病人基本信息失败，原因：" + patiPayload.getString("msg"));
                resultMap.put("code", "1");
                //throw new CustomException(HttpStatus.BAD_REQUEST, "HIS获取门诊病人基本信息失败，原因：" + patiPayload.getString("msg"));
            }
        } catch (IOException | JDOMException e) {
            logger.info("HIS获取门诊病人基本信息失败，原因：" + e.getMessage());
            resultMap.put("code", "1");
            //throw new CustomException(HttpStatus.BAD_REQUEST, "HIS获取门诊病人基本信息失败，原因：" + e.getMessage());
        }

        return resultMap;
    }

    /**
     * 电子病历-下单
     */
    @Override
    @ApiExpose("MZPayOrder")
    public Object MZPayOrder(PayOrderParam param) {
        PayOrderCreateRequest request = new PayOrderCreateRequest();
        PayOrderCreateReqModel model = new PayOrderCreateReqModel();
        request.setBizModel(model);

        String fwqdid = "";
        String operatorId = "99997";
        String operatorName = "门诊电子病历";
        // 门诊电子病历服务渠道ID
        fwqdid = "5D8724E9ED494C5C988300D6B9EA561E";
        if ("palm".equals(param.getSsxt00())) {
            // 掌上医院操作员ID
            operatorId = "99996";
            // 掌上医院操作员姓名
            operatorName = "掌上医院";
            // 掌上医院服务渠道ID
            fwqdid = "5501332E7AA24B50B0607E5C43563019";
        }

        model.setAppId(fwqdid);
        model.setMchOrderNo(SeqKit.genMhoOrderId());
        model.setWayCode("QR_CASHIER");
        String amountStr = param.getZfje00();  // 前端填写的为元,可以为小数点2位
        Long amountL = new BigDecimal(amountStr.trim()).multiply(new BigDecimal(100)).longValue(); // // 转成分
        model.setAmount(amountL);
        model.setSubject("门诊费用结算");

        model.setUserName(param.getBrxm00());
        model.setCardNo(param.getKh0000());
        model.setOrderType("01");
        model.setOperatorId(operatorId);
        model.setOperatorName(operatorName);

        //设置扩展参数
        JSONObject extParams = new JSONObject();
        extParams.put("brid00", param.getBrid00());
        extParams.put("ghh000", param.getGhh000());
        extParams.put("xtgzh0", param.getXtgzh0());
        model.setExtParam(extParams.toString());

        PayFwqd00 mchApp = jdbcGateway.selectOne("pay.fwqd00.selectByPrimaryKey", fwqdid);

        JeepayClient jeepayClient = new JeepayClient(applicationProperty.getPaySiteBackUrl(), mchApp.getQdmy00());
        try {
            PayOrderCreateResponse response = jeepayClient.execute(request);
            if (response.getCode() != 0) {
                throw new CustomException(HttpStatus.BAD_REQUEST, response.getMsg());
            }
            return response.get();
        } catch (JeepayException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * 电子病历-充值预交金并结算
     */
    @Override
    public void payOrderRechargeAndSettle(PayZfdd00 payZfdd00) {
        String cjbm00 = applicationProperty.getHisManufacturer();
        if ("YX".equals(cjbm00)) {
            //调用线上结算接口
            boolean selletStatus = YXSettleConfirm(payZfdd00);
            if (!selletStatus) {
                // 调用退款接口
                //refundOrder(payZfdd00, "门诊");
            }
        }
    }

    /**
     * 医信-结算确认接口
     */
    private boolean YXSettleConfirm(PayZfdd00 payZfdd00) {
        JSONObject jsonObject = JSONObject.parseObject(payZfdd00.getKzcs00());
        String czy000 = applicationProperty.getHisOperator();
        String kh0000 = payZfdd00.getKhzyh0();
        String czje00 = AmountUtil.convertCent2Dollar(payZfdd00.getZfje00());

        String brid00 = jsonObject.getString("brid00");
        String ghh000 = jsonObject.getString("ghh000");
        String xtgzh0 = jsonObject.getString("xtgzh0");
        Map param = HisCommonParam.SP_SF_TYJK_ONLINE_JSQR(czy000, kh0000, brid00, ghh000, xtgzh0, czje00, payZfdd00.getZfqd00(), payZfdd00.getXtddh0());
        logger.info("HIS结算确认接口入参—>param:" + param.toString());
        JSONObject hisResultInfo = JSONObject.parseObject(HttpUtil.post(applicationProperty.getYlzCommonUrl(), param, 4000));
        logger.info("HIS结算确认接口结果—>hisResultInfo:" + hisResultInfo.toString());
        try {
            JSONObject payload = XmlUtils.xml2Json(hisResultInfo.get("payload").toString()).getJSONObject("Response");
            if ("200".equals(payload.getString("status"))) {
                logger.info("订单号：" + payZfdd00.getXtddh0() + "，His结算确认接口成功");
                return true;
            } else {
                logger.info("订单号：" + payZfdd00.getXtddh0() + "，His结算确认接口失败，原因：" + payload.getString("msg"));
                Map jsjgParam = HisCommonParam.SP_SF_TYJK_JSJGCX(czy000, kh0000, xtgzh0);
                logger.info("HIS获取门诊通用接口结算结果查询入参—>jsjgParam:" + jsjgParam.toString());
                JSONObject jsjgResultInfo = JSONObject.parseObject(HttpUtil.post(applicationProperty.getYlzCommonUrl(), jsjgParam, 4000));
                logger.info("HIS获取门诊通用接口结算结果查询结果—>jsjgResultInfo:" + jsjgResultInfo.toString());
                try {
                    JSONObject jsjgPayload = XmlUtils.xml2Json(jsjgResultInfo.get("payload").toString()).getJSONObject("Response");
                    if ("200".equals(jsjgPayload.getString("status"))) {
                        logger.info("订单号：" + payZfdd00.getXtddh0() + "，His结算接口结算结果成功");
                        return true;
                    } else {
                        logger.info("订单号：" + payZfdd00.getXtddh0() + "，His结算接口结算结果失败，原因：" + jsjgPayload.getString("msg"));
                        if(jsjgPayload.getString("msg").contains("未找到有效的数据!")){
                            // 门诊线上获取待结算处方明细
                            Map djsParam = HisCommonParam.SP_SF_TYJK_ONLINE_JSMX(czy000, kh0000, brid00, ghh000);
                            logger.info("HIS获取待结算信息入参—>djsParam:" + djsParam.toString());
                            JSONObject djsResultInfo = JSONObject.parseObject(HttpUtil.post(applicationProperty.getYlzCommonUrl(), djsParam, 4000));
                            logger.info("HIS获取待结算信息结果—>djsResultInfo:" + djsResultInfo.toString());
                            JSONObject djsPayload = null;
                            try {
                                djsPayload = XmlUtils.xml2Json(djsResultInfo.get("payload").toString()).getJSONObject("Response");
                            } catch (IOException | JDOMException e) {
                                logger.info("His获取待结算信息失败，原因：" + e.getMessage());
                                return false;
                            }
                            if ("200".equals(djsPayload.getString("status"))) {
                                logger.info("His获取待结算信息成功" + djsPayload.toJSONString());
                                JSONArray resultList = djsPayload.getJSONArray("result");
                                JSONObject result = resultList.getJSONObject(0);

                                String djsXtgzh0 = result.getString("xtgzh0");
                                Map jsParam = HisCommonParam.SP_SF_TYJK_ONLINE_JSQR(czy000, kh0000, brid00, ghh000, djsXtgzh0, czje00, payZfdd00.getZfqd00(), payZfdd00.getXtddh0());
                                logger.info("HIS结算确认接口入参—>jsParam:" + jsParam.toString());
                                JSONObject jsResultInfo = JSONObject.parseObject(HttpUtil.post(applicationProperty.getYlzCommonUrl(), jsParam, 4000));
                                logger.info("HIS结算确认接口结果—>jsResultInfo:" + jsResultInfo.toString());
                                try {
                                    JSONObject jsPayload = XmlUtils.xml2Json(jsResultInfo.get("payload").toString()).getJSONObject("Response");
                                    if ("200".equals(jsPayload.getString("status"))) {
                                        logger.info("订单号：" + payZfdd00.getXtddh0() + "，His结算确认接口成功");
                                        return true;
                                    } else {
                                        logger.info("订单号：" + payZfdd00.getXtddh0() + "，His结算确认接口失败，原因：" + jsPayload.getString("msg"));
                                        Map jsjgParamNew = HisCommonParam.SP_SF_TYJK_JSJGCX(czy000, kh0000, djsXtgzh0);
                                        logger.info("HIS获取门诊通用接口结算结果查询入参—>jsjgParamNew:" + jsjgParamNew.toString());
                                        JSONObject jsjgResultInfoNew = JSONObject.parseObject(HttpUtil.post(applicationProperty.getYlzCommonUrl(), jsjgParamNew, 4000));
                                        logger.info("HIS获取门诊通用接口结算结果查询结果—>jsjgResultInfoNew:" + jsjgResultInfoNew.toString());
                                        try {
                                            JSONObject jsjgPayloadNew = XmlUtils.xml2Json(jsjgResultInfoNew.get("payload").toString()).getJSONObject("Response");
                                            if ("200".equals(jsjgPayloadNew.getString("status"))) {
                                                logger.info("订单号：" + payZfdd00.getXtddh0() + "，His结算接口结算结果成功");
                                                return true;
                                            } else {
                                                logger.info("订单号：" + payZfdd00.getXtddh0() + "，His结算接口结算结果失败，原因：" + jsjgPayloadNew.getString("msg"));
                                                return false;
                                            }
                                        } catch (IOException | JDOMException e) {
                                            logger.info("HIS获取门诊通用接口结算结果查询结果失败，原因：" + e.getMessage());
                                            return false;
                                        }
                                    }
                                } catch (IOException | JDOMException e) {
                                    logger.info("HIS结算确认接口结果失败，原因：" + e.getMessage());
                                    return false;
                                }
                            }
                        }
                        return false;
                    }
                } catch (IOException | JDOMException e) {
                    logger.info("His获取门诊通用接口结算结果失败，原因：" + e.getMessage());
                    return false;
                }
            }
        } catch (Exception e) {
            logger.info("订单号：" + payZfdd00.getXtddh0() + "，His结算确认接口失败，原因：" + e.getMessage());
            return false;
        }
    }


    /**
     * 医信-住院预交金充值
     */
    private boolean YXRechargeZY(PayZfdd00 payZfdd00) {
        String czy000 = applicationProperty.getHisOperator();
        String czje00 = AmountUtil.convertCent2Dollar(payZfdd00.getZfje00());
        Map param = HisCommonParam.SP_ZY_TYJK_YJJCZ(payZfdd00.getKhzyh0(), payZfdd00.getHzxm00(), czy000, czje00, payZfdd00.getZfqd00(), payZfdd00.getXtddh0());
        logger.info("HIS住院预交金充值入参—>param:" + param.toString());
        JSONObject hisResultInfo = JSONObject.parseObject(HttpUtil.post(applicationProperty.getYlzCommonUrl(), param, 4000));
        logger.info("HIS住院预交金充值结果—>hisResultInfo:" + hisResultInfo.toString());
        try {
            JSONObject payload = XmlUtils.xml2Json(hisResultInfo.get("payload").toString()).getJSONObject("Response");
            if ("200".equals(payload.getString("status"))) {
                logger.info("订单号：" + payZfdd00.getXtddh0() + "，His住院预交金充值成功");
                return true;
            } else {
                logger.info("订单号：" + payZfdd00.getXtddh0() + "，His住院预交金充值失败，原因：" + payload.getString("msg"));
                return false;
            }
        } catch (Exception e) {
            logger.info("订单号：" + payZfdd00.getXtddh0() + "，His住院预交金充值失败，原因：" + e.getMessage());
            return false;
        }

    }

    /**
     * 医信-门诊预交金充值
     */
    private boolean YXRechargeMZ(PayZfdd00 payZfdd00) {
        String czy000 = applicationProperty.getHisOperator();
        String czje00 = AmountUtil.convertCent2Dollar(payZfdd00.getZfje00());
        Map param = HisCommonParam.SP_SF_TYJK_YJJCZ(payZfdd00.getKhzyh0(), payZfdd00.getHzxm00(), czy000, czje00, payZfdd00.getZfqd00(), payZfdd00.getXtddh0());
        logger.info("HIS门诊预交金充值入参—>param:" + param.toString());
        JSONObject hisResultInfo = JSONObject.parseObject(HttpUtil.post(applicationProperty.getYlzCommonUrl(), param, 4000));
        logger.info("HIS门诊预交金充值结果—>hisResultInfo:" + hisResultInfo.toString());
        try {
            JSONObject payload = XmlUtils.xml2Json(hisResultInfo.get("payload").toString()).getJSONObject("Response");
            if ("200".equals(payload.getString("status"))) {
                logger.info("订单号：" + payZfdd00.getXtddh0() + "，His门诊预交金充值成功");
                return true;
            } else {
                logger.info("订单号：" + payZfdd00.getXtddh0() + "，His门诊预交金充值失败，原因：" + payload.getString("msg"));
                return false;
            }
        } catch (IOException | JDOMException e) {
            logger.info("订单号：" + payZfdd00.getXtddh0() + "，His门诊预交金充值失败，原因：" + e.getMessage());
            return false;
        }

    }

    /**
     * 预交金退款
     */
    private void refundOrder(PayZfdd00 payZfdd00, String type) {
        RefundOrderCreateRequest request = new RefundOrderCreateRequest();
        RefundOrderCreateReqModel model = new RefundOrderCreateReqModel();
        request.setBizModel(model);

        model.setAppId(payZfdd00.getFwqdid());
        model.setPayOrderId(payZfdd00.getXtddh0());
        model.setMchRefundNo(SeqKit.genMhoOrderId());
        model.setRefundAmount(payZfdd00.getZfje00());
        model.setRefundReason(type + "-预交金充值失败退款");
        model.setOperatorId("99999");
        model.setOperatorName("系统");

        PayFwqd00 mchApp = jdbcGateway.selectOne("pay.fwqd00.selectByPrimaryKey", payZfdd00.getFwqdid());

        JeepayClient jeepayClient = new JeepayClient(applicationProperty.getPaySiteBackUrl(), mchApp.getQdmy00());

        try {
            RefundOrderCreateResponse response = jeepayClient.execute(request);
            if (response.getCode() != 0) {
                throw new CustomException(HttpStatus.BAD_REQUEST, response.getMsg());
            }
        } catch (JeepayException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

}
