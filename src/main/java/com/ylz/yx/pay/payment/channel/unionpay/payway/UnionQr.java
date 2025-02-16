package com.ylz.yx.pay.payment.channel.unionpay.payway;

import com.ylz.core.logging.Logger;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.core.model.params.union.UnionpayMchParams;
import com.ylz.yx.pay.payment.channel.unionpay.UnionpayPaymentService;
import com.ylz.yx.pay.payment.channel.unionpay.utils.UnionSignUtils;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.AbstractRS;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import com.ylz.yx.pay.payment.rqrs.payorder.UnifiedOrderRQ;
import com.ylz.yx.pay.payment.rqrs.payorder.payway.UnionQrOrderRQ;
import com.ylz.yx.pay.payment.rqrs.payorder.payway.UnionQrOrderRS;
import com.ylz.yx.pay.payment.util.ApiResBuilder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/*
 * 银联 QR支付
 */
@Service("unionPaymentByUnionQrService") //Service Name需保持全局唯一性
public class UnionQr extends UnionpayPaymentService {

    private static final Logger log = new Logger(UnionQr.class.getName());

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayZfdd00 payZfdd00) {
        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext) throws Exception {
        String logPrefix = "【银联条码(unionpay)支付】";

        UnionQrOrderRQ bizRQ = (UnionQrOrderRQ) rq;
        UnionQrOrderRS res = ApiResBuilder.buildSuccess(UnionQrOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);

        Map<String, String> contentData = new HashMap<String, String>();

        /***银联全渠道系统，产品参数，除了encoding自行选择外其他不需修改***/
        contentData.put("txnType", "01");              		 	//交易类型 01:消费
        contentData.put("txnSubType", "07");           		 	//交易子类 07：申请消费二维码
        contentData.put("bizType", "000000");          		 	//填写000000
        contentData.put("channelType", "08");          		 	//渠道类型 08手机

        /***商户接入参数***/
        contentData.put("accessType", "0");            		 	//接入类型，商户接入填0 ，不需修改（0：直连商户， 1： 收单机构 2：平台商户）

        // 请求方保留域，
        // 透传字段，查询、通知、对账文件中均会原样出现，如有需要请启用并修改自己希望透传的数据。
        // 出现部分特殊字符时可能影响解析，请按下面建议的方式填写：
        // 1. 如果能确定内容不会出现&={}[]"'等符号时，可以直接填写数据，建议的方法如下。
//		contentData.put("reqReserved", "透传信息1|透传信息2|透传信息3");
        // 2. 内容可能出现&={}[]"'符号时：
        // 1) 如果需要对账文件里能显示，可将字符替换成全角＆＝｛｝【】“‘字符（自己写代码，此处不演示）；
        // 2) 如果对账文件没有显示要求，可做一下base64（如下）。
        //    注意控制数据长度，实际传输的数据长度不能超过1024位。
        //    查询、通知等接口解析时使用new String(Base64.decodeBase64(reqReserved), DemoBase.encoding);解base64后再对数据做后续解析。
//		contentData.put("reqReserved", Base64.encodeBase64String("任意格式的信息都可以".toString().getBytes(DemoBase.encoding)));

        //后台通知地址（需设置为外网能访问 http https均可），支付成功后银联会自动将异步通知报文post到商户上送的该地址，【支付失败的交易银联不会发送后台通知】
        //后台通知参数详见open.unionpay.com帮助中心 下载  产品接口规范  网关支付产品接口规范 消费交易 商户通知
        //注意:1.需设置为外网能访问，否则收不到通知    2.http https均可  3.收单后台通知后需要10秒内返回http200或302状态码
        //    4.如果银联通知服务器发送通知后10秒内未收到返回状态码或者应答码非http200或302，那么银联会间隔一段时间再次发送。总共发送5次，银联后续间隔1、2、4、5 分钟后会再次通知。
        //    5.后台通知地址如果上送了带有？的参数，例如：http://abc/web?a=b&c=d 在后台通知处理程序验证签名之前需要编写逻辑将这些字段去掉再验签，否则将会验签失败
        contentData.put("backUrl", getNotifyUrl());

        // 银联 bar 统一参数赋值
        unionPublicParams(contentData, payZfdd00);

        // 发送请求
        Map<String, String> rspData = packageParamAndReq("/gateway/api/backTransReq.do", contentData, logPrefix, mchAppConfigContext);

        if(!rspData.isEmpty()){
            UnionpayMchParams unionpayMchParams = (UnionpayMchParams) configContextQueryService.queryMchParams(mchAppConfigContext.getFwqdid(), getIfCode());
            if(UnionSignUtils.validate(rspData, applicationProperty.getFileStoragePath(), unionpayMchParams, "UTF-8")){
                log.info("验证签名成功");
                String respCode = rspData.get("respCode"); //应答码
                String respMsg = rspData.get("respMsg"); //应答信息
                if(("00").equals(respCode)){
                    if(CS.PAY_DATA_TYPE.CODE_IMG_URL.equals(bizRQ.getPayDataType())){ //二维码地址
                        res.setCodeImgUrl(applicationProperty.genScanImgUrl(rspData.get("qrCode")));
                    }else{ //默认都为跳转地址方式
                        res.setCodeUrl(rspData.get("qrCode"));
                    }
                    channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.WAITING);
                }else{
                    res.setOrderState(PayZfdd00.STATE_FAIL);  //支付失败
                    channelRetMsg.setChannelErrCode(respCode);
                    channelRetMsg.setChannelErrMsg(respMsg);
                }
            }else{
                log.error("验证签名失败");
            }
        }else{
            //未返回正确的http状态
            log.error("未获取到返回报文或返回http状态码非200");
        }
        return res;
    }

}
