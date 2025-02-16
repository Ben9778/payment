package com.ylz.yx.pay.system.ftp;

import com.ylz.yx.pay.system.ftp.model.QueryParam;
import com.ylz.yx.pay.system.ftp.model.XtFtp000;

import java.util.Map;

public interface FtpService {

    Map<String, Object> queryList(QueryParam param);

    Object detail(XtFtp000 xtFtp000);

    void add(XtFtp000 xtFtp000);

    void upd(XtFtp000 xtFtp000);

    void del(XtFtp000 xtFtp000);


}
