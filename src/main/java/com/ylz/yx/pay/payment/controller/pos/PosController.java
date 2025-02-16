package com.ylz.yx.pay.payment.controller.pos;

import com.ylz.core.logging.Logger;
import com.ylz.yx.pay.core.constants.ApiCodeEnum;
import com.ylz.yx.pay.core.model.ApiRes;
import com.ylz.yx.pay.payment.channel.pospay.utils.StringUtil;
import com.ylz.yx.pay.payment.controller.ApiController;
import com.ylz.yx.pay.payment.rqrs.PosSettleRQ;
import com.ylz.yx.pay.payment.rqrs.PosSettleRS;
import com.ylz.yx.pay.payment.service.ConfigContextQueryService;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/pos")
public class PosController extends ApiController {

    private static final Logger log = new Logger(PosController.class.getName());

    @Autowired
    private ConfigContextQueryService configContextQueryService;

    /**
     * POS结算
     * **/
    @PostMapping("/settle")
    public ApiRes settle() throws Exception {
        String logPrefix = "【银联POS机银行卡结算】";

        PosSettleRQ rq = getRQByWithMchSign(PosSettleRQ.class);

        PosSettleRS res = new PosSettleRS();
        //应用类型标志        2位
        String yylxbz = "00";
        //POS机号            8位
        String posjh0 = "";
        //POS员工号          8位
        String posygh = "";
        //交易类型标志        2位
        String jylxbz = "06";
        //金额              12位
        Integer czje00 = 0;
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
        final boolean[] flag = {true};
        try {
            WebSocketClient webSocketClient = new WebSocketClient(new URI("ws://"+rq.getPosIp()+":1818"), new Draft_6455()) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    log.info(logPrefix + " 连接成功");
                    this.send(sb.toString());
                }

                @Override
                public void onMessage(String message) {
                    log.info(logPrefix + " 收到消息=" + message);
                    String code = StringUtil.getFromCompressedUnicode(message, 0, 2);
                    String respMsg = StringUtil.getFromCompressedUnicode(message, 44,40);
                    if ("00".equals(code)) {
                        res.setCode(ApiCodeEnum.SUCCESS.getCode());
                    } else {
                        res.setCode(ApiCodeEnum.CUSTOM_FAIL.getCode());
                        res.setMsg(respMsg);
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
            res.setCode(ApiCodeEnum.CUSTOM_FAIL.getCode());
        }
        while(flag[0]){
            Thread.sleep(1000);
        }

        return ApiRes.okWithSign(res, configContextQueryService.queryMchApp(rq.getAppId()).getQdmy00());
    }
}
