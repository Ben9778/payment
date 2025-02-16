package com.ylz.yx.pay.order.pay;

import com.ylz.svc.web.HttpResponse;
import com.ylz.yx.pay.order.pay.model.PayZfdd00Param;
import com.ylz.yx.pay.order.pay.model.SelectParam;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface PayService {

    Map<String, Object> queryList(SelectParam param);

    String expList(SelectParam param, HttpServletResponse response);

    Object orderDetail(SelectParam param);

    void refreshState(SelectParam param);

    Map<String, Object> orderRefund(PayZfdd00Param param);

    HttpResponse applyRefund(PayZfdd00Param param);

}
