package com.ylz.yx.pay.system.approval.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

@Data
public class PaySpbzb0 {
    /**
     * 主键ID
     */
    private String id0000;

    /**
     * 审批记录表ID
     */
    private String jlbid0;

    /**
     * 审批步骤（0：发起人；1：一级审批；2：二级审批；3：三级审批；4：四级审批）
     */
    private String spbz00;

    /**
     * 审批时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date spsj00;

    /**
     * 审批人
     */
    private String spr000;

    /**
     * 审批人姓名
     */
    private String sprxm0;

    /**
     * 审批结果
     */
    private String spjg00;

    /**
     * 申请原因
     */
    private String sqyy00;

    /**
     * 图片附件
     */
    private String tpfj00;

    /**
     * 文档附件
     */
    private String wdfj00;
}