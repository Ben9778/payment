package com.ylz.yx.pay.payment.channel.pospay;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ylz.core.logging.Logger;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.payment.channel.IPayOrderCloseService;
import com.ylz.yx.pay.payment.channel.pospay.utils.StringUtil;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.stereotype.Service;

import java.net.URI;

/**
 * 银联POS机 关闭订单接口实现类
 */
@Service
public class PospayPayOrderCloseService implements IPayOrderCloseService {

    private static final Logger log = new Logger(PospayPayOrderCloseService.class.getName());

    @Override
    public String getIfCode() {
        return CS.IF_CODE.POSPAY;
    }

    @Override
    public ChannelRetMsg close(PayZfdd00 payZfdd00, MchAppConfigContext mchAppConfigContext) throws Exception {
        ChannelRetMsg channelRetMsg = new ChannelRetMsg();
        String logPrefix = "【银联POS机撤销】";

        JSONObject jsonObject = JSON.parseObject(payZfdd00.getQdcs00());
        String posIp = jsonObject.getString("posIp");

        //应用类型标志        2位
        String yylxbz = "00";
        //POS机号            8位
        String posjh0 = "";
        //POS员工号          8位
        String posygh = "";
        //交易类型标志        2位
        String jylxbz = "01";
        //金额              12位
        Integer czje00 = null;
        //原交易日期          8位
        String yjyrq0 = "";
        //原交易参考号        12位
        String yjyckh = "";
        //原凭证号            6位
        String ypzh00 = payZfdd00.getZfddh0().split("&")[1];;
        //LRC校验            3位
        String lrcjy0 = "";
        //POS通串码          50位
        String postcm = "";
        //银商订单号          50位
        String ysddh0 = "";
        //ERP订单号           50位
        String erpddh = "";
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
        try {
            WebSocketClient webSocketClient = new WebSocketClient(new URI("ws://"+posIp+":1818"),new Draft_6455()) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    log.info(logPrefix + " 连接成功");
                    this.send(sb.toString());
                }

                @Override
                public void onMessage(String message) {
                    log.info(logPrefix + " 收到消息=" + message);
                    String code = StringUtil.getFromCompressedUnicode(message, 0,2);
                    String respMsg = StringUtil.getFromCompressedUnicode(message, 44,40);
                    String orderId = StringUtil.getFromCompressedUnicode(message, 123,12);
                    if("00".equals(code)){
                        channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS);
                    } else {
                        channelRetMsg.setChannelState(ChannelRetMsg.ChannelState.CONFIRM_FAIL);
                        channelRetMsg.setChannelErrMsg(respMsg);
                    }
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
        return channelRetMsg;
    }

}
