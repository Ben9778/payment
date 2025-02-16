package com.ylz.yx.pay.record.impl;

import cn.hutool.core.date.DateUtil;
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
import com.ylz.yx.pay.config.ApplicationProperty;
import com.ylz.yx.pay.core.entity.PayFwqd00;
import com.ylz.yx.pay.core.entity.PayTkdd00;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.core.exception.CustomException;
import com.ylz.yx.pay.core.exception.HttpStatus;
import com.ylz.yx.pay.record.RecordService;
import com.ylz.yx.pay.record.model.*;
import com.ylz.yx.pay.system.approval.ApprovalService;
import com.ylz.yx.pay.system.approval.model.PaySpbzb0;
import com.ylz.yx.pay.system.approval.model.PaySpjlb0;
import com.ylz.yx.pay.system.approval.model.XtSpsz00;
import com.ylz.yx.pay.system.dict.impl.DictServiceImpl;
import com.ylz.yx.pay.system.dict.vo.GlobalDict;
import com.ylz.yx.pay.annotation.BusinessLog;
import com.ylz.yx.pay.annotation.BusinessType;
import com.ylz.yx.pay.utils.SeqKit;
import com.ylz.yx.pay.utils.excel.ExcelData;
import com.ylz.yx.pay.utils.excel.ExcelUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

@ApiExpose("record")
@Service("recordService")
public class RecordServiceImpl implements RecordService {

    @Autowired
    private JdbcGateway jdbcGateway;
    @Resource
    private ApplicationProperty applicationProperty;
    @Autowired
    private ApprovalService approvalService;

    /**
     * 查询汇总数据
     **/
    @Override
    @ApiExpose("cxhzsj")
    public Object cxhzsj(QueryParam param) {
        return jdbcGateway.selectOne("pay.dzhzb0.cxhzsj", param);
    }

    /**
     * 每日对账汇总数据
     **/
    @Override
    @ApiExpose("mrdzhz")
    public Map<String, Object> mrdzhz(QueryParam param) {
        if (param.getPageSize() == null || param.getPageIndex() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "传入的参数不完整！");
        }

        int pageSize = Integer.valueOf(param.getPageSize());
        int pageIndex = Integer.valueOf(param.getPageIndex());
        PageModel pageModel = new PageModel(pageIndex, pageSize);
        Map<String, Object> resultMap = new HashMap<>();
        List<PayMrdzhz> list = jdbcGateway.selectListByPage("pay.dzmxb0.mrdzhz", param, pageModel);

        for (PayMrdzhz bean : list) {
            bean.setFwdzjg(countDzjg00(bean.getFwdzjg()));
            bean.setZfdzjg(countDzjg00(bean.getZfdzjg()));
            bean.setCljd00(countCljd00(bean.getCljd00()));

            if(StringUtils.isBlank(param.getFwqd00())){
                bean.setFwqdid("");
                bean.setFwqdmc("全部");
            }
            if(StringUtils.isBlank(param.getZfqd00())){
                bean.setZfqdid("");
                bean.setZfqdmc("全部");
            }
            if(StringUtils.isNotBlank(param.getYwlx00())){
                if("01".equals(param.getYwlx00())){
                    bean.setDdlx00(param.getYwlx00());
                    bean.setDdlxmc("门诊");
                } else if("02".equals(param.getYwlx00())){
                    bean.setDdlx00(param.getYwlx00());
                    bean.setDdlxmc("住院");
                }
            } else {
                bean.setDdlx00("");
                bean.setDdlxmc("全部");
            }
        }

        resultMap.put("list", list);
        resultMap.put("total", jdbcGateway.selectOne("pay.dzmxb0.mrdzhzCount", param));
        return resultMap;
    }

    /**
     * 每日对账汇总数据导出
     **/
    @Override
    @BusinessLog(title = "对账管理-每日汇总统计", businessType = BusinessType.EXPORT)
    public String expmrdzhz(QueryParam param, HttpServletResponse response) {
        List<PayMrdzhz> list = jdbcGateway.selectList("pay.dzmxb0.mrdzhz", param);

        if (CollectionUtils.isEmpty(list)) {
            return "暂无数据";
        }

        ExcelData excelData = new ExcelData();
        List<String> titleList = new ArrayList();
        titleList.add("对账日期");
        titleList.add("服务渠道");
        titleList.add("支付渠道");
        titleList.add("业务类型");
        titleList.add("平台金额");
        titleList.add("平台笔数");
        titleList.add("HIS金额");
        titleList.add("HIS笔数");
        titleList.add("支付渠道金额");
        titleList.add("支付渠道笔数");
        titleList.add("平台与HIS对账");
        titleList.add("平台与支付渠道对账");
        titleList.add("处理进度");
        excelData.setTitles(titleList);
        List<List<Object>> rows = new ArrayList();
        for (PayMrdzhz bean : list) {
            List<Object> row = new ArrayList();
            row.add(bean.getDzrq00());
            if(StringUtils.isBlank(param.getFwqd00())){
                row.add("全部");
            } else {
                row.add(bean.getFwqdmc());
            }
            if(StringUtils.isBlank(param.getZfqd00())){
                row.add("全部");
            } else {
                row.add(bean.getZfqdmc());
            }
            if(StringUtils.isNotBlank(param.getYwlx00())){
                if("01".equals(param.getYwlx00())){
                    row.add("门诊");
                } else if("02".equals(param.getYwlx00())){
                    row.add("住院");
                }
            } else {
                row.add("全部");
            }
            row.add(bean.getPtje00());
            row.add(bean.getPtbs00());
            row.add(bean.getFwqdje());
            row.add(bean.getFwqdbs());
            row.add(bean.getZfptje());
            row.add(bean.getZfptbs());
            row.add(countDzjg00(bean.getFwdzjg()));
            row.add(countDzjg00(bean.getZfdzjg()));
            row.add(countCljd00(bean.getCljd00()));
            rows.add(row);
        }
        excelData.setRows(rows);
        try {
            ExcelUtils.exportExcel(response, "每日汇总统计", excelData);
        } catch (Exception e) {

        }

        return null;
    }

    /**
     * 按支付渠道统计汇总数据
     **/
    @Override
    @ApiExpose("zfqddzhz")
    public Map<String, Object> zfqddzhz(QueryParam param) {
        Map<String, Object> resultMap = new HashMap<>();
        List<PayZfdzhz> list = jdbcGateway.selectList("pay.zfdzhz.zfqddzhz", param);

        for (PayZfdzhz bean : list) {
            bean.setFwdzjg(countDzjg00(bean.getFwdzjg()));
            bean.setZfdzjg(countDzjg00(bean.getZfdzjg()));
            bean.setCljd00(countCljd00(bean.getCljd00()));
        }

        resultMap.put("list", list);
        resultMap.put("total", list.size());
        return resultMap;
    }

    /**
     * 支付渠道统计汇总数据导出
     **/
    @Override
    @BusinessLog(title = "对账管理-对账汇总统计（支付渠道）", businessType = BusinessType.EXPORT)
    public String expzfqddzhz(QueryParam param, HttpServletResponse response) {
        List<PayZfdzhz> list = jdbcGateway.selectList("pay.zfdzhz.zfqddzhz", param);

        if (CollectionUtils.isEmpty(list)) {
            return "暂无数据";
        }

        ExcelData excelData = new ExcelData();
        List<String> titleList = new ArrayList();
        titleList.add("支付渠道名称");
        titleList.add("平台金额");
        titleList.add("平台笔数");
        titleList.add("HIS金额");
        titleList.add("HIS笔数");
        titleList.add("支付渠道金额");
        titleList.add("支付渠道笔数");
        titleList.add("平台与HIS对账");
        titleList.add("平台与支付渠道对账");
        titleList.add("处理进度");
        excelData.setTitles(titleList);
        List<List<Object>> rows = new ArrayList();
        for (PayZfdzhz bean : list) {
            List<Object> row = new ArrayList();
            row.add(bean.getZfqdmc());
            row.add(bean.getPtje00());
            row.add(bean.getPtbs00());
            row.add(bean.getFwqdje());
            row.add(bean.getFwqdbs());
            row.add(bean.getZfptje());
            row.add(bean.getZfptbs());
            row.add(countDzjg00(bean.getFwdzjg()));
            row.add(countDzjg00(bean.getZfdzjg()));
            row.add(countCljd00(bean.getCljd00()));
            rows.add(row);
        }
        excelData.setRows(rows);
        try {
            ExcelUtils.exportExcel(response, "对账汇总统计（支付渠道）", excelData);
        } catch (Exception e) {

        }

        return null;
    }

    /**
     * 按服务渠道统计汇总数据
     **/
    @Override
    @ApiExpose("fwqddzhz")
    public Map<String, Object> fwqddzhz(QueryParam param) {
        Map<String, Object> resultMap = new HashMap<>();
        List<PayFwdzhz> list = jdbcGateway.selectList("pay.fwdzhz.fwqddzhz", param);
        for (PayFwdzhz bean : list) {
            bean.setFwdzjg(countDzjg00(bean.getFwdzjg()));
            bean.setZfdzjg(countDzjg00(bean.getZfdzjg()));
            bean.setCljd00(countCljd00(bean.getCljd00()));
        }
        resultMap.put("list", list);
        resultMap.put("total", list.size());
        return resultMap;
    }

    /**
     * 服务渠道统计汇总数据导出
     **/
    @Override
    @BusinessLog(title = "对账管理-对账汇总统计（服务渠道）", businessType = BusinessType.EXPORT)
    public String expfwqddzhz(QueryParam param, HttpServletResponse response) {
        List<PayFwdzhz> list = jdbcGateway.selectList("pay.fwdzhz.fwqddzhz", param);

        if (CollectionUtils.isEmpty(list)) {
            return "暂无数据";
        }

        ExcelData excelData = new ExcelData();
        List<String> titleList = new ArrayList();
        titleList.add("服务渠道名称");
        titleList.add("平台金额");
        titleList.add("平台笔数");
        titleList.add("HIS金额");
        titleList.add("HIS笔数");
        titleList.add("支付渠道金额");
        titleList.add("支付渠道笔数");
        titleList.add("平台与HIS对账");
        titleList.add("平台与支付渠道对账");
        titleList.add("处理进度");
        excelData.setTitles(titleList);
        List<List<Object>> rows = new ArrayList();
        for (PayFwdzhz bean : list) {
            List<Object> row = new ArrayList();
            row.add(bean.getFwqdmc());
            row.add(bean.getPtje00());
            row.add(bean.getPtbs00());
            row.add(bean.getFwqdje());
            row.add(bean.getFwqdbs());
            row.add(bean.getZfptje());
            row.add(bean.getZfptbs());
            row.add(countDzjg00(bean.getFwdzjg()));
            row.add(countDzjg00(bean.getZfdzjg()));
            row.add(countCljd00(bean.getCljd00()));
            rows.add(row);
        }
        excelData.setRows(rows);
        try {
            ExcelUtils.exportExcel(response, "对账汇总统计（服务渠道）", excelData);
        } catch (Exception e) {

        }

        return null;
    }

    /**
     * 查询对账明细统计
     **/
    @Override
    @ApiExpose("dzmxtj")
    public Map<String, Object> dzmxtj(QueryParam param) {
        if (param.getPageSize() == null || param.getPageIndex() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "传入的参数不完整！");
        }

        int pageSize = Integer.valueOf(param.getPageSize());
        int pageIndex = Integer.valueOf(param.getPageIndex());
        PageModel pageModel = new PageModel(pageIndex, pageSize);
        Map<String, Object> resultMap = new HashMap<>();
        List<PayDzmxb0> list = jdbcGateway.selectListByPage("pay.dzmxb0.selectAllList", param, pageModel);
        // 得到所有订单类型
        Map<String, String> ddlxmcMap = new HashMap<>();
        GlobalDict orderType = DictServiceImpl.getDictByKey000("order_type");
        if (orderType != null) {
            for (GlobalDict dict : orderType.getChildren().values()) {
                ddlxmcMap.put(dict.getKey000(), dict.getZdmc00());
            }
            for (PayDzmxb0 order : list) {
                // 存入订单类型名称
                if (StringUtils.isNotEmpty(ddlxmcMap.get(order.getDdlx00()))) {
                    order.setDdlx00(ddlxmcMap.get(order.getDdlx00()));
                } else {
                    order.setDdlx00(order.getDdlx00());
                }
            }
        }
        // 得到所有对账结果名称
        Map<String, String> dzjgmcMap = new HashMap<>();
        GlobalDict recordResult = DictServiceImpl.getDictByKey000("record_result");
        if (recordResult != null) {
            for (GlobalDict dict : recordResult.getChildren().values()) {
                dzjgmcMap.put(dict.getKey000(), dict.getZdmc00());
            }
            for (PayDzmxb0 order : list) {
                // 存入对账结果名称
                if (StringUtils.isNotEmpty(dzjgmcMap.get(order.getFwdzjg()))) {
                    order.setFwdzjg(dzjgmcMap.get(order.getFwdzjg()));
                } else {
                    order.setFwdzjg(order.getFwdzjg());
                }
                // 存入对账结果名称
                if (StringUtils.isNotEmpty(dzjgmcMap.get(order.getZfdzjg()))) {
                    order.setZfdzjg(dzjgmcMap.get(order.getZfdzjg()));
                } else {
                    order.setZfdzjg(order.getZfdzjg());
                }
                if ("对账一致".equals(order.getFwdzjg()) && "对账一致".equals(order.getZfdzjg())) {
                    order.setDzjg00("对账一致");
                    order.setDzjgsm("对账一致");
                } else if ("对账一致".equals(order.getFwdzjg()) && !"对账一致".equals(order.getZfdzjg())) {
                    order.setDzjg00(order.getZfdzjg() + "￥" + order.getZfcyje() + "元");
                    order.setDzjgsm(order.getZfdzjg());
                    if (("短款".equals(order.getDzjgsm()) && "充值".equals(order.getYwlx00())) || ("长款".equals(order.getDzjgsm()) && "退款".equals(order.getYwlx00()))) {
                        // 系统对账时间往后延迟2天
                        Date date1 = DateUtil.offsetDay(order.getCjsj00(), 2).toJdkDate();
                        Date date2 = new Date();

                        long time1 = date1.getTime();
                        long time2 = date2.getTime();

                        if (time2 > time1) {
                            order.setYxcz00("1");
                        } else {
                            order.setYxcz00("0");
                        }
                    }
                } else if (!"对账一致".equals(order.getFwdzjg()) && "对账一致".equals(order.getZfdzjg())) {
                    order.setDzjg00(order.getFwdzjg() + "￥" + order.getFwcyje() + "元");
                    order.setDzjgsm(order.getFwdzjg());
                } else {
                    order.setDzjg00(order.getFwdzjg() + "￥" + order.getFwcyje() + "元");
                    order.setDzjgsm(order.getFwdzjg());
                }
            }
        }
        // 得到所有处理状态
        Map<String, String> clztmcMap = new HashMap<>();
        GlobalDict handleState = DictServiceImpl.getDictByKey000("handle_state");
        if (handleState != null) {
            for (GlobalDict dict : handleState.getChildren().values()) {
                clztmcMap.put(dict.getKey000(), dict.getZdmc00());
            }
            for (PayDzmxb0 order : list) {
                // 存入处理状态名称
                if (StringUtils.isNotEmpty(clztmcMap.get(order.getClzt00()))) {
                    order.setClzt00(clztmcMap.get(order.getClzt00()));
                } else {
                    order.setClzt00(order.getClzt00());
                }
            }
        }
        resultMap.put("list", list);
        resultMap.put("total", jdbcGateway.selectOne("pay.dzmxb0.selectCountAllList", param));

        return resultMap;
    }

    /**
     * 对账明细统计导出
     **/
    @Override
    @BusinessLog(title = "对账管理-对账明细统计", businessType = BusinessType.EXPORT)
    public String expdzmxtj(QueryParam param, HttpServletResponse response) {
        List<PayDzmxb0> list = jdbcGateway.selectList("pay.dzmxb0.selectAllList", param);
        // 得到所有订单类型
        Map<String, String> ddlxmcMap = new HashMap<>();
        GlobalDict orderType = DictServiceImpl.getDictByKey000("order_type");
        if (orderType != null) {
            for (GlobalDict dict : orderType.getChildren().values()) {
                ddlxmcMap.put(dict.getKey000(), dict.getZdmc00());
            }
            for (PayDzmxb0 order : list) {
                // 存入订单类型名称
                if (StringUtils.isNotEmpty(ddlxmcMap.get(order.getDdlx00()))) {
                    order.setDdlx00(ddlxmcMap.get(order.getDdlx00()));
                } else {
                    order.setDdlx00(order.getDdlx00());
                }
            }
        }
        // 得到所有对账结果名称
        Map<String, String> dzjgmcMap = new HashMap<>();
        GlobalDict recordResult = DictServiceImpl.getDictByKey000("record_result");
        if (recordResult != null) {
            for (GlobalDict dict : recordResult.getChildren().values()) {
                dzjgmcMap.put(dict.getKey000(), dict.getZdmc00());
            }
            for (PayDzmxb0 order : list) {
                // 存入对账结果名称
                if (StringUtils.isNotEmpty(dzjgmcMap.get(order.getFwdzjg()))) {
                    order.setFwdzjg(dzjgmcMap.get(order.getFwdzjg()));
                } else {
                    order.setFwdzjg(order.getFwdzjg());
                }
                // 存入对账结果名称
                if (StringUtils.isNotEmpty(dzjgmcMap.get(order.getZfdzjg()))) {
                    order.setZfdzjg(dzjgmcMap.get(order.getZfdzjg()));
                } else {
                    order.setZfdzjg(order.getZfdzjg());
                }

            }
        }
        // 得到所有处理状态
        Map<String, String> clztmcMap = new HashMap<>();
        GlobalDict handleState = DictServiceImpl.getDictByKey000("handle_state");
        if (handleState != null) {
            for (GlobalDict dict : handleState.getChildren().values()) {
                clztmcMap.put(dict.getKey000(), dict.getZdmc00());
            }
            for (PayDzmxb0 order : list) {
                // 存入处理状态名称
                if (StringUtils.isNotEmpty(clztmcMap.get(order.getClzt00()))) {
                    order.setClzt00(clztmcMap.get(order.getClzt00()));
                } else {
                    order.setClzt00(order.getClzt00());
                }
            }
        }

        if (CollectionUtils.isEmpty(list)) {
            return "暂无数据";
        }

        ExcelData excelData = new ExcelData();
        List<String> titleList = new ArrayList();
        titleList.add("订单交易时间");
        titleList.add("系统订单号");
        titleList.add("业务类型");
        titleList.add("交易类型");
        titleList.add("患者姓名");
        titleList.add("就诊卡号");
        titleList.add("服务渠道");
        titleList.add("支付渠道");
        titleList.add("平台金额");
        titleList.add("服务渠道金额");
        titleList.add("支付渠道金额");
        titleList.add("对账结果");
        titleList.add("处理状态");
        excelData.setTitles(titleList);
        List<List<Object>> rows = new ArrayList();
        for (PayDzmxb0 bean : list) {
            List<Object> row = new ArrayList();
            row.add(DateUtils.toStr(bean.getDdcjsj()));
            row.add(bean.getXtddh0());
            row.add(bean.getDdlx00());
            row.add(bean.getYwlx00());
            row.add(bean.getHzxm00());
            row.add(bean.getKhzyh0());
            row.add(bean.getFwqdid());
            row.add(bean.getZfqd00());
            row.add(bean.getPtje00());
            row.add(bean.getFwqdje());
            row.add(bean.getZfptje());
            if ("对账一致".equals(bean.getFwdzjg()) && "对账一致".equals(bean.getZfdzjg())) {
                row.add("对账一致");
            } else if ("对账一致".equals(bean.getFwdzjg()) && !"对账一致".equals(bean.getZfdzjg())) {
                row.add(bean.getZfdzjg() + "￥" + bean.getZfcyje() + "元");
            } else if (!"对账一致".equals(bean.getFwdzjg()) && "对账一致".equals(bean.getZfdzjg())) {
                row.add(bean.getFwdzjg() + "￥" + bean.getFwcyje() + "元");
            } else {
                row.add(bean.getFwdzjg() + "￥" + bean.getFwcyje() + "元");
            }

            row.add(bean.getClzt00());
            rows.add(row);
        }
        excelData.setRows(rows);
        try {
            ExcelUtils.exportExcel(response, "对账明细统计", excelData);
        } catch (Exception e) {

        }
        return null;
    }

    /**
     * 查询差异账统计
     **/
    @Override
    @ApiExpose("cyztj")
    public Map<String, Object> cyztj(QueryParam param) {
        if (param.getPageSize() == null || param.getPageIndex() == null) {
            throw new CustomException(HttpStatus.BAD_REQUEST, "传入的参数不完整！");
        }

        int pageSize = Integer.valueOf(param.getPageSize());
        int pageIndex = Integer.valueOf(param.getPageIndex());
        PageModel pageModel = new PageModel(pageIndex, pageSize);
        Map<String, Object> resultMap = new HashMap<>();
        param.setYczt00("1");
        List<PayDzmxb0> list = jdbcGateway.selectListByPage("pay.dzmxb0.selectAllList", param, pageModel);
        List<PayDzmxb0> list1 = jdbcGateway.selectList("pay.dzmxb0.selectAllList", param);
        // 得到所有订单类型
        Map<String, String> ddlxmcMap = new HashMap<>();
        GlobalDict orderType = DictServiceImpl.getDictByKey000("order_type");
        if (orderType != null) {
            for (GlobalDict dict : orderType.getChildren().values()) {
                ddlxmcMap.put(dict.getKey000(), dict.getZdmc00());
            }
            for (PayDzmxb0 order : list) {
                // 存入订单类型名称
                if (StringUtils.isNotEmpty(ddlxmcMap.get(order.getDdlx00()))) {
                    order.setDdlx00(ddlxmcMap.get(order.getDdlx00()));
                } else {
                    order.setDdlx00(order.getDdlx00());
                }
            }
        }
        // 得到所有对账结果名称
        Map<String, String> dzjgmcMap = new HashMap<>();
        GlobalDict recordResult = DictServiceImpl.getDictByKey000("record_result");
        if (recordResult != null) {
            for (GlobalDict dict : recordResult.getChildren().values()) {
                dzjgmcMap.put(dict.getKey000(), dict.getZdmc00());
            }
            for (PayDzmxb0 order : list) {
                // 存入对账结果名称
                if (StringUtils.isNotEmpty(dzjgmcMap.get(order.getFwdzjg()))) {
                    order.setFwdzjg(dzjgmcMap.get(order.getFwdzjg()));
                } else {
                    order.setFwdzjg(order.getFwdzjg());
                }
                // 存入对账结果名称
                if (StringUtils.isNotEmpty(dzjgmcMap.get(order.getZfdzjg()))) {
                    order.setZfdzjg(dzjgmcMap.get(order.getZfdzjg()));
                } else {
                    order.setZfdzjg(order.getZfdzjg());
                }
                if ("对账一致".equals(order.getFwdzjg()) && "对账一致".equals(order.getZfdzjg())) {
                    order.setDzjg00("对账一致");
                    order.setDzjgsm("对账一致");
                } else if ("对账一致".equals(order.getFwdzjg()) && !"对账一致".equals(order.getZfdzjg())) {
                    order.setDzjg00(order.getZfdzjg() + "￥" + order.getZfcyje() + "元");
                    order.setDzjgsm(order.getZfdzjg());
                    if (("短款".equals(order.getDzjgsm()) && "充值".equals(order.getYwlx00())) || ("长款".equals(order.getDzjgsm()) && "退款".equals(order.getYwlx00()))) {
                        // 系统对账时间往后延迟2天
                        Date date1 = DateUtil.offsetDay(order.getCjsj00(), 2).toJdkDate();
                        Date date2 = new Date();

                        long time1 = date1.getTime();
                        long time2 = date2.getTime();

                        if (time2 > time1) {
                            order.setYxcz00("1");
                        } else {
                            order.setYxcz00("0");
                        }
                    }
                } else if (!"对账一致".equals(order.getFwdzjg()) && "对账一致".equals(order.getZfdzjg())) {
                    order.setDzjg00(order.getFwdzjg() + "￥" + order.getFwcyje() + "元");
                    order.setDzjgsm(order.getFwdzjg());
                    order.setYxcz00("1");
                } else {
                    order.setDzjg00(order.getFwdzjg() + "￥" + order.getFwcyje() + "元");
                    order.setDzjgsm(order.getFwdzjg());
                    order.setYxcz00("1");
                }
            }

            for (PayDzmxb0 order : list1) {
                // 存入对账结果名称
                if (StringUtils.isNotEmpty(dzjgmcMap.get(order.getFwdzjg()))) {
                    order.setFwdzjg(dzjgmcMap.get(order.getFwdzjg()));
                } else {
                    order.setFwdzjg(order.getFwdzjg());
                }
                // 存入对账结果名称
                if (StringUtils.isNotEmpty(dzjgmcMap.get(order.getZfdzjg()))) {
                    order.setZfdzjg(dzjgmcMap.get(order.getZfdzjg()));
                } else {
                    order.setZfdzjg(order.getZfdzjg());
                }
                if ("对账一致".equals(order.getFwdzjg()) && "对账一致".equals(order.getZfdzjg())) {
                    order.setDzjg00("对账一致");
                    order.setDzjgsm("对账一致");
                } else if ("对账一致".equals(order.getFwdzjg()) && !"对账一致".equals(order.getZfdzjg())) {
                    order.setDzjg00(order.getZfdzjg() + "￥" + order.getZfcyje() + "元");
                    order.setDzjgsm(order.getZfdzjg());
                    if (("短款".equals(order.getDzjgsm()) && "充值".equals(order.getYwlx00())) || ("长款".equals(order.getDzjgsm()) && "退款".equals(order.getYwlx00()))) {
                        // 系统对账时间往后延迟2天
                        Date date1 = DateUtil.offsetDay(order.getCjsj00(), 2).toJdkDate();
                        Date date2 = new Date();

                        long time1 = date1.getTime();
                        long time2 = date2.getTime();

                        if (time2 > time1) {
                            order.setYxcz00("1");
                        } else {
                            order.setYxcz00("0");
                        }
                    }
                } else if (!"对账一致".equals(order.getFwdzjg()) && "对账一致".equals(order.getZfdzjg())) {
                    order.setDzjg00(order.getFwdzjg() + "￥" + order.getFwcyje() + "元");
                    order.setDzjgsm(order.getFwdzjg());
                    order.setYxcz00("1");
                } else {
                    order.setDzjg00(order.getFwdzjg() + "￥" + order.getFwcyje() + "元");
                    order.setDzjgsm(order.getFwdzjg());
                    order.setYxcz00("1");
                }
            }
        }
        // 得到所有处理状态
        Map<String, String> clztmcMap = new HashMap<>();
        GlobalDict handleState = DictServiceImpl.getDictByKey000("handle_state");
        if (handleState != null) {
            for (GlobalDict dict : handleState.getChildren().values()) {
                clztmcMap.put(dict.getKey000(), dict.getZdmc00());
            }
            for (PayDzmxb0 order : list) {
                // 存入处理状态名称
                if (StringUtils.isNotEmpty(clztmcMap.get(order.getClzt00()))) {
                    order.setClzt00(clztmcMap.get(order.getClzt00()));
                } else {
                    order.setClzt00(order.getClzt00());
                }
            }
        }

        List<PayDzmxb0> fwck = list1.stream().filter(view -> "长款".equals(view.getDzjgsm())).collect(toList());
        List<PayDzmxb0> fwdk = list1.stream().filter(view -> "短款".equals(view.getDzjgsm())).collect(toList());
        List<PayDzmxb0> fwje = list1.stream().filter(view -> "金额不一致".equals(view.getDzjgsm())).collect(toList());
        List<PayDzmxb0> fwts = list1.stream().filter(view -> "特殊异常账".equals(view.getDzjgsm())).collect(toList());
        resultMap.put("list", list);
        resultMap.put("total", jdbcGateway.selectOne("pay.dzmxb0.selectCountAllList", param));
        resultMap.put("ckbs00", fwck.size());
        resultMap.put("dkbs00", fwdk.size());
        resultMap.put("jebyz0", fwje.size());
        resultMap.put("tsycz0", fwts.size());
        return resultMap;
    }

    /**
     * 差异账统计导出
     **/
    @Override
    @BusinessLog(title = "对账管理-差异账统计", businessType = BusinessType.EXPORT)
    public String expcyztj(QueryParam param, HttpServletResponse response) {

        param.setYczt00("1");
        List<PayDzmxb0> list = jdbcGateway.selectList("pay.dzmxb0.selectAllList", param);
        // 得到所有订单类型
        Map<String, String> ddlxmcMap = new HashMap<>();
        GlobalDict orderType = DictServiceImpl.getDictByKey000("order_type");
        if (orderType != null) {
            for (GlobalDict dict : orderType.getChildren().values()) {
                ddlxmcMap.put(dict.getKey000(), dict.getZdmc00());
            }
            for (PayDzmxb0 order : list) {
                // 存入订单类型名称
                if (StringUtils.isNotEmpty(ddlxmcMap.get(order.getDdlx00()))) {
                    order.setDdlx00(ddlxmcMap.get(order.getDdlx00()));
                } else {
                    order.setDdlx00(order.getDdlx00());
                }
            }
        }
        // 得到所有对账结果名称
        Map<String, String> dzjgmcMap = new HashMap<>();
        GlobalDict recordResult = DictServiceImpl.getDictByKey000("record_result");
        if (recordResult != null) {
            for (GlobalDict dict : recordResult.getChildren().values()) {
                dzjgmcMap.put(dict.getKey000(), dict.getZdmc00());
            }
            for (PayDzmxb0 order : list) {
                // 存入对账结果名称
                if (StringUtils.isNotEmpty(dzjgmcMap.get(order.getFwdzjg()))) {
                    order.setFwdzjg(dzjgmcMap.get(order.getFwdzjg()));
                } else {
                    order.setFwdzjg(order.getFwdzjg());
                }
                // 存入对账结果名称
                if (StringUtils.isNotEmpty(dzjgmcMap.get(order.getZfdzjg()))) {
                    order.setZfdzjg(dzjgmcMap.get(order.getZfdzjg()));
                } else {
                    order.setZfdzjg(order.getZfdzjg());
                }
            }
        }
        // 得到所有处理状态
        Map<String, String> clztmcMap = new HashMap<>();
        GlobalDict handleState = DictServiceImpl.getDictByKey000("handle_state");
        if (handleState != null) {
            for (GlobalDict dict : handleState.getChildren().values()) {
                clztmcMap.put(dict.getKey000(), dict.getZdmc00());
            }
            for (PayDzmxb0 order : list) {
                // 存入处理状态名称
                if (StringUtils.isNotEmpty(clztmcMap.get(order.getClzt00()))) {
                    order.setClzt00(clztmcMap.get(order.getClzt00()));
                } else {
                    order.setClzt00(order.getClzt00());
                }
            }
        }
        if (CollectionUtils.isEmpty(list)) {
            return "暂无数据";
        }

        ExcelData excelData = new ExcelData();
        List<String> titleList = new ArrayList();
        titleList.add("订单交易时间");
        titleList.add("系统订单号");
        titleList.add("业务类型");
        titleList.add("交易类型");
        titleList.add("患者姓名");
        titleList.add("就诊卡号");
        titleList.add("服务渠道");
        titleList.add("支付渠道");
        titleList.add("平台金额");
        titleList.add("服务渠道金额");
        titleList.add("支付渠道金额");
        titleList.add("对账结果");
        titleList.add("处理状态");
        titleList.add("处理方式");
        titleList.add("处理时间");
        titleList.add("处理人");
        excelData.setTitles(titleList);
        List<List<Object>> rows = new ArrayList();
        for (PayDzmxb0 bean : list) {
            List<Object> row = new ArrayList();
            row.add(DateUtils.toStr(bean.getDdcjsj()));
            row.add(bean.getXtddh0());
            row.add(bean.getDdlx00());
            row.add(bean.getYwlx00());
            row.add(bean.getHzxm00());
            row.add(bean.getKhzyh0());
            row.add(bean.getFwqdid());
            row.add(bean.getZfqd00());
            row.add(bean.getPtje00());
            row.add(bean.getFwqdje());
            row.add(bean.getZfptje());
            if ("对账一致".equals(bean.getFwdzjg()) && "对账一致".equals(bean.getZfdzjg())) {
                row.add("对账一致");
            } else if ("对账一致".equals(bean.getFwdzjg()) && !"对账一致".equals(bean.getZfdzjg())) {
                row.add(bean.getZfdzjg() + "￥" + bean.getZfcyje() + "元");
            } else if (!"对账一致".equals(bean.getFwdzjg()) && "对账一致".equals(bean.getZfdzjg())) {
                row.add(bean.getFwdzjg() + "￥" + bean.getFwcyje() + "元");
            } else {
                row.add(bean.getFwdzjg() + "￥" + bean.getFwcyje() + "元");
            }
            row.add(bean.getClzt00());
            row.add(bean.getClfs00());
            row.add(DateUtils.toStr(bean.getClsj00()));
            row.add(bean.getClr000());
            rows.add(row);
        }
        excelData.setRows(rows);
        try {
            ExcelUtils.exportExcel(response, "差异账统计", excelData);
        } catch (Exception e) {

        }
        return null;
    }

    /**
     * 差异账处理
     **/
    @Override
    @ApiExpose("cyzcl")
    @BusinessLog(title = "对账管理", businessType = BusinessType.UPDATE)
    public Object cyzcl(QueryParam param) {
        Map<String, Object> resultMap = new HashMap<>();
        PayDzmxb0 payDzmxb0 = jdbcGateway.selectOne("pay.dzmxb0.selectByPrimaryKey", param.getId0000());
        if ("2".equals(payDzmxb0.getFwdzjg())) { //服务渠道-长款
            // 是否需要走退款审批流程
            if (applicationProperty.getIsRefundApproval()){
                // 获取审批配置
                XtSpsz00 xtSpsz00 = jdbcGateway.selectOne("system.spsz00.select");
                if("02".contains(xtSpsz00.getYymk00())){
                    // 获取流程设置
                    JSONObject jsonObject = JSON.parseObject(xtSpsz00.getLcsz00());
                    // 获取流程发起人
                    String originator = jsonObject.getString("originator");
                    // 判断当前操作者是否在流程发起人中
                    if(!originator.contains(param.getClr000())){
                        throw new CustomException(HttpStatus.BAD_REQUEST, "暂无权限操作，请联系管理员！");
                    }
                    // 判断是否已发起退款审批流程
                    PaySpjlb0 paySpjlb0 = jdbcGateway.selectOne("pay.spjlb0.selectByGlddid", param.getId0000());
                    if(paySpjlb0 != null && !"2".equals(paySpjlb0.getSpzt00())){
                        throw new CustomException(HttpStatus.BAD_REQUEST, "该订单已发起退款审批流程，请勿重复操作！");
                    }

                    resultMap.put("sfyxtk", "1"); //0：否；1：是
                    return resultMap;
                } else {
                    payDzmxb0.setClzt00("1");
                    payDzmxb0.setClfs00("人工退款");
                    payDzmxb0.setClsj00(new Date());
                    payDzmxb0.setClr000(param.getClr000());

                    PayZfdd00 payZfdd00 = new PayZfdd00();
                    // 调用退款接口
                    String refundNo = SeqKit.genMhoOrderId();
                    if("充值".equals(payDzmxb0.getYwlx00())){
                        payZfdd00 = jdbcGateway.selectOne("pay.zfdd00.selectByXtddh0", payDzmxb0.getXtddh0());
                    } else if("退款".equals(payDzmxb0.getYwlx00())){
                        PayTkdd00 payTkdd00 = jdbcGateway.selectOne("pay.tkdd00.selectByXtddh0", payDzmxb0.getXtddh0());
                        payZfdd00 = jdbcGateway.selectOne("pay.zfdd00.selectByXtddh0", payTkdd00.getYxtddh());
                    }
                    if (payZfdd00 == null) {
                        throw new CustomException(HttpStatus.BAD_REQUEST, "订单不存在！");
                    }

                    if (!payZfdd00.getDdzt00().equals(PayZfdd00.STATE_SUCCESS)) {
                        throw new CustomException(HttpStatus.BAD_REQUEST, "订单状态不正确");
//                        throw new GeneralException("参数不能为空");
                    }

                    if (payZfdd00.getTkzje0() + payDzmxb0.getFwcyje() > payZfdd00.getZfje00()) {
                        throw new CustomException(HttpStatus.BAD_REQUEST, "退款金额超过订单可退款金额！");
                    }

                    RefundOrderCreateRequest request = new RefundOrderCreateRequest();
                    RefundOrderCreateReqModel model = new RefundOrderCreateReqModel();
                    request.setBizModel(model);

                    model.setAppId(payZfdd00.getFwqdid());
                    model.setPayOrderId(payZfdd00.getXtddh0());
                    model.setMchRefundNo(refundNo);
                    model.setRefundAmount(new Double(payDzmxb0.getFwcyje() * 100).longValue());
                    model.setRefundReason("差异账退款");
                    model.setChannelExtra(payZfdd00.getQdcs00());
                    model.setOperatorId(param.getClr000());
                    model.setOperatorName(param.getClrmc0());


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
                    payDzmxb0.setBz0000(refundNo);
                }
            } else {
                payDzmxb0.setClzt00("1");
                payDzmxb0.setClfs00("人工退款");
                payDzmxb0.setClsj00(new Date());
                payDzmxb0.setClr000(param.getClr000());

                PayZfdd00 payZfdd00 = new PayZfdd00();
                // 调用退款接口
                String refundNo = SeqKit.genMhoOrderId();
                if("充值".equals(payDzmxb0.getYwlx00())){
                    payZfdd00 = jdbcGateway.selectOne("pay.zfdd00.selectByXtddh0", payDzmxb0.getXtddh0());
                } else if("退款".equals(payDzmxb0.getYwlx00())){
                    PayTkdd00 payTkdd00 = jdbcGateway.selectOne("pay.tkdd00.selectByXtddh0", payDzmxb0.getXtddh0());
                    payZfdd00 = jdbcGateway.selectOne("pay.zfdd00.selectByXtddh0", payTkdd00.getYxtddh());
                }
                if (payZfdd00 == null) {
                    throw new CustomException(HttpStatus.BAD_REQUEST, "订单不存在！");
                }

                if (!payZfdd00.getDdzt00().equals(PayZfdd00.STATE_SUCCESS)) {
                    throw new CustomException(HttpStatus.BAD_REQUEST, "订单状态不正确");
                }

                if (payZfdd00.getTkzje0() + payDzmxb0.getFwcyje() > payZfdd00.getZfje00()) {
                    throw new CustomException(HttpStatus.BAD_REQUEST, "退款金额超过订单可退款金额！");
                }

                RefundOrderCreateRequest request = new RefundOrderCreateRequest();
                RefundOrderCreateReqModel model = new RefundOrderCreateReqModel();
                request.setBizModel(model);

                model.setAppId(payZfdd00.getFwqdid());
                model.setPayOrderId(payZfdd00.getXtddh0());
                model.setMchRefundNo(refundNo);
                model.setRefundAmount(new Double(payDzmxb0.getFwcyje() * 100).longValue());
                model.setRefundReason("差异账退款");
                model.setChannelExtra(payZfdd00.getQdcs00());
                model.setOperatorId(param.getClr000());
                model.setOperatorName(param.getClrmc0());


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
                payDzmxb0.setBz0000(refundNo);
            }
        } else if ("3".equals(payDzmxb0.getFwdzjg())) { //服务渠道-短款
            payDzmxb0.setClzt00("1");
            payDzmxb0.setClfs00("人工补款");
            payDzmxb0.setClsj00(new Date());
            payDzmxb0.setClr000(param.getClr000());
            payDzmxb0.setBz0000(param.getClnr00());
        } else if ("4".equals(payDzmxb0.getFwdzjg()) || "5".equals(payDzmxb0.getZfdzjg())) { //服务渠道-金额不一致 或者 支付渠道-特殊异常账
            payDzmxb0.setClzt00("1");
            payDzmxb0.setClfs00("人工平账");
            payDzmxb0.setClsj00(new Date());
            payDzmxb0.setClr000(param.getClr000());
            payDzmxb0.setBz0000(param.getClnr00());
        } else if ("3".equals(payDzmxb0.getZfdzjg())) { //支付渠道-短款
            payDzmxb0.setClzt00("1");
            payDzmxb0.setClfs00("人工补款");
            payDzmxb0.setClsj00(new Date());
            payDzmxb0.setClr000(param.getClr000());
            payDzmxb0.setBz0000(param.getClnr00());
        }
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
        return resultMap;
    }

    /**
     * 查看平账记录
     **/
    @Override
    @ApiExpose("pzjl")
    public Object pzjl(QueryParam param) {
        PayDzmxb0 order = jdbcGateway.selectOne("pay.dzmxb0.selectByPrimaryKey", param.getId0000());
        // 得到所有对账结果名称
        Map<String, String> dzjgmcMap = new HashMap<>();
        GlobalDict recordResult = DictServiceImpl.getDictByKey000("record_result");
        if (recordResult != null) {
            for (GlobalDict dict : recordResult.getChildren().values()) {
                dzjgmcMap.put(dict.getKey000(), dict.getZdmc00());
            }
            // 存入对账结果名称
            if (StringUtils.isNotEmpty(dzjgmcMap.get(order.getFwdzjg()))) {
                order.setFwdzjg(dzjgmcMap.get(order.getFwdzjg()));
            } else {
                order.setFwdzjg(order.getFwdzjg());
            }
            // 存入对账结果名称
            if (StringUtils.isNotEmpty(dzjgmcMap.get(order.getZfdzjg()))) {
                order.setZfdzjg(dzjgmcMap.get(order.getZfdzjg()));
            } else {
                order.setZfdzjg(order.getZfdzjg());
            }
            if ("对账一致".equals(order.getFwdzjg()) && "对账一致".equals(order.getZfdzjg())) {
                order.setDzjg00("对账一致");
                order.setDzjgsm("对账一致");
            } else if ("对账一致".equals(order.getFwdzjg()) && !"对账一致".equals(order.getZfdzjg())) {
                order.setDzjg00(order.getZfdzjg() + "￥" + order.getZfcyje() + "元");
                order.setDzjgsm(order.getZfdzjg());
                if (("短款".equals(order.getDzjgsm()) && "充值".equals(order.getYwlx00())) || ("长款".equals(order.getDzjgsm()) && "退款".equals(order.getYwlx00()))) {
                    // 系统对账时间往后延迟2天
                    Date date1 = DateUtil.offsetDay(order.getCjsj00(), 2).toJdkDate();
                    Date date2 = new Date();

                    long time1 = date1.getTime();
                    long time2 = date2.getTime();

                    if (time2 > time1) {
                        order.setYxcz00("1");
                    } else {
                        order.setYxcz00("0");
                    }
                }
            } else if (!"对账一致".equals(order.getFwdzjg()) && "对账一致".equals(order.getZfdzjg())) {
                order.setDzjg00(order.getFwdzjg() + "￥" + order.getFwcyje() + "元");
                order.setDzjgsm(order.getFwdzjg());
            } else {
                order.setDzjg00(order.getFwdzjg() + "￥" + order.getFwcyje() + "元");
                order.setDzjgsm(order.getFwdzjg());
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
    @ApiExpose("applyRefund")
    public void applyRefund(QueryParam param) {
        PayDzmxb0 payDzmxb0 = jdbcGateway.selectOne("pay.dzmxb0.selectByPrimaryKey", param.getId0000());
        // 是否需要走退款审批流程
        if (applicationProperty.getIsRefundApproval()){
            // 获取审批配置
            XtSpsz00 xtSpsz00 = jdbcGateway.selectOne("system.spsz00.select");
            if("02".contains(xtSpsz00.getYymk00())){
                // 获取流程设置
                JSONObject jsonObject = JSON.parseObject(xtSpsz00.getLcsz00());
                // 获取流程发起人
                String originator = jsonObject.getString("originator");
                // 判断当前操作者是否在流程发起人中
                if(!originator.contains(param.getClr000())){
                    throw new CustomException(HttpStatus.BAD_REQUEST, "暂无权限操作，请联系管理员！");
                }
                // 判断是否已发起退款审批流程
                String jlbid0 = "";
                PaySpjlb0 paySpjlb0 = jdbcGateway.selectOne("pay.spjlb0.selectByGlddid", param.getId0000());
                if(paySpjlb0 != null && !"2".equals(paySpjlb0.getSpzt00())){
                    throw new CustomException(HttpStatus.BAD_REQUEST, "该订单已发起退款审批流程，请勿重复操作！");
                }
                if(paySpjlb0 != null){
                    jlbid0 = paySpjlb0.getId0000();
                }
                // 插入审批记录
                approvalService.insertData(xtSpsz00, param.getId0000(), param.getClr000(), param.getClrmc0(), "长款退款申请", param.getTkyy00(), param.getTpfj00(), jlbid0);

                payDzmxb0.setClzt00("3");
                payDzmxb0.setClfs00("人工退款");
                payDzmxb0.setClsj00(new Date());
                payDzmxb0.setClr000(param.getClr000());

                jdbcGateway.update("pay.dzmxb0.updateByPrimaryKey", payDzmxb0);
            }
        }
    }

    public String countDzjg00(String dzjg00){
        Double ckje00 = 0.0;
        int ckbs00 = 0;
        Double dkje00 = 0.0;
        int dkbs00 = 0;
        Double byzje0 = 0.0;
        int bzybs0 = 0;
        Double tsje00 = 0.0;
        int tsbs00 = 0;
        String[] dzjg = dzjg00.split("\n");
        for(int i = 0;i < dzjg.length; i++){
            String data = dzjg[i];
            String type = data.split("￥")[0];
            if("长款".equals(type)){
                ckje00 = ckje00 + new Double(data.substring(data.indexOf("￥")+1,data.indexOf("元")));
                System.out.println(data.split("/")[1].replace("笔",""));
                ckbs00 = ckbs00 + Integer.valueOf(data.split("/")[1].replace("笔",""));
            } else if("短款".equals(type)){
                dkje00 = dkje00 + new Double(data.substring(data.indexOf("￥")+1,data.indexOf("元")));
                dkbs00 = dkbs00 + Integer.valueOf(data.split("/")[1].replace("笔",""));
            } else if("金额不一致".equals(type)){
                byzje0 = byzje0 + new Double(data.substring(data.indexOf("￥")+1,data.indexOf("元")));
                bzybs0 = bzybs0 + Integer.valueOf(data.split("/")[1].replace("笔",""));
            } else if("特殊异常账".equals(type)){
                tsje00 = tsje00 + new Double(data.substring(data.indexOf("￥")+1,data.indexOf("元")));
                tsbs00 = tsbs00 + Integer.valueOf(data.split("/")[1].replace("笔",""));
            }
        }
        StringBuffer sb = new StringBuffer();
        if(ckje00 > 0){
            sb.append("长款￥"+String.format("%.2f", ckje00)+"元/"+ckbs00+"笔\n");
        }
        if(dkje00 > 0){
            sb.append("短款￥"+String.format("%.2f", dkje00)+"元/"+dkbs00+"笔\n");
        }
        if(byzje0 > 0){
            sb.append("金额不一致￥"+String.format("%.2f", byzje0)+"元/"+bzybs0+"笔\n");
        }
        if(tsje00 > 0){
            sb.append("特殊异常账￥"+String.format("%.2f", tsje00)+"元/"+tsbs00+"笔\n");
        }
        if(sb.length() == 0){
            sb.append("对账一致");
        }
        return sb.toString();
    }

    public String countCljd00(String cljd00){
        int clbs00 = 0;
        String[] dzjg = cljd00.split("\n");
        for(int i = 0;i < dzjg.length; i++){
            String data = dzjg[i];
            if(data.contains("待处理")){
                clbs00 = clbs00 + Integer.valueOf(data.substring(0,data.indexOf("笔")));
            }
        }
        StringBuffer sb = new StringBuffer();
        if(clbs00 > 0){
            sb.append(clbs00 + "笔待处理\n");
        } else {
            sb.append("已处理\n");
        }
        return sb.toString();
    }
}
