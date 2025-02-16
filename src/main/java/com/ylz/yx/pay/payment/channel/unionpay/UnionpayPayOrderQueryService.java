package com.ylz.yx.pay.payment.channel.unionpay;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.ylz.core.logging.Logger;
import com.ylz.yx.pay.config.ApplicationProperty;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.core.model.params.union.UnionpayMchParams;
import com.ylz.yx.pay.payment.channel.IPayOrderQueryService;
import com.ylz.yx.pay.payment.channel.unionpay.utils.UnionSignUtils;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import com.ylz.yx.pay.payment.service.ConfigContextQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * 银联查单
 */
@Service
public class UnionpayPayOrderQueryService implements IPayOrderQueryService {

    private static final Logger log = new Logger(UnionpayPayOrderQueryService.class.getName());

    @Override
    public String getIfCode() {
        return CS.IF_CODE.UNIONPAY;
    }

    @Autowired
    private UnionpayPaymentService unionpayPaymentService;
    @Autowired
    protected ConfigContextQueryService configContextQueryService;
    @Resource
    protected ApplicationProperty applicationProperty;

    @Override
    public ChannelRetMsg query(PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext) throws Exception {
        String logPrefix = "【银联查单】";

        try {
            Map<String, String> data = new HashMap<String, String>();

            /***银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改***/
            data.put("encoding", "UTF-8");      //字符集编码 可以使用UTF-8,GBK两种方式
            data.put("txnType", "00");          //交易类型 00-默认
            data.put("txnSubType", "00");       //交易子类型  默认00
            data.put("bizType", "000201");      //业务类型

            /***商户接入参数***/
            data.put("accessType", "0");                         //接入类型，商户接入固定填0，不需修改
            data.put("orderId", payZfdd00.getXtddh0());          //商户订单号，8-40位数字字母，不能含“-”或“_”，可以自行定制规则，重新产生，不同于原消费
            data.put("txnTime", DateUtil.format(new Date(), DatePattern.PURE_DATETIME_PATTERN));      //订单发送时间，格式为YYYYMMDDhhmmss，必须取当前时间，否则会报txnTime无效

            // 发送请求
            Map<String, String> rspData = unionpayPaymentService.packageParamAndReq("/gateway/api/queryTrans.do", data, logPrefix, mchAppConfigContext);

            if(!rspData.isEmpty()){
                UnionpayMchParams unionpayMchParams = (UnionpayMchParams) configContextQueryService.queryMchParams(mchAppConfigContext.getFwqdid(), getIfCode());
                if(UnionSignUtils.validate(rspData, applicationProperty.getFileStoragePath(), unionpayMchParams, "UTF-8")){
                    log.info("验证签名成功");
                    String respCode = rspData.get("respCode"); //应答码
                    if(("00").equals(respCode)){//如果查询交易成功
                        String origRespCode = rspData.get("origRespCode"); //原交易应答码
                        if(("00").equals(origRespCode)){
                            //交易成功，更新商户订单状态
                            return ChannelRetMsg.confirmSuccess(rspData.get("queryId"));  //支付成功
                        }else{
                            return ChannelRetMsg.waiting(); //支付中
                        }
                    }else{
                        return ChannelRetMsg.waiting(); //支付中
                    }
                }else{
                    log.error("验证签名失败");
                }
            }else{
                //未返回正确的http状态
                log.error("未获取到返回报文或返回http状态码非200");
            }
            return ChannelRetMsg.waiting(); //支付中
        }catch (Exception e) {
            return ChannelRetMsg.waiting(); //支付中
        }
    }

}
