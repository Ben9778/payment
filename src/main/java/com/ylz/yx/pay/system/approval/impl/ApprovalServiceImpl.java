package com.ylz.yx.pay.system.approval.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jeequan.jeepay.JeepayClient;
import com.jeequan.jeepay.exception.JeepayException;
import com.jeequan.jeepay.model.RefundOrderCreateReqModel;
import com.jeequan.jeepay.request.RefundOrderCreateRequest;
import com.jeequan.jeepay.response.RefundOrderCreateResponse;
import com.ylz.svc.apic.ApiExpose;
import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.svc.data.dao.support.PageModel;
import com.ylz.util.DateUtils;
import com.ylz.yx.pay.annotation.BusinessLog;
import com.ylz.yx.pay.annotation.BusinessType;
import com.ylz.yx.pay.config.ApplicationProperty;
import com.ylz.yx.pay.core.entity.PayFwqd00;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.core.exception.CustomException;
import com.ylz.yx.pay.core.exception.HttpStatus;
import com.ylz.yx.pay.order.pay.PayService;
import com.ylz.yx.pay.order.pay.model.PayZfdd00RS;
import com.ylz.yx.pay.order.pay.model.SelectParam;
import com.ylz.yx.pay.record.RecordService;
import com.ylz.yx.pay.record.model.PayDzmxb0;
import com.ylz.yx.pay.system.approval.ApprovalService;
import com.ylz.yx.pay.system.approval.model.*;
import com.ylz.yx.pay.utils.SeqKit;
import com.ylz.yx.pay.utils.StringKit;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiExpose("approval")
@Service("approvalService")
public class ApprovalServiceImpl implements ApprovalService {

    @Autowired
    private JdbcGateway jdbcGateway;

    @Resource
    private ApplicationProperty applicationProperty;

    @Autowired
    private PayService payService;

    @Autowired
    private RecordService recordService;

    /**
     * 我的审批列表
     */
    @Override
    @ApiExpose("queryList")
    public Map<String, Object> queryList(QueryParam param) {
        if (param.getPageSize() == null || param.getPageIndex() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "传入的参数不完整！");
        }

        int pageSize = Integer.valueOf(param.getPageSize());
        int pageIndex = Integer.valueOf(param.getPageIndex());
        PageModel pageModel = new PageModel(pageIndex, pageSize);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", jdbcGateway.selectListByPage("pay.spjlb0.selectAllList", param, pageModel));
        resultMap.put("total", jdbcGateway.selectOne("pay.spjlb0.selectCountAllList", param));

        return resultMap;
    }

    /**
     * 我的审批详情
     */
    @Override
    @ApiExpose("queryDetail")
    public Object queryDetail(QueryParam param) {
        if("支付订单退款申请".equals(param.getSplcmc())){
            SelectParam selectParam = new SelectParam();
            selectParam.setId0000(param.getGlddid());
            selectParam.setClr000(param.getSpr000());
            selectParam.setClrmc0(param.getSprxm0());
            PayZfdd00RS order = (PayZfdd00RS) payService.orderDetail(selectParam);
            order.setSfxsan("0");
            // 获取当前流程审批步骤
            PaySpbzb0 paySpbzb0 = jdbcGateway.selectOne("pay.spbzb0.selectALLCount", param);
            if(paySpbzb0 != null){
                if(paySpbzb0.getSpr000().contains(param.getSpr000())){
                    order.setSfxsan("1");
                    order.setDqbz00(paySpbzb0.getSpbz00());
                }
            }

            order.setJlbid0(param.getId0000());
            return order;
        }
        if("长款退款申请".equals(param.getSplcmc())){
            com.ylz.yx.pay.record.model.QueryParam queryParam = new com.ylz.yx.pay.record.model.QueryParam();
            queryParam.setId0000(param.getGlddid());
            queryParam.setClr000(param.getSpr000());
            queryParam.setClrmc0(param.getSprxm0());
            PayDzmxb0 order = (PayDzmxb0) recordService.pzjl(queryParam);
            order.setSfxsan("0");
            // 获取当前流程审批步骤
            PaySpbzb0 paySpbzb0 = jdbcGateway.selectOne("pay.spbzb0.selectALLCount", param);
            if(paySpbzb0 != null){
                if(paySpbzb0.getSpr000().contains(param.getSpr000())){
                    order.setSfxsan("1");
                    order.setDqbz00(paySpbzb0.getSpbz00());
                }
            }
            order.setJlbid0(param.getId0000());
            return order;
        }
        return null;
    }


    /**
     * 审批设置详情
     */
    @Override
    @ApiExpose("detail")
    public Object detail() {
        XtSpsz00 xtSpsz00 = jdbcGateway.selectOne("system.spsz00.select");
        List<PaySpjlb0> list = jdbcGateway.selectList("pay.spjlb0.select");
        if(list.size() > 0){
            xtSpsz00.setTswa00("当前存在历史审批流程正在进行中，暂不支持修改。");
        }
        return xtSpsz00;
    }

    /**
     * 审批设置保存
     */
    @Override
    @ApiExpose("upd")
    @BusinessLog(title = "审批设置", businessType = BusinessType.UPDATE, sqlName = "system.spsz00.select", sqlParam = "")
    public void upd(XtSpsz00 xtSpsz00) {
        List<PaySpjlb0> list = jdbcGateway.selectList("pay.spjlb0.select");
        if(list.size() > 0){
            throw new CustomException(HttpStatus.BAD_REQUEST, "当前存在历史审批流程正在进行中，暂不支持修改。");
        }

        jdbcGateway.update("system.spsz00.updateByPrimaryKeySelective", xtSpsz00);

        Map map = new HashMap();
        map.put("key000", "isRefundApproval");
        map.put("value0", "0".equals(xtSpsz00.getSfqy00()) ? "false" : "true");
        jdbcGateway.update("system.zd0000.updateByKey", map);

        //是否需要走退款审批流程 true/false
        String isRefundApproval = jdbcGateway.selectOne("system.zd0000.getSysParam", "isRefundApproval");
        applicationProperty.setIsRefundApproval(isRefundApproval.equals("true"));
    }

    @Override
    @ApiExpose("approval")
    public void approval(ApprovalParam approvalParam) {
        // 获取审批配置
        XtSpsz00 xtSpsz00 = jdbcGateway.selectOne("system.spsz00.select");
        // 我的审批
        if("0".equals(approvalParam.getLx0000())){
            PaySpbzb0 paySpbzb0 = new PaySpbzb0();
            paySpbzb0.setJlbid0(approvalParam.getId0000());
            paySpbzb0.setSpbz00(approvalParam.getSpbz00());
            paySpbzb0.setSpsj00(new Date());
            paySpbzb0.setSpr000(approvalParam.getSpr000());
            paySpbzb0.setSprxm0(approvalParam.getSprxm0());
            paySpbzb0.setSpjg00(approvalParam.getSpjg00());
            paySpbzb0.setSqyy00(approvalParam.getSqyy00());
            paySpbzb0.setTpfj00(approvalParam.getTpfj00());
            int count = jdbcGateway.update("pay.spbzb0.updateByJlbid0AndSpbz00", paySpbzb0);
            if(count > 0 && "0".equals(xtSpsz00.getSpfs00())){
                // 一人审批即可通过
                // 删除当前步骤剩余审批人
                jdbcGateway.delete("pay.spbzb0.deleteByJlbid0AndSpbz00", paySpbzb0);
            }
            if("审批通过".equals(approvalParam.getSpjg00())) {
                // 查询当前步骤是否还有审批中的记录
                PaySpbzb0 selSpbzb0 = new PaySpbzb0();
                selSpbzb0.setJlbid0(paySpbzb0.getJlbid0());
                selSpbzb0.setSpbz00(paySpbzb0.getSpbz00());
                List<PaySpbzb0> list = jdbcGateway.selectList("pay.spbzb0.selectByJlbid0AndSpbz00", selSpbzb0);
                if(list.size() == 0){
                    PaySpbzb0 updSpbzb0 = new PaySpbzb0();
                    updSpbzb0.setJlbid0(paySpbzb0.getJlbid0());
                    updSpbzb0.setSpbz00(String.valueOf(Integer.valueOf(paySpbzb0.getSpbz00()) + 1));
                    updSpbzb0.setSpjg00("审批中");
                    jdbcGateway.update("pay.spbzb0.updateSpjg00", updSpbzb0);
                }
            } else {
                // 更新审批记录表状态为拒绝
                PaySpjlb0 updSpjlb0 = new PaySpjlb0();
                updSpjlb0.setId0000(approvalParam.getId0000());
                updSpjlb0.setSpzt00("2");
                jdbcGateway.update("pay.spjlb0.updateByPrimaryKeySelective", updSpjlb0);
                // 删除剩余待审批和审批中的记录
                jdbcGateway.delete("pay.spbzb0.deleteByJlbid0", paySpbzb0.getJlbid0());
            }

            if(count > 0 && approvalParam.getSybzs0() == 1){
                if("审批通过".equals(approvalParam.getSpjg00())) {
                    PaySpjlb0 updSpjlb0 = new PaySpjlb0();
                    updSpjlb0.setId0000(approvalParam.getId0000());
                    updSpjlb0.setSpzt00("1");
                    updSpjlb0.setWcsj00(new Date());
                    jdbcGateway.update("pay.spjlb0.updateByPrimaryKeySelective", updSpjlb0);

                    if ("支付订单退款申请".equals(approvalParam.getSplcmc())) {
                        PaySpjlb0 paySpjlb0 = jdbcGateway.selectOne("pay.spjlb0.selectByPrimaryKey", approvalParam.getId0000());
                        PayZfdd00 payZfdd00 = jdbcGateway.selectOne("pay.zfdd00.selectByPrimaryKey", paySpjlb0.getGlddid());
                        if (payZfdd00 == null) {
                            throw new CustomException(HttpStatus.BAD_REQUEST, "订单不存在！");
                        }

                        if (!payZfdd00.getDdzt00().equals(PayZfdd00.STATE_SUCCESS)) {
                            throw new CustomException(HttpStatus.BAD_REQUEST, "订单状态不正确");
                        }

                        if (payZfdd00.getTkzje0() + payZfdd00.getZfje00() > payZfdd00.getZfje00()) {
                            throw new CustomException(HttpStatus.BAD_REQUEST, "退款金额超过订单可退款金额！");
                        }
                        RefundOrderCreateRequest request = new RefundOrderCreateRequest();
                        RefundOrderCreateReqModel model = new RefundOrderCreateReqModel();
                        request.setBizModel(model);

                        model.setAppId(payZfdd00.getFwqdid());
                        model.setPayOrderId(payZfdd00.getXtddh0());
                        model.setMchRefundNo(SeqKit.genMhoOrderId());
                        model.setRefundAmount(payZfdd00.getZfje00());
                        model.setRefundReason("单边账-原路退回");
                        model.setOperatorId(approvalParam.getSpr000());
                        model.setOperatorName(approvalParam.getSprxm0());

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
                        // 更新支付订单审批状态为审批结束
                        PayZfdd00 updZfdd00 = new PayZfdd00();
                        updZfdd00.setSpzt00("2");
                        updZfdd00.setId0000(payZfdd00.getId0000());
                        jdbcGateway.update("pay.zfdd00.updateByPrimaryKeySelective", updZfdd00);

                        PaySpbzb0 spbzb0 = new PaySpbzb0();
                        spbzb0.setJlbid0(approvalParam.getId0000());
                        spbzb0.setSpbz00("5");
                        spbzb0.setSpsj00(new Date());
                        spbzb0.setSpr000("99999");
                        spbzb0.setSprxm0("系统");
                        spbzb0.setSpjg00("系统自动退款");
                        jdbcGateway.insert("pay.spbzb0.insertSelective", spbzb0);

                    } else if ("长款退款申请".equals(approvalParam.getSplcmc())) {
                        PaySpjlb0 paySpjlb0 = jdbcGateway.selectOne("pay.spjlb0.selectByPrimaryKey", approvalParam.getId0000());
                        PayDzmxb0 payDzmxb0 = jdbcGateway.selectOne("pay.dzmxb0.selectByPrimaryKey", paySpjlb0.getGlddid());
                        // 发起退款
                        // 调用退款接口
                        String refundNo = SeqKit.genMhoOrderId();
                        PayZfdd00 payZfdd00 = jdbcGateway.selectOne("pay.zfdd00.selectByXtddh0", payDzmxb0.getXtddh0());
                        if (payZfdd00 == null) {
                            throw new CustomException(HttpStatus.BAD_REQUEST, "订单不存在！");
                        }

                        if (!payZfdd00.getDdzt00().equals(PayZfdd00.STATE_SUCCESS)) {
                            throw new CustomException(HttpStatus.BAD_REQUEST, "订单状态不正确");
                        }

                        if (payZfdd00.getTkzje0() + payDzmxb0.getPtje00() > payZfdd00.getZfje00()) {
                            throw new CustomException(HttpStatus.BAD_REQUEST, "退款金额超过订单可退款金额！");
                        }

                        RefundOrderCreateRequest request = new RefundOrderCreateRequest();
                        RefundOrderCreateReqModel model = new RefundOrderCreateReqModel();
                        request.setBizModel(model);

                        model.setAppId(payZfdd00.getFwqdid());
                        model.setPayOrderId(payZfdd00.getXtddh0());
                        model.setMchRefundNo(refundNo);
                        model.setRefundAmount(new Double(payDzmxb0.getPtje00() * 100).longValue());
                        model.setRefundReason("差异账退款");
                        model.setOperatorId(approvalParam.getSpr000());
                        model.setOperatorName(approvalParam.getSprxm0());

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

                        PaySpbzb0 spbzb0 = new PaySpbzb0();
                        spbzb0.setJlbid0(approvalParam.getId0000());
                        spbzb0.setSpbz00("5");
                        spbzb0.setSpsj00(new Date());
                        spbzb0.setSpr000("99999");
                        spbzb0.setSprxm0("系统");
                        spbzb0.setSpjg00("系统自动退款");
                        jdbcGateway.insert("pay.spbzb0.insertSelective", spbzb0);

                        // 更改差异账统计异常记录状态为已处理
                        payDzmxb0.setClzt00("1");
                        payDzmxb0.setClfs00("人工退款");
                        payDzmxb0.setClsj00(new Date());
                        payDzmxb0.setClr000(approvalParam.getSpr000());
                        payDzmxb0.setBz0000(refundNo);
                        jdbcGateway.update("pay.dzmxb0.updateByPrimaryKey", payDzmxb0);

                        String billDate = DateUtils.format(payDzmxb0.getXtdzsj(), "yyyyMMdd");
                        // 插入对账汇总数据
                        jdbcGateway.delete("pay.dzhzb0.delete", billDate);
                        jdbcGateway.insert("pay.dzhzb0.insert", billDate);
                        // 插入服务渠道对账汇总数据
                        jdbcGateway.delete("pay.fwdzhz.delete", billDate);
                        jdbcGateway.insert("pay.fwdzhz.insert", billDate);
                        // 插入支付渠道对账汇总数据
                        jdbcGateway.delete("pay.zfdzhz.delete", billDate);
                        jdbcGateway.insert("pay.zfdzhz.insert", billDate);
                    }
                } else {
                    PaySpjlb0 updSpjlb0 = new PaySpjlb0();
                    updSpjlb0.setId0000(approvalParam.getId0000());
                    updSpjlb0.setSpzt00("2");
                    jdbcGateway.update("pay.spjlb0.updateByPrimaryKeySelective", updSpjlb0);
                }
            }
        } else if("1".equals(approvalParam.getLx0000())){
            // 支付订单
            //PaySpjlb0 paySpjlb0 = jdbcGateway.selectOne("pay.spjlb0.selectByPrimaryKey", approvalParam.getId0000());
            PaySpbzb0 paySpbzb0 = new PaySpbzb0();
            paySpbzb0.setJlbid0(approvalParam.getId0000());
            paySpbzb0.setSpbz00(approvalParam.getSpbz00());
            paySpbzb0.setSpsj00(new Date());
            paySpbzb0.setSpr000(approvalParam.getSpr000());
            paySpbzb0.setSprxm0(approvalParam.getSprxm0());
            paySpbzb0.setSpjg00(approvalParam.getSpjg00());
            paySpbzb0.setSqyy00(approvalParam.getSqyy00());
            paySpbzb0.setTpfj00(approvalParam.getTpfj00());
            int count = jdbcGateway.update("pay.spbzb0.updateByJlbid0AndSpbz00", paySpbzb0);
            if(count > 0 && "0".equals(xtSpsz00.getSpfs00())){
                // 一人审批即可通过
                // 删除当前步骤剩余审批人
                jdbcGateway.delete("pay.spbzb0.deleteByJlbid0AndSpbz00", paySpbzb0);
            }

            if("审批通过".equals(approvalParam.getSpjg00())) {
                // 查询当前步骤是否还有审批中的记录
                PaySpbzb0 selSpbzb0 = new PaySpbzb0();
                selSpbzb0.setJlbid0(paySpbzb0.getJlbid0());
                selSpbzb0.setSpbz00(paySpbzb0.getSpbz00());
                List<PaySpbzb0> list = jdbcGateway.selectList("pay.spbzb0.selectByJlbid0AndSpbz00", selSpbzb0);
                if(list.size() == 0){
                    PaySpbzb0 updSpbzb0 = new PaySpbzb0();
                    updSpbzb0.setJlbid0(paySpbzb0.getJlbid0());
                    updSpbzb0.setSpbz00(String.valueOf(Integer.valueOf(paySpbzb0.getSpbz00()) + 1));
                    updSpbzb0.setSpjg00("审批中");
                    jdbcGateway.update("pay.spbzb0.updateSpjg00", updSpbzb0);
                }
            } else {
                // 更新审批记录表状态为拒绝
                PaySpjlb0 updSpjlb0 = new PaySpjlb0();
                updSpjlb0.setId0000(approvalParam.getId0000());
                updSpjlb0.setSpzt00("2");
                jdbcGateway.update("pay.spjlb0.updateByPrimaryKeySelective", updSpjlb0);
                // 删除剩余待审批和审批中的记录
                jdbcGateway.delete("pay.spbzb0.deleteByJlbid0", paySpbzb0.getJlbid0());
            }

            if(count > 0 && approvalParam.getSybzs0() == 1){
                if("审批通过".equals(approvalParam.getSpjg00())) {
                    PaySpjlb0 updSpjlb0 = new PaySpjlb0();
                    updSpjlb0.setId0000(approvalParam.getId0000());
                    updSpjlb0.setSpzt00("1");
                    updSpjlb0.setWcsj00(new Date());
                    jdbcGateway.update("pay.spjlb0.updateByPrimaryKeySelective");

                    PaySpjlb0 paySpjlb0 = jdbcGateway.selectOne("pay.spjlb0.selectByPrimaryKey", approvalParam.getId0000());
                    PayZfdd00 payZfdd00 = jdbcGateway.selectOne("pay.zfdd00.selectByPrimaryKey", paySpjlb0.getGlddid());
                    if (payZfdd00 == null) {
                        throw new CustomException(HttpStatus.BAD_REQUEST, "订单不存在！");
                    }

                    if (!payZfdd00.getDdzt00().equals(PayZfdd00.STATE_SUCCESS)) {
                        throw new CustomException(HttpStatus.BAD_REQUEST, "订单状态不正确");
                    }

                    if (payZfdd00.getTkzje0() + payZfdd00.getZfje00() > payZfdd00.getZfje00()) {
                        throw new CustomException(HttpStatus.BAD_REQUEST, "退款金额超过订单可退款金额！");
                    }
                    RefundOrderCreateRequest request = new RefundOrderCreateRequest();
                    RefundOrderCreateReqModel model = new RefundOrderCreateReqModel();
                    request.setBizModel(model);

                    model.setAppId(payZfdd00.getFwqdid());
                    model.setPayOrderId(payZfdd00.getXtddh0());
                    model.setMchRefundNo(SeqKit.genMhoOrderId());
                    model.setRefundAmount(payZfdd00.getZfje00());
                    model.setRefundReason("单边账-原路退回");
                    model.setOperatorId(approvalParam.getSpr000());
                    model.setOperatorName(approvalParam.getSprxm0());

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
                    // 更新支付订单审批状态为审批结束
                    PayZfdd00 updZfdd00 = new PayZfdd00();
                    updZfdd00.setSpzt00("2");
                    updZfdd00.setId0000(payZfdd00.getId0000());
                    jdbcGateway.update("pay.zfdd00.updateByPrimaryKeySelective", updZfdd00);

                    PaySpbzb0 spbzb0 = new PaySpbzb0();
                    spbzb0.setJlbid0(approvalParam.getId0000());
                    spbzb0.setSpbz00("5");
                    spbzb0.setSpsj00(new Date());
                    spbzb0.setSpr000("99999");
                    spbzb0.setSprxm0("系统");
                    spbzb0.setSpjg00("系统自动退款");
                    jdbcGateway.insert("pay.spbzb0.insertSelective", spbzb0);
                } else {
                    PaySpjlb0 updSpjlb0 = new PaySpjlb0();
                    updSpjlb0.setId0000(approvalParam.getId0000());
                    updSpjlb0.setSpzt00("2");
                    jdbcGateway.update("pay.spjlb0.updateByPrimaryKeySelective", updSpjlb0);
                }
            }
        } else if("2".equals(approvalParam.getLx0000())){
            // 差异账统计
            //PaySpjlb0 paySpjlb0 = jdbcGateway.selectOne("pay.spjlb0.selectByGlddid", approvalParam.getId0000());
            PaySpbzb0 paySpbzb0 = new PaySpbzb0();
            paySpbzb0.setJlbid0(approvalParam.getId0000());
            paySpbzb0.setSpbz00(approvalParam.getSpbz00());
            paySpbzb0.setSpsj00(new Date());
            paySpbzb0.setSpr000(approvalParam.getSpr000());
            paySpbzb0.setSprxm0(approvalParam.getSprxm0());
            paySpbzb0.setSpjg00(approvalParam.getSpjg00());
            paySpbzb0.setSqyy00(approvalParam.getSqyy00());
            paySpbzb0.setTpfj00(approvalParam.getTpfj00());
            int count = jdbcGateway.update("pay.spbzb0.updateByJlbid0AndSpbz00", paySpbzb0);
            if(count > 0 && "0".equals(xtSpsz00.getSpfs00())){
                // 一人审批即可通过
                // 删除当前步骤剩余审批人
                jdbcGateway.delete("pay.spbzb0.deleteByJlbid0AndSpbz00", paySpbzb0);
            }

            if("审批通过".equals(approvalParam.getSpjg00())) {
                // 查询当前步骤是否还有审批中的记录
                PaySpbzb0 selSpbzb0 = new PaySpbzb0();
                selSpbzb0.setJlbid0(paySpbzb0.getJlbid0());
                selSpbzb0.setSpbz00(paySpbzb0.getSpbz00());
                List<PaySpbzb0> list = jdbcGateway.selectList("pay.spbzb0.selectByJlbid0AndSpbz00", selSpbzb0);
                if(list.size() == 0){
                    PaySpbzb0 updSpbzb0 = new PaySpbzb0();
                    updSpbzb0.setJlbid0(paySpbzb0.getJlbid0());
                    updSpbzb0.setSpbz00(String.valueOf(Integer.valueOf(paySpbzb0.getSpbz00()) + 1));
                    updSpbzb0.setSpjg00("审批中");
                    jdbcGateway.update("pay.spbzb0.updateSpjg00", updSpbzb0);
                }
            } else {
                // 更新审批记录表状态为拒绝
                PaySpjlb0 updSpjlb0 = new PaySpjlb0();
                updSpjlb0.setId0000(approvalParam.getId0000());
                updSpjlb0.setSpzt00("2");
                jdbcGateway.update("pay.spjlb0.updateByPrimaryKeySelective", updSpjlb0);
                // 删除剩余待审批和审批中的记录
                jdbcGateway.delete("pay.spbzb0.deleteByJlbid0", paySpbzb0.getJlbid0());
            }

            if(count > 0 && approvalParam.getSybzs0() == 1) {
                if("审批通过".equals(approvalParam.getSpjg00())) {
                    PaySpjlb0 updSpjlb0 = new PaySpjlb0();
                    updSpjlb0.setId0000(approvalParam.getId0000());
                    updSpjlb0.setSpzt00("1");
                    updSpjlb0.setWcsj00(new Date());
                    jdbcGateway.update("pay.spjlb0.updateByPrimaryKeySelective", updSpjlb0);

                    PaySpjlb0 paySpjlb0 = jdbcGateway.selectOne("pay.spjlb0.selectByPrimaryKey", approvalParam.getId0000());
                    PayDzmxb0 payDzmxb0 = jdbcGateway.selectOne("pay.dzmxb0.selectByPrimaryKey", paySpjlb0.getGlddid());
                    // 发起退款
                    // 调用退款接口
                    String refundNo = SeqKit.genMhoOrderId();
                    PayZfdd00 payZfdd00 = jdbcGateway.selectOne("pay.zfdd00.selectByXtddh0", payDzmxb0.getXtddh0());
                    if (payZfdd00 == null) {
                        throw new CustomException(HttpStatus.BAD_REQUEST, "订单不存在！");
                    }

                    if (!payZfdd00.getDdzt00().equals(PayZfdd00.STATE_SUCCESS)) {
                        throw new CustomException(HttpStatus.BAD_REQUEST, "订单状态不正确");
                    }

                    if (payZfdd00.getTkzje0() + payDzmxb0.getPtje00() > payZfdd00.getZfje00()) {
                        throw new CustomException(HttpStatus.BAD_REQUEST, "退款金额超过订单可退款金额！");
                    }

                    RefundOrderCreateRequest request = new RefundOrderCreateRequest();
                    RefundOrderCreateReqModel model = new RefundOrderCreateReqModel();
                    request.setBizModel(model);

                    model.setAppId(payZfdd00.getFwqdid());
                    model.setPayOrderId(payZfdd00.getXtddh0());
                    model.setMchRefundNo(refundNo);
                    model.setRefundAmount(new Double(payDzmxb0.getPtje00() * 100).longValue());
                    model.setRefundReason("差异账退款");
                    model.setOperatorId(approvalParam.getSpr000());
                    model.setOperatorName(approvalParam.getSprxm0());

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

                    PaySpbzb0 spbzb0 = new PaySpbzb0();
                    spbzb0.setJlbid0(approvalParam.getId0000());
                    spbzb0.setSpbz00("5");
                    spbzb0.setSpsj00(new Date());
                    spbzb0.setSpr000("99999");
                    spbzb0.setSprxm0("系统");
                    spbzb0.setSpjg00("系统自动退款");
                    jdbcGateway.insert("pay.spbzb0.insertSelective", spbzb0);

                    // 更改差异账统计异常记录状态为已处理
                    payDzmxb0.setClzt00("1");
                    payDzmxb0.setClfs00("人工退款");
                    payDzmxb0.setClsj00(new Date());
                    payDzmxb0.setClr000(approvalParam.getSpr000());
                    payDzmxb0.setBz0000(refundNo);
                    jdbcGateway.update("pay.dzmxb0.updateByPrimaryKey", payDzmxb0);

                    String billDate = DateUtils.format(payDzmxb0.getXtdzsj(), "yyyyMMdd");
                    // 插入对账汇总数据
                    jdbcGateway.delete("pay.dzhzb0.delete", billDate);
                    jdbcGateway.insert("pay.dzhzb0.insert", billDate);
                    // 插入服务渠道对账汇总数据
                    jdbcGateway.delete("pay.fwdzhz.delete", billDate);
                    jdbcGateway.insert("pay.fwdzhz.insert", billDate);
                    // 插入支付渠道对账汇总数据
                    jdbcGateway.delete("pay.zfdzhz.delete", billDate);
                    jdbcGateway.insert("pay.zfdzhz.insert", billDate);
                } else {
                    PaySpjlb0 updSpjlb0 = new PaySpjlb0();
                    updSpjlb0.setId0000(approvalParam.getId0000());
                    updSpjlb0.setSpzt00("2");
                    jdbcGateway.update("pay.spjlb0.updateByPrimaryKeySelective", updSpjlb0);
                }
            }
        }
    }

    /**
     * 插入流程审批数据
     */
    @Override
    public void insertData(XtSpsz00 xtSpsz00, String id0000, String czyid0, String czyxm0, String splcmc, String sqyy00, String tpfj00, String jlbid0) {
        // 获取审批方式
        String spfs00 = xtSpsz00.getSpfs00();
        // 获取流程设置
        JSONObject jsonObject = JSON.parseObject(xtSpsz00.getLcsz00());
        // 获取一级审批人
        String oneapprover = jsonObject.getString("oneapprover");
        String oneapproverName = jsonObject.getString("oneapproverName");
        // 获取二级审批人
        String twoapprover = jsonObject.getString("twoapprover");
        String twoapproverName = jsonObject.getString("twoapproverName");
        // 获取三级审批人
        String threeapprover = jsonObject.getString("threeapprover");
        String threeapproverName = jsonObject.getString("threeapproverName");
        // 获取四级审批人
        String fourapprover = jsonObject.getString("fourapprover");
        String fourapproverName = jsonObject.getString("fourapproverName");

        // 插入审批记录
        Date nowtime = new Date();
        String spid00 = StringKit.getUUID();
        if(StringUtils.isNotBlank(jlbid0)){
            spid00 = jlbid0;
            PaySpjlb0 spjlb0 = new PaySpjlb0();
            spjlb0.setId0000(spid00);
            spjlb0.setSpzt00("0");
            jdbcGateway.update("pay.spjlb0.updateByPrimaryKeySelective", spjlb0);
        } else {
            PaySpjlb0 spjlb0 = new PaySpjlb0();
            spjlb0.setId0000(spid00);
            spjlb0.setGlddid(id0000);
            spjlb0.setSplcmc(splcmc);
            spjlb0.setLcfqr0(czyid0);
            spjlb0.setSpzt00("0");
            spjlb0.setFqsj00(nowtime);
            jdbcGateway.insert("pay.spjlb0.insertSelective", spjlb0);
        }
        // 插入审批步骤表
        // 发起人
        PaySpbzb0 spbzb0 = new PaySpbzb0();
        spbzb0.setJlbid0(spid00);
        spbzb0.setSpbz00("0");
        spbzb0.setSpsj00(nowtime);
        spbzb0.setSpr000(czyid0);
        spbzb0.setSprxm0(czyxm0);
        spbzb0.setSpjg00("发起退款申请");
        spbzb0.setSqyy00(sqyy00);
        spbzb0.setTpfj00(tpfj00);
        jdbcGateway.insert("pay.spbzb0.insertSelective", spbzb0);
        // 一级审批人
        if (StringUtils.isNotBlank(oneapprover)) {
            if ("1".equals(spfs00)) {
                String[] spl = oneapprover.split(",");
                String[] splName = oneapproverName.split(",");
                for (int i = 0; i < spl.length; i++) {
                    spbzb0 = new PaySpbzb0();
                    spbzb0.setJlbid0(spid00);
                    spbzb0.setSpbz00("1");
                    spbzb0.setSpr000(spl[i]);
                    spbzb0.setSprxm0(splName[i]);
                    spbzb0.setSpjg00("审批中");
                    jdbcGateway.insert("pay.spbzb0.insertSelective", spbzb0);
                }
            } else {
                spbzb0 = new PaySpbzb0();
                spbzb0.setJlbid0(spid00);
                spbzb0.setSpbz00("1");
                spbzb0.setSpr000(oneapprover);
                spbzb0.setSprxm0(oneapproverName);
                spbzb0.setSpjg00("审批中");
                jdbcGateway.insert("pay.spbzb0.insertSelective", spbzb0);
            }
        }
        // 二级审批人
        if (StringUtils.isNotBlank(twoapprover)) {
            if ("1".equals(spfs00)) {
                String[] spl = twoapprover.split(",");
                String[] splName = twoapproverName.split(",");
                for (int i = 0; i < spl.length; i++) {
                    spbzb0 = new PaySpbzb0();
                    spbzb0.setJlbid0(spid00);
                    spbzb0.setSpbz00("2");
                    spbzb0.setSpr000(spl[i]);
                    spbzb0.setSprxm0(splName[i]);
                    spbzb0.setSpjg00("等待审批");
                    jdbcGateway.insert("pay.spbzb0.insertSelective", spbzb0);
                }
            } else {
                spbzb0 = new PaySpbzb0();
                spbzb0.setJlbid0(spid00);
                spbzb0.setSpbz00("2");
                spbzb0.setSpr000(twoapprover);
                spbzb0.setSprxm0(twoapproverName);
                spbzb0.setSpjg00("等待审批");
                jdbcGateway.insert("pay.spbzb0.insertSelective", spbzb0);
            }
        }
        // 三级审批人
        if (StringUtils.isNotBlank(threeapprover)) {
            if ("1".equals(spfs00)) {
                String[] spl = threeapprover.split(",");
                String[] splName = threeapproverName.split(",");
                for (int i = 0; i < spl.length; i++) {
                    spbzb0 = new PaySpbzb0();
                    spbzb0.setJlbid0(spid00);
                    spbzb0.setSpbz00("3");
                    spbzb0.setSpr000(spl[i]);
                    spbzb0.setSprxm0(splName[i]);
                    spbzb0.setSpjg00("等待审批");
                    jdbcGateway.insert("pay.spbzb0.insertSelective", spbzb0);
                }
            } else {
                spbzb0 = new PaySpbzb0();
                spbzb0.setJlbid0(spid00);
                spbzb0.setSpbz00("3");
                spbzb0.setSpr000(threeapprover);
                spbzb0.setSprxm0(threeapproverName);
                spbzb0.setSpjg00("等待审批");
                jdbcGateway.insert("pay.spbzb0.insertSelective", spbzb0);
            }
        }
        // 四级审批人
        if (StringUtils.isNotBlank(fourapprover)) {
            if ("1".equals(spfs00)) {
                String[] spl = fourapprover.split(",");
                String[] splName = fourapproverName.split(",");
                for (int i = 0; i < spl.length; i++) {
                    spbzb0 = new PaySpbzb0();
                    spbzb0.setJlbid0(spid00);
                    spbzb0.setSpbz00("4");
                    spbzb0.setSpr000(spl[i]);
                    spbzb0.setSprxm0(splName[i]);
                    spbzb0.setSpjg00("等待审批");
                    jdbcGateway.insert("pay.spbzb0.insertSelective", spbzb0);
                }
            } else {
                spbzb0 = new PaySpbzb0();
                spbzb0.setJlbid0(spid00);
                spbzb0.setSpbz00("4");
                spbzb0.setSpr000(fourapprover);
                spbzb0.setSprxm0(fourapproverName);
                spbzb0.setSpjg00("等待审批");
                jdbcGateway.insert("pay.spbzb0.insertSelective", spbzb0);
            }
        }
    }
}
