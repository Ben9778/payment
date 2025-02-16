package com.ylz.yx.pay.core.model.params.pospay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ylz.yx.pay.core.model.params.NormalMchParams;
import lombok.Data;

/*
 * 银联POS机 普通商户参数定义
 */
@Data
public class PospayMchParams extends NormalMchParams {

    /** FTP地址 */
    private String ftpUrl;

    /** FTP用户 */
    private String ftpUser;

    /** FTP密码 */
    private String ftpPwd;

    /** FTP文件名 */
    private String ftpFileName;

    @Override
    public String deSenData() {
        PospayMchParams isvParams = this;
        return ((JSONObject) JSON.toJSON(isvParams)).toJSONString();
    }
}
