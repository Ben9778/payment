package com.ylz.yx.pay.payment.channel.unionpay;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.ylz.core.logging.Logger;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.core.model.params.union.UnionpayMchParams;
import com.ylz.yx.pay.payment.channel.AbstractPaymentService;
import com.ylz.yx.pay.payment.channel.unionpay.utils.UnionHttpUtil;
import com.ylz.yx.pay.payment.channel.unionpay.utils.UnionSignUtils;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.AbstractRS;
import com.ylz.yx.pay.payment.rqrs.payorder.UnifiedOrderRQ;
import com.ylz.yx.pay.payment.util.PaywayUtil;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * 银联下单
 */
@Service
public class UnionpayPaymentService extends AbstractPaymentService {

    private static final Logger log = new Logger(UnionpayPaymentService.class.getName());

    @Override
    public String getIfCode() {
        return CS.IF_CODE.UNIONPAY;
    }

    @Override
    public boolean isSupport(String wayCode) {
        return true;
    }

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayZfdd00 payZfdd00) {
        return PaywayUtil.getRealPaywayService(this, payZfdd00.getZffs00()).preCheck(rq, payZfdd00);
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext) throws Exception {
        return PaywayUtil.getRealPaywayService(this, payZfdd00.getZffs00()).pay(rq, payZfdd00, mchAppConfigContext);
    }

    /** 封装参数 & 统一请求 **/
    public Map<String, String> packageParamAndReq(String apiUri, Map<String, String> reqParams, String logPrefix, MchAppConfigContext mchAppConfigContext) throws Exception {

        UnionpayMchParams unionpayMchParams = (UnionpayMchParams) configContextQueryService.queryMchParams(mchAppConfigContext.getFwqdid(), getIfCode());
        reqParams.put("version", unionpayMchParams.getVersion());            //版本号 全渠道默认值
        reqParams.put("signMethod", unionpayMchParams.getSignMethod()); //签名方法
        reqParams.put("merId", unionpayMchParams.getMerId()); // 商户号

        //签名
        String isvPrivateCertFile = channelCertConfigKitBean.getCertFilePath(unionpayMchParams.getPrivateCertFile());
        String isvPrivateCertPwd = unionpayMchParams.getPrivateCertPwd();
        Map<String, String> reqData = UnionSignUtils.signByCertInfo(reqParams, isvPrivateCertFile, isvPrivateCertPwd, "UTF-8");			 //报文中certId,signature的值是在signData方法中获取并自动赋值的，只要证书配置正确即可。

        // 调起上游接口
        log.info(logPrefix + " reqParams=" + reqParams);
        Map<String, String> rspData = UnionHttpUtil.post(reqData,unionpayMchParams.getGatewayUrl() + apiUri, unionpayMchParams.isIfValidateRemoteCert()); //发送请求报文并接受同步应答（默认连接超时时间30秒，读取返回结果超时时间30秒）;这里调用signData之后，调用submitUrl之前不能对submitFromData中的键值对做任何修改，如果修改会导致验签不通过
        log.info(logPrefix + " resParams=" + rspData);

        return rspData;
    }

    /** 银联公共参数赋值 **/
    public static void unionPublicParams(Map<String, String> reqParams, PayZfdd00 payZfdd00) {
        /***银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改***/
        reqParams.put("encoding", "UTF-8");     //字符集编码 可以使用UTF-8,GBK两种方式
        //获取订单类型
        reqParams.put("orderId", payZfdd00.getXtddh0()); //订单号
        reqParams.put("txnTime", DateUtil.format(new Date(), DatePattern.PURE_DATETIME_PATTERN)); //订单时间 如：20180702142900
        reqParams.put("txnAmt", String.valueOf(payZfdd00.getZfje00())); //交易金额 单位：分，不带小数点
        reqParams.put("currencyCode", "156"); //交易币种 不出现则默认为人民币-156
    }
}
