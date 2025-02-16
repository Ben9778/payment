package com.ylz.yx.pay.order.refund;

import com.ylz.svc.web.HttpResponse;
import com.ylz.yx.pay.order.refund.model.PayTkdd00Param;
import com.ylz.yx.pay.order.refund.model.SelectParam;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface RefundService {

    Map<String, Object> queryList(SelectParam param) throws Exception;

    String expList(SelectParam param, HttpServletResponse response);

    Object orderDetail(SelectParam param);

    HttpResponse orderRefund(PayTkdd00Param param);
}
