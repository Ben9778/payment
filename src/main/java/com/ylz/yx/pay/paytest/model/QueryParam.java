package com.ylz.yx.pay.paytest.model;

import lombok.Data;

@Data
public class QueryParam {
    private String amount;

    private String wayCode;

    private String authCode;
}
