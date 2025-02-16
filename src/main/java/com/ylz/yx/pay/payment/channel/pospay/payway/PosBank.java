package com.ylz.yx.pay.payment.channel.pospay.payway;

import com.ylz.core.logging.Logger;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.payment.channel.pospay.PospayPaymentService;
import com.ylz.yx.pay.payment.channel.pospay.utils.StringUtil;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.AbstractRS;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import com.ylz.yx.pay.payment.rqrs.payorder.UnifiedOrderRQ;
import com.ylz.yx.pay.payment.rqrs.payorder.payway.PosBankOrderRQ;
import com.ylz.yx.pay.payment.rqrs.payorder.payway.PosBankOrderRS;
import com.ylz.yx.pay.payment.util.ApiResBuilder;
import org.apache.commons.lang3.StringUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.stereotype.Service;

import java.net.URI;

/*
 * 银联 POS机支付
 */
@Service("unionPaymentByPosBankService") //Service Name需保持全局唯一性
public class PosBank extends PospayPaymentService {

    private static final Logger log = new Logger(PosBank.class.getName());

    @Override
    public String preCheck(UnifiedOrderRQ rq, PayZfdd00 payZfdd00) {

        PosBankOrderRQ bizRQ = (PosBankOrderRQ) rq;
        if (StringUtils.isEmpty(bizRQ.getPosIp())) {
            return "POS机IP地址不可为空";
        }

        return null;
    }

    @Override
    public AbstractRS pay(UnifiedOrderRQ rq, PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext) throws Exception {
        String logPrefix = "【银联POS机银行卡支付】";

        PosBankOrderRQ bizRQ = (PosBankOrderRQ) rq;
        PosBankOrderRS res = ApiResBuilder.buildSuccess(PosBankOrderRS.class);
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        res.setChannelRetMsg(channelRetMsg);

        //应用类型标志        2位
        String yylxbz = "00";
        //POS机号            8位
        String posjh0 = "";
        //POS员工号          8位
        String posygh = "";
        //交易类型标志        2位
        String jylxbz = "00";
        //金额              12位
        Integer czje00 = payZfdd00.getZfje00().intValue();
        //原交易日期          8位
        String yjyrq0 = "";
        //原交易参考号        12位
        String yjyckh = "";
        //原凭证号            6位
        String ypzh00 = "";
        //LRC校验            3位
        String lrcjy0 = "";
        //POS通串码          50位
        String postcm = "";
        //银商订单号          50位
        String ysddh0 = "";
        //ERP订单号           50位
        String erpddh = payZfdd00.getXtddh0();
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
            WebSocketClient webSocketClient = new WebSocketClient(new URI("ws://"+bizRQ.getPosIp()+":1818"), new Draft_6455()) {
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
        return res;
    }

}
