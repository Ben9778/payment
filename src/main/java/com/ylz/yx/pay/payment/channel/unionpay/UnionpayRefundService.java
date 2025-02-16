package com.ylz.yx.pay.payment.channel.unionpay;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.ylz.core.logging.Logger;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.entity.PayTkdd00;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.core.model.params.union.UnionpayMchParams;
import com.ylz.yx.pay.payment.channel.AbstractRefundService;
import com.ylz.yx.pay.payment.channel.unionpay.utils.UnionSignUtils;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import com.ylz.yx.pay.payment.rqrs.refund.RefundOrderRQ;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*
 * 退款接口： 银联官方
 */
@Service
public class UnionpayRefundService extends AbstractRefundService {

    private static final Logger log = new Logger(UnionpayRefundService.class.getName());

    @Override
    public String getIfCode() {
        return CS.IF_CODE.UNIONPAY;
    }

    @Autowired
    private UnionpayPaymentService unionpayPaymentService;

    @Override
    public String preCheck(RefundOrderRQ bizRQ, PayTkdd00 payTkdd00, PayZfdd00 payZfdd00) {
        return null;
    }

    @Override
    public ChannelRetMsg refund(RefundOrderRQ bizRQ, PayTkdd00 payTkdd00, PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext) throws Exception {
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        String logPrefix = "【银联退款】";
        try {
            Map<String, String> data = new HashMap<String, String>();

            /***银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改***/
            data.put("encoding", "UTF-8");      //字符集编码 可以使用UTF-8,GBK两种方式
            data.put("txnType", "04");          //交易类型 04-退货
            data.put("txnSubType", "00");       //交易子类型  默认00
            data.put("bizType", "000000");      //填写000000
            data.put("channelType", "08");      //渠道类型，07-PC，08-手机

            /***商户接入参数***/
            data.put("accessType", "0");                         //接入类型，商户接入固定填0，不需修改
            data.put("orderId", payTkdd00.getXtddh0());          //商户订单号，8-40位数字字母，不能含“-”或“_”，可以自行定制规则，重新产生，不同于原消费
            data.put("txnTime", DateUtil.format(new Date(), DatePattern.PURE_DATETIME_PATTERN));      //订单发送时间，格式为YYYYMMDDhhmmss，必须取当前时间，否则会报txnTime无效
            data.put("currencyCode", "156");                     //交易币种（境内商户一般是156 人民币）
            data.put("txnAmt", String.valueOf(payTkdd00.getTkje00()));                          //****退货金额，单位分，不要带小数点。退货金额小于等于原消费金额，当小于的时候可以多次退货至退货累计金额等于原消费金额
            data.put("backUrl", getNotifyUrl());               //后台通知地址，后台通知参数详见open.unionpay.com帮助中心 下载  产品接口规范  网关支付产品接口规范 退货交易 商户通知,其他说明同消费交易的后台通知
            data.put("origQryId", payZfdd00.getZfddh0());                     //****原消费交易返回的的queryId，可以从消费交易后台通知接口中或者交易状态查询接口中获取

            // 请求方保留域，
            // 透传字段，查询、通知、对账文件中均会原样出现，如有需要请启用并修改自己希望透传的数据。
            // 出现部分特殊字符时可能影响解析，请按下面建议的方式填写：
            // 1. 如果能确定内容不会出现&={}[]"'等符号时，可以直接填写数据，建议的方法如下。
            // data.put("reqReserved", "透传信息1|透传信息2|透传信息3");
            // 2. 内容可能出现&={}[]"'符号时：
            // 1) 如果需要对账文件里能显示，可将字符替换成全角＆＝｛｝【】“‘字符（自己写代码，此处不演示）；
            // 2) 如果对账文件没有显示要求，可做一下base64（如下）。
            //    注意控制数据长度，实际传输的数据长度不能超过1024位。
            //    查询、通知等接口解析时使用new String(Base64.decodeBase64(reqReserved), DemoBase.encoding);解base64后再对数据做后续解析。
            // data.put("reqReserved", Base64.encodeBase64String("任意格式的信息都可以".toString().getBytes(DemoBase.encoding)));

            // 发送请求
            Map<String, String> rspData = unionpayPaymentService.packageParamAndReq("/gateway/api/backTransReq.do", data, logPrefix, mchAppConfigContext);

            if (!rspData.isEmpty()) {
                UnionpayMchParams unionpayMchParams = (UnionpayMchParams) configContextQueryService.queryMchParams(mchAppConfigContext.getFwqdid(), getIfCode());
                if (UnionSignUtils.validate(rspData, applicationProperty.getFileStoragePath(), unionpayMchParams, "UTF-8")) {
                    log.info("验证签名成功");
                    String respCode = rspData.get("respCode"); //应答码
                    String respMsg = rspData.get("respMsg"); //应答信息
                    if (("00").equals(respCode)) {
                        channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                        channelRetMsg.setChannelOrderId(rspData.get("queryId"));
                    } else if (("03").equals(respCode) || ("04").equals(respCode) || ("05").equals(respCode)) {
                        //后续需发起交易状态查询交易确定交易状态
                        channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
                    } else {
                        //其他应答码为失败请排查原因
                        channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                        channelRetMsg.setChannelErrCode(respCode);
                        channelRetMsg.setChannelErrMsg(respMsg);
                    }
                } else {
                    log.error("验证签名失败");
                }
            } else {
                //未返回正确的http状态
                log.error("未获取到返回报文或返回http状态码非200");
            }
        } catch (Exception e) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.SYS_ERROR); // 系统异常
        }
        return channelRetMsg;
    }

    @Override
    public ChannelRetMsg query(PayTkdd00 payTkdd00, MchAppConfigContext mchAppConfigContext) throws Exception {
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        String logPrefix = "【银联退款查询】";
        try {
            Map<String, String> data = new HashMap<String, String>();

            /***银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改***/
            data.put("encoding", "UTF-8");      //字符集编码 可以使用UTF-8,GBK两种方式
            data.put("txnType", "00");          //交易类型 00-默认
            data.put("txnSubType", "00");       //交易子类型  默认00
            data.put("bizType", "000201");      //业务类型

            /***商户接入参数***/
            data.put("accessType", "0");                         //接入类型，商户接入固定填0，不需修改
            data.put("orderId", payTkdd00.getXtddh0());          //商户订单号，8-40位数字字母，不能含“-”或“_”，可以自行定制规则，重新产生，不同于原消费
            data.put("txnTime", DateUtil.format(new Date(), DatePattern.PURE_DATETIME_PATTERN));      //订单发送时间，格式为YYYYMMDDhhmmss，必须取当前时间，否则会报txnTime无效

            // 发送请求
            Map<String, String> rspData = unionpayPaymentService.packageParamAndReq("/gateway/api/backTransReq.do", data, logPrefix, mchAppConfigContext);

            if (!rspData.isEmpty()) {
                UnionpayMchParams unionpayMchParams = (UnionpayMchParams) configContextQueryService.queryMchParams(mchAppConfigContext.getFwqdid(), getIfCode());
                if (UnionSignUtils.validate(rspData, applicationProperty.getFileStoragePath(), unionpayMchParams, "UTF-8")) {
                    log.info("验证签名成功");
                    String respCode = rspData.get("respCode"); //应答码
                    String respMsg = rspData.get("respMsg"); //应答信息
                    if (("00").equals(respCode)) {//如果查询交易成功
                        String origRespCode = rspData.get("origRespCode"); //原交易应答码
                        String origRespMsg = rspData.get("origRespMsg"); //原交易应答信息
                        if (("00").equals(origRespCode)) {
                            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                            log.info(logPrefix + " >>> 退款成功");
                        } else if (("03").equals(origRespCode) ||
                                ("04").equals(origRespCode) ||
                                ("05").equals(origRespCode)) {
                            //订单处理中或交易状态未明，需稍后发起交易状态查询交易 【如果最终尚未确定交易是否成功请以对账文件为准】

                        } else {
                            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                            channelRetMsg.setChannelErrCode(respCode);
                            channelRetMsg.setChannelErrMsg(respMsg);
                            log.info(logPrefix + " >>> 退款失败, " + origRespMsg);
                        }
                    } else if (("34").equals(rspData.get("respCode"))) {
                        //订单不存在，可认为交易状态未明，需要稍后发起交易状态查询，或依据对账结果为准

                    } else {//查询交易本身失败，如应答码10/11检查查询报文是否正确

                    }
                } else {
                    log.error("验证签名失败");
                }
            } else {
                //未返回正确的http状态
                log.error("未获取到返回报文或返回http状态码非200");
            }
        } catch (Exception e) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.SYS_ERROR); // 系统异常
        }
        return channelRetMsg;
    }

}
