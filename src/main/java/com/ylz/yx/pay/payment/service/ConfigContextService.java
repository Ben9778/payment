package com.ylz.yx.pay.payment.service;

import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.yx.pay.config.ApplicationProperty;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.entity.PayFwqd00;
import com.ylz.yx.pay.core.entity.PayZfqd00;
import com.ylz.yx.pay.core.model.params.NormalMchParams;
import com.ylz.yx.pay.core.model.params.alipay.AlipayMchParams;
import com.ylz.yx.pay.core.model.params.wxpay.WxpayMchParams;
import com.ylz.yx.pay.payment.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
* 配置信息上下文服务
*/
@Service
public class ConfigContextService {

    @Autowired
    private JdbcGateway jdbcGateway;

    /** <应用ID, 商户配置上下文>  **/
    private static final Map<String, MchAppConfigContext> mchAppConfigContextMap = new ConcurrentHashMap<>();


    /** 获取 [商户应用支付参数配置信息] **/
    public MchAppConfigContext getMchAppConfigContext(String fwqdid){

        MchAppConfigContext mchAppConfigContext = mchAppConfigContextMap.get(fwqdid);

        //无此数据， 需要初始化
        if(mchAppConfigContext == null){
            initMchAppConfigContext(fwqdid);
        }

        return mchAppConfigContextMap.get(fwqdid);
    }

    /** 初始化 [商户应用支付参数配置信息] **/
    public synchronized void initMchAppConfigContext(String fwqdid) {

        if (!isCache()) { // 当前系统不进行缓存
            return;
        }

        // 查询服务渠道信息主体
        PayFwqd00 payFwqd00 = jdbcGateway.selectOne("pay.fwqd00.selectByPrimaryKey", fwqdid);

        //DB已经删除
        if (payFwqd00 == null) {
            mchAppConfigContextMap.remove(fwqdid);  //清除缓存信息
            return;
        }

        MchAppConfigContext mchAppConfigContext = new MchAppConfigContext();
        // 设置商户信息
        mchAppConfigContext.setFwqdid(fwqdid);
        mchAppConfigContext.setPayFwqd00(payFwqd00);

        // 查询服务渠道的所有支持的参数配置
        List<PayZfqd00> payZfqd00List = jdbcGateway.selectList("pay.zfqd00.selectListByFwqdid", fwqdid);

        for (PayZfqd00 payZfqd00 : payZfqd00List) {
            Map<String, NormalMchParams> map = new HashMap();
            map.put(payZfqd00.getQdbm00(), NormalMchParams.factory(payZfqd00.getQdbm00(), payZfqd00.getQdpz00()));
            mchAppConfigContext.getNormalMchParamsMap().put(
                    fwqdid,
                    map
            );
        }

        //放置alipay client
        AlipayMchParams alipayParams = mchAppConfigContext.getNormalMchParamsByIfCode(CS.IF_CODE.ALIPAY, AlipayMchParams.class);
        if (alipayParams != null) {
            mchAppConfigContext.setAlipayClientWrapper(AlipayClientWrapper.buildAlipayClientWrapper(alipayParams));
        }

        //放置 wxJavaService
        WxpayMchParams wxpayParams = mchAppConfigContext.getNormalMchParamsByIfCode(CS.IF_CODE.WXPAY, WxpayMchParams.class);
        if (wxpayParams != null) {
            mchAppConfigContext.setWxServiceWrapper(WxServiceWrapper.buildWxServiceWrapper(wxpayParams));
        }

        mchAppConfigContextMap.put(fwqdid, mchAppConfigContext);
    }

    private boolean isCache(){
        return ApplicationProperty.IS_USE_CACHE;
    }

}
