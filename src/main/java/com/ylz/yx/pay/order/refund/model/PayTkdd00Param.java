package com.ylz.yx.pay.order.refund.model;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class PayTkdd00Param {
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
}
