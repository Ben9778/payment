package com.ylz.yx.pay.order.refund.impl;

import com.jeequan.jeepay.JeepayClient;
import com.jeequan.jeepay.exception.JeepayException;
import com.jeequan.jeepay.model.RefundOrderCreateReqModel;
import com.jeequan.jeepay.model.RefundOrderCreateResModel;
import com.jeequan.jeepay.request.RefundOrderCreateRequest;
import com.jeequan.jeepay.response.RefundOrderCreateResponse;
import com.ylz.svc.apic.ApiExpose;
import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.svc.data.dao.support.PageModel;
import com.ylz.svc.web.HttpResponse;
import com.ylz.yx.pay.config.ApplicationProperty;
import com.ylz.yx.pay.core.entity.PayFwqd00;
import com.ylz.yx.pay.core.entity.PayTkdd00;
import com.ylz.yx.pay.core.exception.CustomException;
import com.ylz.yx.pay.core.exception.HttpStatus;
import com.ylz.yx.pay.order.refund.model.PayTkdd00Param;
import com.ylz.yx.pay.order.refund.RefundService;
import com.ylz.yx.pay.order.refund.model.PayTkdd00RS;
import com.ylz.yx.pay.order.refund.model.SelectParam;
import com.ylz.yx.pay.system.dict.impl.DictServiceImpl;
import com.ylz.yx.pay.system.dict.vo.GlobalDict;
import com.ylz.yx.pay.annotation.BusinessLog;
import com.ylz.yx.pay.annotation.BusinessType;
import com.ylz.yx.pay.utils.ValidateUtils;
import com.ylz.yx.pay.utils.excel.ExcelData;
import com.ylz.yx.pay.utils.excel.ExcelUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiExpose("refundorder")
@Service("refundService")
public class RefundServiceImpl implements RefundService {

    @Autowired
    private JdbcGateway jdbcGateway;

    @Resource
    private ApplicationProperty applicationProperty;

    @Override
    @ApiExpose("queryList")
    public Map<String, Object> queryList(SelectParam param) throws Exception {
        if (param.getPageSize() == null || param.getPageIndex() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "传入的参数不完整！");
        }

        int pageSize = Integer.valueOf(param.getPageSize());
        int pageIndex = Integer.valueOf(param.getPageIndex());
        PageModel pageModel = new PageModel(pageIndex, pageSize);

        List<PayTkdd00RS> payTkdd00List = jdbcGateway.selectListByPage("pay.tkdd00.selectList", param, pageModel);

        // 得到所有支付渠道
        Map<String, String> zfqdmcMap = new HashMap<>();
        GlobalDict payChannel = DictServiceImpl.getDictByKey000("pay_channel");
        if (payChannel != null) {
            for (GlobalDict dict : payChannel.getChildren().values()) {
                zfqdmcMap.put(dict.getKey000(), dict.getZdmc00());
            }
            for (PayTkdd00RS order:payTkdd00List) {
                // 存入支付渠道名称
                if (StringUtils.isNotEmpty(zfqdmcMap.get(order.getZfqd00()))) {
                    order.setZfqdmc(zfqdmcMap.get(order.getZfqd00()));
                }else {
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
            for (PayTkdd00RS order:payTkdd00List) {
                // 存入订单类型名称
                if (StringUtils.isNotEmpty(ddlxmcMap.get(order.getDdlx00()))) {
                    order.setDdlxmc(ddlxmcMap.get(order.getDdlx00()));
                }else {
                    order.setDdlxmc(order.getDdlx00());
                }
            }
        }
        // 得到所有订单状态
        Map<String, String> ddzt00Map = new HashMap<>();
        GlobalDict refundState = DictServiceImpl.getDictByKey000("refund_state");
        if (refundState != null) {
            for (GlobalDict dict : refundState.getChildren().values()) {
                ddzt00Map.put(dict.getKey000(), dict.getZdmc00());
            }
            for (PayTkdd00RS order:payTkdd00List) {
                // 存入订单状态名称
                if (StringUtils.isNotEmpty(ddzt00Map.get(order.getTkzt00()))) {
                    order.setTkztmc(ddzt00Map.get(order.getTkzt00()));
                }else {
                    order.setTkztmc(order.getTkzt00());
                }
            }
        }

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("list", payTkdd00List);
        resultMap.put("total", jdbcGateway.selectOne("pay.tkdd00.selectCountList", param));
        return resultMap;
    }

    @Override
    @BusinessLog(title = "退款订单列表", businessType = BusinessType.EXPORT)
    public String expList(SelectParam param, HttpServletResponse response) {
        List<PayTkdd00RS> list = jdbcGateway.selectList("pay.tkdd00.selectList", param);

        if(CollectionUtils.isEmpty(list)) {
            return "暂无数据";
        }

        // 得到所有支付渠道
        Map<String, String> zfqdmcMap = new HashMap<>();
        GlobalDict payChannel = DictServiceImpl.getDictByKey000("pay_channel");
        if (payChannel != null) {
            for (GlobalDict dict : payChannel.getChildren().values()) {
                zfqdmcMap.put(dict.getKey000(), dict.getZdmc00());
            }
            for (PayTkdd00RS order:list) {
                // 存入支付渠道名称
                if (StringUtils.isNotEmpty(zfqdmcMap.get(order.getZfqd00()))) {
                    order.setZfqdmc(zfqdmcMap.get(order.getZfqd00()));
                }else {
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
            for (PayTkdd00RS order:list) {
                // 存入订单类型名称
                if (StringUtils.isNotEmpty(ddlxmcMap.get(order.getDdlx00()))) {
                    order.setDdlxmc(ddlxmcMap.get(order.getDdlx00()));
                }else {
                    order.setDdlxmc(order.getDdlx00());
                }
            }
        }
        // 得到所有订单状态
        Map<String, String> ddzt00Map = new HashMap<>();
        GlobalDict refundState = DictServiceImpl.getDictByKey000("refund_state");
        if (refundState != null) {
            for (GlobalDict dict : refundState.getChildren().values()) {
                ddzt00Map.put(dict.getKey000(), dict.getZdmc00());
            }
            for (PayTkdd00RS order:list) {
                // 存入订单状态名称
                if (StringUtils.isNotEmpty(ddzt00Map.get(order.getTkzt00()))) {
                    order.setTkztmc(ddzt00Map.get(order.getTkzt00()));
                }else {
                    order.setTkztmc(order.getTkzt00());
                }
            }
        }

        ExcelData excelData = new ExcelData();
        List<String> titleList = new ArrayList();
        titleList.add("退款订单号");
        titleList.add("订单类型");
        titleList.add("患者姓名");
        titleList.add("就诊卡号");
        titleList.add("服务渠道");
        titleList.add("退款渠道");
        titleList.add("退款金额");
        titleList.add("退款原因");
        titleList.add("退款订单状态");
        titleList.add("退款时间");
        excelData.setTitles(titleList);
        List<List<Object>> rows = new ArrayList();
        for (PayTkdd00RS bean : list) {
            List<Object> row = new ArrayList();
            row.add(bean.getXtddh0());
            row.add(bean.getDdlxmc());
            row.add(bean.getHzxm00());
            row.add(bean.getKhzyh0());
            row.add(bean.getFwqdmc());
            row.add(bean.getZfqdmc());
            row.add(bean.getTkje00()+"元");
            row.add(bean.getTkyy00());
            row.add(bean.getTkztmc());
            row.add(bean.getDdcjsj());
            rows.add(row);
        }
        excelData.setRows(rows);
        try {
            ExcelUtils.exportExcel(response, "退款订单列表", excelData);
        } catch (Exception e) {

        }

        return null;
    }

    @Override
    @ApiExpose("orderDetail")
    public Object orderDetail(SelectParam param) {
        PayTkdd00RS order = jdbcGateway.selectOne("pay.tkdd00.selectById0000", param.getId0000());

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
            }else {
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
            }else {
                order.setZffsmc(order.getZffs00());
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
            }else {
                order.setDdlxmc(order.getDdlx00());
            }
        }
        // 得到所有订单状态
        Map<String, String> ddzt00Map = new HashMap<>();
        GlobalDict refundState = DictServiceImpl.getDictByKey000("refund_state");
        if (refundState != null) {
            for (GlobalDict dict : refundState.getChildren().values()) {
                ddzt00Map.put(dict.getKey000(), dict.getZdmc00());
            }
            // 存入订单状态名称
            if (StringUtils.isNotEmpty(ddzt00Map.get(order.getTkzt00()))) {
                order.setTkztmc(ddzt00Map.get(order.getTkzt00()));
            }else {
                order.setTkztmc(order.getTkzt00());
            }
        }
        order.setTkfs00("原路退回"); // 固定写死
        return order;
    }

    @Override
//    @ApiExpose("orderRefund")
    @BusinessLog(title = "退款订单列表", businessType = BusinessType.UPDATE)
    public HttpResponse orderRefund(PayTkdd00Param param) {

        ValidateUtils.Validate(param);

        String id0000 = param.getId0000();

        PayTkdd00 payTkdd00 = jdbcGateway.selectOne("pay.tkdd00.selectByPrimaryKey", id0000);
        if (payTkdd00 == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "订单不存在！");
        }

        if(!payTkdd00.getTkzt00().equals(PayTkdd00.STATE_FAIL)){
            throw new CustomException(HttpStatus.BAD_REQUEST, "订单状态不正确");
        }

        RefundOrderCreateRequest request = new RefundOrderCreateRequest();
        RefundOrderCreateReqModel model = new RefundOrderCreateReqModel();
        request.setBizModel(model);

        model.setMchNo(payTkdd00.getXtddh0());     // 系统订单号
        model.setAppId(payTkdd00.getFwqdid());
        model.setPayOrderId(payTkdd00.getYxtddh());
        model.setMchRefundNo(payTkdd00.getFwddh0());
        model.setRefundAmount(payTkdd00.getTkje00());
        model.setRefundReason(payTkdd00.getTkyy00());
        model.setNotifyUrl(payTkdd00.getYbtzdz());
        model.setChannelExtra(payTkdd00.getQdcs00());
        model.setExtParam(payTkdd00.getKzcs00());
        model.setOperatorId(param.getCzyid0());
        model.setOperatorName(param.getCzyxm0());

        //更新原订单退款状态为关闭
        PayTkdd00 updateRecord = new PayTkdd00();
        updateRecord.setTkzt00(PayTkdd00.STATE_CLOSED);
        updateRecord.setId0000(payTkdd00.getId0000());
        jdbcGateway.update("pay.tkdd00.updateByPrimaryKeySelective", updateRecord);

        PayFwqd00 mchApp = jdbcGateway.selectOne("pay.fwqd00.selectByPrimaryKey", payTkdd00.getFwqdid());

        JeepayClient jeepayClient = new JeepayClient(applicationProperty.getPaySiteBackUrl(), mchApp.getQdmy00());

        try {
            RefundOrderCreateResponse response = jeepayClient.execute(request);
            if(response.getCode() != 0){
                return new HttpResponse(HttpStatus.BAD_REQUEST, response.getMsg());
            } else {
                RefundOrderCreateResModel resModel = response.get();
                if(resModel != null){
                    if(resModel.getState() == 1){
                        return new HttpResponse(HttpStatus.SUCCESS, "退款中！");
                    } else if(resModel.getState() == 3){
                        return new HttpResponse(HttpStatus.BAD_REQUEST, "退款失败！");
                    } else if(resModel.getState() == 2){
                        return new HttpResponse(HttpStatus.SUCCESS, "退款成功！");
                    }
                }
            }
            /*try {
                Thread.sleep(3000);
                RefundOrderQueryRequest queryRequest = new RefundOrderQueryRequest();
                RefundOrderQueryReqModel queryReqModel = new RefundOrderQueryReqModel();
                queryRequest.setBizModel(queryReqModel);
                queryReqModel.setMchRefundNo(payTkdd00.getFwddh0());

                RefundOrderQueryResponse queryResponse = jeepayClient.execute(queryRequest);
                if(queryResponse.getCode() != 0){
                    throw new CustomException(HttpStatus.BAD_REQUEST, queryResponse.getMsg());
                } else {
                    RefundOrderQueryResModel queryResModel = queryResponse.get();
                    if("1".equals(queryResModel.getState())){
                        throw new CustomException(HttpStatus.BAD_REQUEST, "退款中！");
                    } else if("3".equals(queryResModel.getState())){
                        throw new CustomException(HttpStatus.BAD_REQUEST, "退款失败！");
                    }
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/


        } catch (JeepayException e) {
            return new HttpResponse(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return new HttpResponse();
    }
}
