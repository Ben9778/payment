package com.ylz.yx.pay.payment.channel.pospay;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ylz.core.logging.Logger;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.entity.PayTkdd00;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.core.exception.BizException;
import com.ylz.yx.pay.core.model.params.union.UnionpayMchParams;
import com.ylz.yx.pay.payment.channel.AbstractRefundService;
import com.ylz.yx.pay.payment.channel.pospay.utils.StringUtil;
import com.ylz.yx.pay.payment.channel.unionpay.utils.UnionSignUtils;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import com.ylz.yx.pay.payment.rqrs.refund.RefundOrderRQ;
import org.apache.commons.lang3.StringUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/*
 * 退款接口： 银联POS机
 */
@Service
public class PospayRefundService extends AbstractRefundService {

    private static final Logger log = new Logger(PospayRefundService.class.getName());

    @Override
    public String getIfCode() {
        return CS.IF_CODE.POSPAY;
    }

    @Autowired
    private PospayPaymentService pospayPaymentService;

    @Override
    public String preCheck(RefundOrderRQ bizRQ, PayTkdd00 payTkdd00, PayZfdd00 payZfdd00) {
        return null;
    }

    @Override
    public ChannelRetMsg refund(RefundOrderRQ bizRQ, PayTkdd00 payTkdd00, PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext) throws Exception {
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        String logPrefix = "【POS机退款】";
        String posIp = "";
        if (StringUtils.isEmpty(bizRQ.getChannelExtra())) {
            throw new BizException("POS机IP地址不可为空");
        } else {
            JSONObject jsonObject = JSON.parseObject(bizRQ.getChannelExtra());
            if (jsonObject.isEmpty()) {
                throw new BizException("POS机IP地址不可为空");
            } else {
                posIp = jsonObject.getString("posIp");
            }
        }

        //应用类型标志        2位
        String yylxbz = "00";
        //POS机号            8位
        String posjh0 = "";
        //POS员工号          8位
        String posygh = "";
        //交易类型标志        2位
        String jylxbz = "02";
        //金额              12位
        Integer czje00 = payTkdd00.getTkje00().intValue();
        //原交易日期          8位
        String yjyrq0 = DateUtil.format(payZfdd00.getDdzfsj(), "yyyyMMdd");
        //原交易参考号        12位
        String yjyckh = payZfdd00.getZfddh0().split("&")[0];
        //原凭证号            6位
        String ypzh00 = "";
        //LRC校验            3位
        String lrcjy0 = "";
        //POS通串码          50位
        String postcm = "";
        //银商订单号          50位
        String ysddh0 = "";
        //ERP订单号           50位
        String erpddh = payTkdd00.getXtddh0();
        //无硬件C扫B二维码ID   32位
        String ewmid0 = "";
        //无硬件APPIDKEY      300位
        String key000 = "";

        StringBuffer sb = new StringBuffer();
        sb.append(yylxbz).append(String.format("%-8s", posjh0)).append(String.format("%-8s", posygh))
                .append(jylxbz).append(String.format("%012d", czje00)).append(String.format("%-8s", yjyrq0))
                .append(String.format("%-12s", yjyckh)).append(String.format("%-6s", ypzh00))
                .append(String.format("%-3s", lrcjy0)).append(String.format("%-50s", postcm))
                .append(String.format("%-50s", ysddh0)).append(String.format("%-50s", erpddh))
                .append(String.format("%-32s", ewmid0)).append(String.format("%-300s", key000));
        log.info(logPrefix + " reqParams=" + sb);
        final boolean[] flag = {true};
        try {
            WebSocketClient webSocketClient = new WebSocketClient(new URI("ws://" + posIp + ":1818"), new Draft_6455()) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    log.info(logPrefix + " 连接成功");
                    this.send(sb.toString());
                }

                @Override
                public void onMessage(String message) {
                    log.info(logPrefix + " 收到消息=" + message);
                    String code = StringUtil.getFromCompressedUnicode(message, 0, 2);
                    String pzh000 = StringUtil.getFromCompressedUnicode(message, 26, 6);
                    String respMsg = StringUtil.getFromCompressedUnicode(message, 44, 40);
                    String orderId = StringUtil.getFromCompressedUnicode(message, 123, 12);
                    if ("00".equals(code)) {
                        channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                        channelRetMsg.setChannelOrderId(orderId + "&" + pzh000);
                    } else {
                        channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                        channelRetMsg.setChannelErrCode(code);
                        channelRetMsg.setChannelErrMsg(respMsg);
                    }
                    flag[0] = false;
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    log.info(logPrefix + " 退出连接");
                }

                @Override
                public void onError(Exception ex) {
                    log.info(logPrefix + " 连接错误=" + ex.getMessage());
                }
            };
            webSocketClient.connect();
        } catch (Exception e) {
            channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.SYS_ERROR); // 系统异常
        }
        while(flag[0]){
            Thread.sleep(1000);
        }
        return channelRetMsg;
    }

    @Override
    public ChannelRetMsg query(PayTkdd00 payTkdd00, MchAppConfigContext mchAppConfigContext) throws Exception {
        String logPrefix = "【银联POS机退款查询】";
        log.info(logPrefix+" reqParams=默认成功");
        return ChannelRetMsg.unknown();
    }

}
