package com.ylz.yx.pay.record.model;

import lombok.Data;

@Data
public class QueryParam {

    private String id0000; //主键ID

    private String clr000; //处理人

    private String clrmc0; //处理人

    private String clnr00; //处理内容

    private String ksrq00; //开始日期

    private String jsrq00; //结束日期

    private String type;

    private String jylx00; //交易类型

    private String ywlx00; //业务类型

    private String fwqd00; //服务渠道

    private String content; //查询条件

    private String zfqd00; //支付渠道

    private String dzjg00; //对账结果

    private String clzt00; //处理状态

    private String clfs00; //处理方式

    private String yczt00; //异常状态

    private String tkyy00; //退款原因

    private String tpfj00; //图片附件

    private String pageSize;

    private String pageIndex;
}
