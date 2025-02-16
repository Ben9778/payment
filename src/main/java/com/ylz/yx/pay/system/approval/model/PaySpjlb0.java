package com.ylz.yx.pay.system.approval.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class PaySpjlb0 {
    /**
     * 主键ID
     */
    private String id0000;

    /**
     * 关联订单主键ID
     */
    private String glddid;

    /**
     * 审批流程名称
     */
    private String splcmc;

    /**
     * 流程发起人
     */
    private String lcfqr0;

    /**
     * 审批状态（0：审批中；1：审批通过；2：审批拒绝）
     */
    private String spzt00;

    /**
     * 发起时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date fqsj00;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date wcsj00;

}