package com.ylz.yx.pay.record.controller;

import com.ylz.yx.pay.record.RecordService;
import com.ylz.yx.pay.record.model.QueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/record")
public class RecordController {

    @Autowired
    RecordService recordService;

    /**
     * 每日对账汇总数据导出
     **/
    @GetMapping("/expmrdzhz")
    public String expmrdzhz(QueryParam param, HttpServletResponse response) {
        return recordService.expmrdzhz(param, response);
    }

    /**
     * 支付渠道统计汇总数据导出
     **/
    @GetMapping("/expzfqddzhz")
    public String expzfqddzhz(QueryParam param, HttpServletResponse response) {
        return recordService.expzfqddzhz(param, response);
    }

    /**
     * 服务渠道统计汇总数据导出
     **/
    @GetMapping("/expfwqddzhz")
    public String expfwqddzhz(QueryParam param, HttpServletResponse response) {
        return recordService.expfwqddzhz(param, response);
    }

    /**
     * 对账明细统计导出
     **/
    @GetMapping("/expdzmxtj")
    public String expdzmxtj(QueryParam param, HttpServletResponse response) {
        return recordService.expdzmxtj(param, response);
    }

    /**
     * 差异账统计导出
     **/
    @GetMapping("/expcyztj")
    public String expcyztj(QueryParam param, HttpServletResponse response) {
        return recordService.expcyztj(param, response);
    }
}
