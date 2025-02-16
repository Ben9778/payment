package com.ylz.yx.pay.order.pay.impl;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.JeepayClient;
import com.jeequan.jeepay.exception.JeepayException;
import com.jeequan.jeepay.model.RefundOrderCreateReqModel;
import com.jeequan.jeepay.model.RefundOrderCreateResModel;
import com.jeequan.jeepay.request.RefundOrderCreateRequest;
import com.jeequan.jeepay.response.RefundOrderCreateResponse;
import com.ylz.core.logging.Logger;
import com.ylz.svc.apic.ApiExpose;
import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.svc.data.dao.support.PageModel;
import com.ylz.svc.web.HttpResponse;
import com.ylz.yx.pay.config.ApplicationProperty;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.entity.PayFwqd00;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.core.exception.BizException;
import com.ylz.yx.pay.core.exception.CustomException;
import com.ylz.yx.pay.core.exception.HttpStatus;
import com.ylz.yx.pay.order.pay.PayService;
import com.ylz.yx.pay.order.pay.model.PayZfdd00Param;
import com.ylz.yx.pay.order.pay.model.PayZfdd00RS;
import com.ylz.yx.pay.order.pay.model.SelectParam;
import com.ylz.yx.pay.payment.service.ChannelOrderReissueService;
import com.ylz.yx.pay.system.approval.ApprovalService;
import com.ylz.yx.pay.system.approval.model.PaySpbzb0;
import com.ylz.yx.pay.system.approval.model.PaySpjlb0;
import com.ylz.yx.pay.system.approval.model.XtSpsz00;
import com.ylz.yx.pay.system.dict.impl.DictServiceImpl;
import com.ylz.yx.pay.system.dict.vo.GlobalDict;
import com.ylz.yx.pay.annotation.BusinessLog;
import com.ylz.yx.pay.annotation.BusinessType;
import com.ylz.yx.pay.utils.*;
import com.ylz.yx.pay.utils.excel.ExcelData;
import com.ylz.yx.pay.utils.excel.ExcelUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.JDOMException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@ApiExpose("payorder")
@Service("payService")
public class PayServiceImpl implements PayService {

    private static final Logger log = new Logger(PayServiceImpl.class.getName());

    @Autowired
    private ChannelOrderReissueService channelOrderReissueService;
    @Autowired
    private JdbcGateway jdbcGateway;
    @Resource
    private ApplicationProperty applicationProperty;
    @Autowired
    private ApprovalService approvalService;

    @Override
    @ApiExpose("queryList")
    public Map<String, Object> queryList(SelectParam param) {
        if (param.getPageSize() == null || param.getPageIndex() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "传入的参数不完整！");
        }

        int pageSize = Integer.valueOf(param.getPageSize());
        int pageIndex = Integer.valueOf(param.getPageIndex());
        PageModel pageModel = new PageModel(pageIndex, pageSize);

        List<PayZfdd00RS> payZfdd00List = jdbcGateway.selectListByPage("pay.zfdd00.selectList", param, pageModel);

        // 得到所有支付渠道
        Map<String, String> zfqdmcMap = new HashMap<>();
        GlobalDict payChannel = DictServiceImpl.getDictByKey000("pay_channel");
        if (payChannel != null) {
            for (GlobalDict dict : payChannel.getChildren().values()) {
                zfqdmcMap.put(dict.getKey000(), dict.getZdmc00());
            }
            for (PayZfdd00RS order : payZfdd00List) {
                // 存入支付渠道名称
                if (StringUtils.isNotEmpty(zfqdmcMap.get(order.getZfqd00()))) {
                    order.setZfqdmc(zfqdmcMap.get(order.getZfqd00()));
                } else {
                    order.setZfqdmc(order.getZfqd00());
                }
            }
        }
        // 得到所有支付方式
        Map<String, String> zffsmcMap = new HashMap<>();
        if (payChannel != null) {
            for (GlobalDict globalDict : payChannel.getChildren().values()) {
                for (GlobalDict dict : globalDict.getChildren().values()) {
                    zffsmcMap.put(dict.getKey000(), dict.getZdmc00());
                }
            }
            for (PayZfdd00RS order : payZfdd00List) {
                // 存入支付方式名称
                if (StringUtils.isNotEmpty(zffsmcMap.get(order.getZffs00()))) {
                    order.setZffsmc(zffsmcMap.get(order.getZffs00()));
                } else {
                    if (CS.PAY_WAY_CODE.QR_CASHIER.equals(order.getZffs00())) {
                        order.setZffsmc("聚合支付");
                    } else if (CS.PAY_WAY_CODE.AUTO_BAR.equals(order.getZffs00())) {
                        order.setZffsmc("聚合条码付");
                    } else {
                        order.setZffsmc(order.getZffs00());
                    }
                }
            }
        }
        // 得到所有订单类型
        Map<String, String> ddlxmcMap = new HashMap<>();
        GlobalDict orderType = DictServiceImpl.getDictByKey000("order_type");
        if (orderType != null) {
            for (GlobalDict dict : orderType.getChildren().values()) {
                ddlxmcMap.put(dict.getKey000(), dict.getZdmc00());
            }
            for (PayZfdd00RS order : payZfdd00List) {
                // 存入订单类型名称
                if (StringUtils.isNotEmpty(ddlxmcMap.get(order.getDdlx00()))) {
                    order.setDdlxmc(ddlxmcMap.get(order.getDdlx00()));
                } else {
                    order.setDdlxmc(order.getDdlx00());
                }
            }
        }
        // 得到所有订单状态
        Map<String, String> ddzt00Map = new HashMap<>();
        GlobalDict orderState = DictServiceImpl.getDictByKey000("order_state");
        if (orderState != null) {
            for (GlobalDict dict : orderState.getChildren().values()) {
                ddzt00Map.put(dict.getKey000(), dict.getZdmc00());
            }
            for (PayZfdd00RS order : payZfdd00List) {
                // 存入订单状态名称
                if (StringUtils.isNotEmpty(ddzt00Map.get(order.getDdzt00()))) {
                    order.setDdztmc(ddzt00Map.get(order.getDdzt00()));
                } else {
                    order.setDdztmc(order.getDdzt00());
                }
            }
        }
        for (PayZfdd00RS order : payZfdd00List) {
            if(order.getDdcjsj().contains(DateKit.DateFormat(new Date(), "yyyy-MM-dd"))){
                order.setSfxsan("1");//显示
            } else {
                order.setSfxsan("0");//不显示
            }
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", payZfdd00List);
        resultMap.put("total", jdbcGateway.selectOne("pay.zfdd00.selectCountList", param));
        return resultMap;
    }

    @Override
    @BusinessLog(title = "充值订单列表", businessType = BusinessType.EXPORT)
    public String expList(SelectParam param, HttpServletResponse response) {
        List<PayZfdd00RS> list = jdbcGateway.selectList("pay.zfdd00.selectList", param);

        if (CollectionUtils.isEmpty(list)) {
            return "暂无数据";
        }

        // 得到所有支付渠道
        Map<String, String> zfqdmcMap = new HashMap<>();
        GlobalDict payChannel = DictServiceImpl.getDictByKey000("pay_channel");
        if (payChannel != null) {
            for (GlobalDict dict : payChannel.getChildren().values()) {
                zfqdmcMap.put(dict.getKey000(), dict.getZdmc00());
            }
            for (PayZfdd00RS order : list) {
                // 存入支付渠道名称
                if (StringUtils.isNotEmpty(zfqdmcMap.get(order.getZfqd00()))) {
                    order.setZfqdmc(zfqdmcMap.get(order.getZfqd00()));
                } else {
                    order.setZfqdmc(order.getZfqd00());
                }
            }
        }
        // 得到所有订单类型
        Map<String, String> ddlxmcMap = new HashMap<>();
        GlobalDict orderType = DictServiceImpl.getDictByKey000("order_type");
        if (orderType != null) {
            for (GlobalDict dict : orderType.getChildren().values()) {
                ddlxmcMap.put(dict.getKey000(), dict.getZdmc00());
            }
            for (PayZfdd00RS order : list) {
                // 存入订单类型名称
                if (StringUtils.isNotEmpty(ddlxmcMap.get(order.getDdlx00()))) {
                    order.setDdlxmc(ddlxmcMap.get(order.getDdlx00()));
                } else {
                    order.setDdlxmc(order.getDdlx00());
                }
            }
        }
        // 得到所有订单状态
        Map<String, String> ddzt00Map = new HashMap<>();
        GlobalDict orderState = DictServiceImpl.getDictByKey000("order_state");
        if (orderState != null) {
            for (GlobalDict dict : orderState.getChildren().values()) {
                ddzt00Map.put(dict.getKey000(), dict.getZdmc00());
            }
            for (PayZfdd00RS order : list) {
                // 存入订单状态名称
                if (StringUtils.isNotEmpty(ddzt00Map.get(order.getDdzt00()))) {
                    order.setDdztmc(ddzt00Map.get(order.getDdzt00()));
                } else {
                    order.setDdztmc(order.getDdzt00());
                }
            }
        }

        ExcelData excelData = new ExcelData();
        List<String> titleList = new ArrayList();
        titleList.add("系统订单号");
        titleList.add("订单类型");
        titleList.add("患者姓名");
        titleList.add("就诊卡号");
        titleList.add("服务渠道");
        titleList.add("支付渠道");
        titleList.add("支付金额");
        titleList.add("订单交易状态");
        titleList.add("订单创建时间");
        excelData.setTitles(titleList);
        List<List<Object>> rows = new ArrayList();
        for (PayZfdd00RS bean : list) {
            List<Object> row = new ArrayList();
            row.add(bean.getXtddh0());
            row.add(bean.getDdlxmc());
            row.add(bean.getHzxm00());
            row.add(bean.getKhzyh0());
            row.add(bean.getFwqdmc());
            row.add(bean.getZfqdmc());
            row.add(bean.getZfje00() + "元");
            row.add(bean.getDdztmc());
            row.add(bean.getDdcjsj());
            rows.add(row);
        }
        excelData.setRows(rows);
        try {
            ExcelUtils.exportExcel(response, "充值订单列表", excelData);
        } catch (Exception e) {

        }

        return null;
    }

    @Override
    @ApiExpose("orderDetail")
    public Object orderDetail(SelectParam param) {
        PayZfdd00RS order = jdbcGateway.selectOne("pay.zfdd00.selectById0000", param.getId0000());

        // 得到所有支付渠道
        Map<String, String> zfqdmcMap = new HashMap<>();
        GlobalDict payChannel = DictServiceImpl.getDictByKey000("pay_channel");
        if (payChannel != null) {
            for (GlobalDict dict : payChannel.getChildren().values()) {
                zfqdmcMap.put(dict.getKey000(), dict.getZdmc00());
            }
            // 存入支付渠道名称
            if (StringUtils.isNotEmpty(zfqdmcMap.get(order.getZfqd00()))) {
                order.setZfqdmc(zfqdmcMap.get(order.getZfqd00()));
            } else {
                order.setZfqdmc(order.getZfqd00());
            }
        }
        // 得到所有支付方式
        Map<String, String> zffsmcMap = new HashMap<>();
        if (payChannel != null) {
            for (GlobalDict globalDict : payChannel.getChildren().values()) {
                for (GlobalDict dict : globalDict.getChildren().values()) {
                    zffsmcMap.put(dict.getKey000(), dict.getZdmc00());
                }
            }
            // 存入支付方式名称
            if (StringUtils.isNotEmpty(zffsmcMap.get(order.getZffs00()))) {
                order.setZffsmc(zffsmcMap.get(order.getZffs00()));
            } else {
                if (CS.PAY_WAY_CODE.QR_CASHIER.equals(order.getZffs00())) {
                    order.setZffsmc("聚合支付");
                } else if (CS.PAY_WAY_CODE.AUTO_BAR.equals(order.getZffs00())) {
                    order.setZffsmc("聚合条码付");
                } else {
                    order.setZffsmc(order.getZffs00());
                }
            }
        }
        // 得到所有订单类型
        Map<String, String> ddlxmcMap = new HashMap<>();
        GlobalDict orderType = DictServiceImpl.getDictByKey000("order_type");
        if (orderType != null) {
            for (GlobalDict dict : orderType.getChildren().values()) {
                ddlxmcMap.put(dict.getKey000(), dict.getZdmc00());
            }
            // 存入订单类型名称
            if (StringUtils.isNotEmpty(ddlxmcMap.get(order.getDdlx00()))) {
                order.setDdlxmc(ddlxmcMap.get(order.getDdlx00()));
            } else {
                order.setDdlxmc(order.getDdlx00());
            }
        }
        // 得到所有订单状态
        Map<String, String> ddzt00Map = new HashMap<>();
        GlobalDict orderState = DictServiceImpl.getDictByKey000("order_state");
        if (orderState != null) {
            for (GlobalDict dict : orderState.getChildren().values()) {
                ddzt00Map.put(dict.getKey000(), dict.getZdmc00());
            }
            // 存入订单状态名称
            if (StringUtils.isNotEmpty(ddzt00Map.get(order.getDdzt00()))) {
                order.setDdztmc(ddzt00Map.get(order.getDdzt00()));
            } else {
                order.setDdztmc(order.getDdzt00());
            }
        }

        // 根据主键ID获取审批记录表数据
        PaySpjlb0 paySpjlb0 = jdbcGateway.selectOne("pay.spjlb0.selectByGlddid", param.getId0000());
        if(paySpjlb0 != null){
            // 获取审批步骤表数据
            List<PaySpbzb0> paySpbzb0List = jdbcGateway.selectList("pay.spbzb0.selectByJlbid0", paySpjlb0.getId0000());
            order.setBzList(paySpbzb0List);

            // 获取审批配置
            XtSpsz00 xtSpsz00 = jdbcGateway.selectOne("system.spsz00.select");

            if("0".equals(xtSpsz00.getSpfs00())){
                // 一人审批即可通过
                List<PaySpbzb0> haveData = paySpbzb0List.stream().filter(haveEndData ->
                        haveEndData.getSpsj00() == null).collect(toList());
                Map<String, List<PaySpbzb0>> listMap = haveData.stream().collect(Collectors.groupingBy(PaySpbzb0::getSpbz00));
                order.setSybzs0(listMap.size());
                order.setSfxsan("0");
                // 获取当前流程审批步骤
                PaySpbzb0 paySpbzb0 = jdbcGateway.selectOne("pay.spbzb0.selectALLCount", paySpjlb0.getId0000());
                if(paySpbzb0 != null){
                    if(paySpbzb0.getSpr000().contains(param.getClr000())){
                        order.setSfxsan("1");
                        order.setDqbz00(paySpbzb0.getSpbz00());
                    }
                }
            } else {
                // 全员审批才可通过
                List<PaySpbzb0> haveData = paySpbzb0List.stream().filter(haveEndData ->
                        haveEndData.getSpsj00() == null).collect(toList());
                order.setSybzs0(haveData.size());
                order.setSfxsan("0");
                // 获取当前流程审批步骤
                PaySpbzb0 paySpbzb0 = jdbcGateway.selectOne("pay.spbzb0.selectALLCount", paySpjlb0.getId0000());
                if(paySpbzb0 != null){
                    if(paySpbzb0.getSpr000().contains(param.getClr000())){
                        order.setSfxsan("1");
                        order.setDqbz00(paySpbzb0.getSpbz00());
                    }
                }
            }
            // 获取流程设置
            JSONObject jsonObject = JSON.parseObject(xtSpsz00.getLcsz00());
            // 获取流程发起人
            String originator = jsonObject.getString("originator");
            // 判断当前操作者是否在流程发起人中
            if(originator.contains(param.getClr000())){
                order.setSqtkan("1");
            }
            order.setJlbid0(paySpjlb0.getId0000());
        }
        return order;
    }

    @Override
    @ApiExpose("refreshState")
    @BusinessLog(title = "充值订单列表", businessType = BusinessType.UPDATE)
    public void refreshState(SelectParam param) {
        PayZfdd00 payZfdd00 = jdbcGateway.selectOne("pay.zfdd00.selectByPrimaryKey", param.getId0000());
        channelOrderReissueService.processPayOrder(payZfdd00);
    }

    @Override
    @ApiExpose("orderRefund")
    @BusinessLog(title = "充值订单列表", businessType = BusinessType.UPDATE)
    public Map<String, Object> orderRefund(PayZfdd00Param param) {

        Map<String, Object> resultMap = new HashMap<>();

        ValidateUtils.Validate(param);

        String id0000 = param.getId0000();

        PayZfdd00 payZfdd00 = jdbcGateway.selectOne("pay.zfdd00.selectByPrimaryKey", id0000);
        if (payZfdd00 == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "订单不存在！");
        }

        if (!payZfdd00.getDdzt00().equals(PayZfdd00.STATE_SUCCESS)) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "订单状态不正确");
        }

        if (payZfdd00.getTkzje0() + payZfdd00.getZfje00() > payZfdd00.getZfje00()) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "退款金额超过订单可退款金额！");
        }
        // 是否判断HIS订单
        if (applicationProperty.getIsHisOrder()) {
            // 判断HIS是否存在该笔订单
            String czy000 = applicationProperty.getHisOperator();
            Map mapParam = new HashMap();
            // 门诊
            if ("01".equals(payZfdd00.getDdlx00())) {
                mapParam = HisCommonParam.SP_SF_TYJK_YJJ_JYCX(czy000, payZfdd00.getKhzyh0());
            }
            // 住院
            else if ("02".equals(payZfdd00.getDdlx00())) {
                mapParam = HisCommonParam.SP_TYJK_PAYMENT_DETAILS(czy000, payZfdd00.getKhzyh0());
            }
            log.info("HIS获取" + payZfdd00.getDdlx00() + "预交金记录入参—>param:" + mapParam.toString());
            JSONObject hisResultInfo = JSONObject.parseObject(HttpUtil.post(applicationProperty.getYlzCommonUrl(), mapParam));
            log.info("HIS获取" + payZfdd00.getDdlx00() + "预交金记录结果—>hisResultInfo:" + hisResultInfo.toString());
            try {
                JSONObject payload = XmlUtils.xml2Json(hisResultInfo.get("payload").toString()).getJSONObject("Response");
                if ("200".equals(payload.getString("status"))) {
                    log.info("His获取" + payZfdd00.getDdlx00() + "预交金记录成功" + payload.toJSONString());
                    JSONArray resultList = payload.getJSONArray("result");
                    Integer resultnum = payload.getInteger("resultnum");
                    boolean flag = false;
                    if (resultnum > 0) {
                        for (int i = 0; i < resultnum; i++) {
                            JSONObject list = resultList.getJSONObject(i);
                            if (payZfdd00.getXtddh0().equals(list.getString("jylsh0"))) {
                                flag = true;
                            }
                        }
                    }
                    if (flag) {
                        throw new CustomException(HttpStatus.BAD_REQUEST, "该笔订单his账户已充值成功，不存在差异账，请勿操作退款！");
                    }
                } else {
                    //log.info("HIS获取" + payZfdd00.getDdlx00() + "预交金记录失败，原因：" + payload.getString("msg"));
                    //return new HttpResponse(HttpStatus.BAD_REQUEST, "HIS获取" + payZfdd00.getDdlx00() + "预交金记录失败，原因：" + payload.getString("msg"));
                }
            } catch (IOException | JDOMException e) {
                log.info("HIS获取" + payZfdd00.getDdlx00() + "预交金记录失败，原因：" + e.getMessage());
                throw new CustomException(HttpStatus.BAD_REQUEST, "HIS获取" + payZfdd00.getDdlx00() + "预交金记录失败，原因：" + e.getMessage());
            }
        }
        // 是否需要走退款审批流程
        if(applicationProperty.getIsRefundApproval()){
            // 获取审批配置
            XtSpsz00 xtSpsz00 = jdbcGateway.selectOne("system.spsz00.select");
            if("01".contains(xtSpsz00.getYymk00())){
                // 获取流程设置
                JSONObject jsonObject = JSON.parseObject(xtSpsz00.getLcsz00());
                // 获取流程发起人
                String originator = jsonObject.getString("originator");
                // 判断当前操作者是否在流程发起人中
                if(!originator.contains(param.getCzyid0())){
                    throw new CustomException(HttpStatus.BAD_REQUEST, "暂无权限操作，请联系管理员！");
                }
                // 判断是否已发起退款审批流程
                PaySpjlb0 paySpjlb0 = jdbcGateway.selectOne("pay.spjlb0.selectByGlddid", id0000);
                if(paySpjlb0 != null && !"2".equals(paySpjlb0.getSpzt00())){
                    throw new CustomException(HttpStatus.BAD_REQUEST, "该订单已发起退款审批流程，请勿重复操作！");
                }
                //approvalService.insertData(jsonObject, id0000, param.getCzyid0(), param.getCzyxm0(), "支付订单退款申请", null);

                // 更新支付订单审批状态为审批中
                //PayZfdd00 updZfdd00 = new PayZfdd00();
                //updZfdd00.setSpzt00("1");
                //updZfdd00.setId0000(payZfdd00.getId0000());
                //jdbcGateway.update("pay.zfdd00.updateByPrimaryKeySelective", updZfdd00);
                resultMap.put("sfyxtk", "1"); //0：否；1：是
                return resultMap;
            }
        }
        Integer tksqCount = jdbcGateway.selectOne("pay.tkdd00.selectIngCount", payZfdd00.getXtddh0());
        if(tksqCount > 0){
            throw new BizException("支付订单已存在退款申请，请勿重复操作");
        }

        RefundOrderCreateRequest request = new RefundOrderCreateRequest();
        RefundOrderCreateReqModel model = new RefundOrderCreateReqModel();
        request.setBizModel(model);

        model.setAppId(payZfdd00.getFwqdid());
        model.setPayOrderId(payZfdd00.getXtddh0());
        model.setMchRefundNo(SeqKit.genMhoOrderId());
        model.setRefundAmount(payZfdd00.getZfje00());
        model.setRefundReason("单边账-原路退回");
        model.setChannelExtra(payZfdd00.getQdcs00());
        model.setOperatorId(param.getCzyid0());
        model.setOperatorName(param.getCzyxm0());

        PayFwqd00 mchApp = jdbcGateway.selectOne("pay.fwqd00.selectByPrimaryKey", payZfdd00.getFwqdid());

        JeepayClient jeepayClient = new JeepayClient(applicationProperty.getPaySiteBackUrl(), mchApp.getQdmy00());

        try {
            RefundOrderCreateResponse response = jeepayClient.execute(request);
            if (response.getCode() != 0) {
                throw new CustomException(HttpStatus.BAD_REQUEST, response.getMsg());
            } else {
                RefundOrderCreateResModel resModel = response.get();
                if (resModel != null) {
                    if (resModel.getState() == 1) {
                        throw new CustomException(HttpStatus.SUCCESS, "退款中！");
                    } else if (resModel.getState() == 3) {
                        throw new CustomException(HttpStatus.BAD_REQUEST, "退款失败！");
                    } else if (resModel.getState() == 2) {
                        throw new CustomException(HttpStatus.SUCCESS, "退款成功！");
                    }
                }
            }
        } catch (JeepayException e) {
            throw new CustomException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        return resultMap;
    }

    @Override
    //@ApiExpose("orderRefund")
    @BusinessLog(title = "充值订单列表", businessType = BusinessType.UPDATE)
    public HttpResponse applyRefund(PayZfdd00Param param) {
        ValidateUtils.Validate(param);

        String id0000 = param.getId0000();

        PayZfdd00 payZfdd00 = jdbcGateway.selectOne("pay.zfdd00.selectByPrimaryKey", id0000);
        if (payZfdd00 == null) {
            return new HttpResponse(HttpStatus.BAD_REQUEST, "订单不存在！");
        }

        if (!payZfdd00.getDdzt00().equals(PayZfdd00.STATE_SUCCESS)) {
            return new HttpResponse(HttpStatus.BAD_REQUEST, "订单状态不正确");
        }

        if (payZfdd00.getTkzje0() + payZfdd00.getZfje00() > payZfdd00.getZfje00()) {
            return new HttpResponse(HttpStatus.BAD_REQUEST, "退款金额超过订单可退款金额！");
        }

        // 是否需要走退款审批流程
        if(applicationProperty.getIsRefundApproval()){
            // 获取审批配置
            XtSpsz00 xtSpsz00 = jdbcGateway.selectOne("system.spsz00.select");
            if("01".contains(xtSpsz00.getYymk00())){
                // 获取流程设置
                JSONObject jsonObject = JSON.parseObject(xtSpsz00.getLcsz00());
                // 获取流程发起人
                String originator = jsonObject.getString("originator");
                // 判断当前操作者是否在流程发起人中
                if(!originator.contains(param.getCzyid0())){
                    return new HttpResponse(HttpStatus.BAD_REQUEST, "暂无权限操作，请联系管理员！");
                }
                String jlbid0 = "";
                // 判断是否已发起退款审批流程
                PaySpjlb0 paySpjlb0 = jdbcGateway.selectOne("pay.spjlb0.selectByGlddid", id0000);
                if(paySpjlb0 != null && !"2".equals(paySpjlb0.getSpzt00())){
                    return new HttpResponse(HttpStatus.BAD_REQUEST, "该订单已发起退款审批流程，请勿重复操作！");
                }
                if(paySpjlb0 != null){
                    jlbid0 = paySpjlb0.getId0000();
                }
                approvalService.insertData(xtSpsz00, id0000, param.getCzyid0(), param.getCzyxm0(), "支付订单退款申请", param.getTkyy00(), param.getTpfj00(), jlbid0);

                // 更新支付订单审批状态为审批中
                PayZfdd00 updZfdd00 = new PayZfdd00();
                updZfdd00.setSpzt00("1");
                updZfdd00.setId0000(payZfdd00.getId0000());
                jdbcGateway.update("pay.zfdd00.updateByPrimaryKeySelective", updZfdd00);
            }
        }

        return new HttpResponse();
    }
}
