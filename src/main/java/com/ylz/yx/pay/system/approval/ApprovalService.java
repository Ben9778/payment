package com.ylz.yx.pay.system.approval;

import com.alibaba.fastjson.JSONObject;
import com.ylz.yx.pay.system.approval.model.ApprovalParam;
import com.ylz.yx.pay.system.approval.model.PaySpbzb0;
import com.ylz.yx.pay.system.approval.model.QueryParam;
import com.ylz.yx.pay.system.approval.model.XtSpsz00;

import java.util.Map;

public interface ApprovalService {

    Map<String, Object> queryList(QueryParam param);

    Object queryDetail(QueryParam param);

    Object detail();

    void upd(XtSpsz00 xtSpsz00);

    void approval(ApprovalParam approvalParam);

    void insertData(XtSpsz00 xtSpsz00, String id0000, String czyid0, String czyxm0, String splcmc, String sqyy00, String tpfj00, String jlbid0);
}
