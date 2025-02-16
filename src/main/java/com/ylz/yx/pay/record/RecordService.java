package com.ylz.yx.pay.record;

import com.ylz.svc.web.HttpResponse;
import com.ylz.yx.pay.record.model.QueryParam;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public interface RecordService {

    Object cxhzsj(QueryParam param);

    Map<String, Object> mrdzhz(QueryParam param);

    String expmrdzhz(QueryParam param, HttpServletResponse response);

    Map<String, Object> zfqddzhz(QueryParam param);

    String expzfqddzhz(QueryParam param, HttpServletResponse response);

    Map<String, Object> fwqddzhz(QueryParam param);

    String expfwqddzhz(QueryParam param, HttpServletResponse response);

    Map<String, Object> dzmxtj(QueryParam param);

    String expdzmxtj(QueryParam param, HttpServletResponse response);

    Map<String, Object> cyztj(QueryParam param);

    String expcyztj(QueryParam param, HttpServletResponse response);

    Object cyzcl(QueryParam param);

    Object pzjl(QueryParam param);

    void applyRefund(QueryParam param);
}
