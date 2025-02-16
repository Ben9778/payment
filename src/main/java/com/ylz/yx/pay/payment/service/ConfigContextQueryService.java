package com.ylz.yx.pay.payment.service;

import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.yx.pay.config.ApplicationProperty;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.entity.PayFwqd00;
import com.ylz.yx.pay.core.entity.PayZfqd00;
import com.ylz.yx.pay.core.model.params.NormalMchParams;
import com.ylz.yx.pay.core.model.params.alipay.AlipayMchParams;
import com.ylz.yx.pay.core.model.params.wxpay.WxpayMchParams;
import com.ylz.yx.pay.payment.model.AlipayClientWrapper;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.model.WxServiceWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/*
* 配置信息查询服务 （兼容 缓存 和 直接查询方式）
*/
@Service
public class ConfigContextQueryService {

    @Autowired ConfigContextService configContextService;
    @Autowired private JdbcGateway jdbcGateway;

    private boolean isCache(){
        return ApplicationProperty.IS_USE_CACHE;
    }

    //获取商户的渠道秘钥
    public PayFwqd00 queryMchApp(String fwqdid){

        if(isCache()){
            return configContextService.getMchAppConfigContext(fwqdid).getPayFwqd00();
        }
        //根据服务渠道ID查询服务渠道信息
        return jdbcGateway.selectOne("pay.fwqd00.selectByPrimaryKey", fwqdid);
    }
    //根据服务渠道ID查询服务渠道信息和appid
    public MchAppConfigContext queryMchInfoAndAppInfo(String fwqdid){

        if(isCache()){
            return configContextService.getMchAppConfigContext(fwqdid);
        }

        PayFwqd00 payFwqd00 = jdbcGateway.selectOne("pay.fwqd00.selectByPrimaryKey", fwqdid);

        if(payFwqd00 == null) {
            return null;
        }

        MchAppConfigContext result = new MchAppConfigContext();
        result.setFwqdid(fwqdid);
        result.setPayFwqd00(payFwqd00);

        return result;
    }


    public NormalMchParams queryMchParams(String fwqdid, String qdbm00){

        if(isCache()){
            return configContextService.getMchAppConfigContext(fwqdid).getNormalMchParamsByIfCode(fwqdid,qdbm00);
        }
        Map<String, Object> map = new HashMap();
        map.put("fwqdid", fwqdid);
        map.put("qdbm00", qdbm00);

        // 查询服务渠道的所有支持的参数配置
        PayZfqd00 payZfqd00 = jdbcGateway.selectOne("pay.zfqd00.selectByQdbm00", map);

        if(payZfqd00 == null){
            return null;
        }

        return NormalMchParams.factory(payZfqd00.getQdbm00(), payZfqd00.getQdpz00());
    }

    public AlipayClientWrapper getAlipayClientWrapper(MchAppConfigContext mchAppConfigContext){

        if(isCache()){
            return configContextService.getMchAppConfigContext(mchAppConfigContext.getFwqdid()).getAlipayClientWrapper();
        }

        AlipayMchParams alipayParams = (AlipayMchParams)queryMchParams(mchAppConfigContext.getFwqdid(), CS.IF_CODE.ALIPAY);
        return AlipayClientWrapper.buildAlipayClientWrapper(alipayParams);

    }

    public WxServiceWrapper getWxServiceWrapper(MchAppConfigContext mchAppConfigContext){

        if(isCache()){
            return configContextService.getMchAppConfigContext(mchAppConfigContext.getFwqdid()).getWxServiceWrapper();
        }

        WxpayMchParams wxParams = (WxpayMchParams)queryMchParams(mchAppConfigContext.getFwqdid(), CS.IF_CODE.WXPAY);
        return WxServiceWrapper.buildWxServiceWrapper(wxParams);

    }

}
