package com.ylz.yx.pay.order.pay.model;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

@Data
public class PayZfdd00Param {
    /**
     * 主键ID
     */
    @NotBlank(message="系统订单号不能为空")
    private String id0000;

    /**
     * 操作员id
     */
    private String czyid0;

    /**
     * 操作员姓名
     */
    private String czyxm0;

    /**
     * 退款原因
     */
    private String tkyy00;

    /**
     * 图片附件
     */
    private String tpfj00;
}
