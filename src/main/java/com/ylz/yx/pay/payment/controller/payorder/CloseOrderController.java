package com.ylz.yx.pay.payment.controller.payorder;

import com.ylz.core.logging.Logger;
import com.ylz.yx.pay.core.exception.BizException;
import com.ylz.yx.pay.core.model.ApiRes;
import com.ylz.yx.pay.payment.channel.IPayOrderCloseService;
import com.ylz.yx.pay.payment.controller.ApiController;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import com.ylz.yx.pay.payment.rqrs.payorder.ClosePayOrderRQ;
import com.ylz.yx.pay.payment.rqrs.payorder.ClosePayOrderRS;
import com.ylz.yx.pay.payment.service.ConfigContextQueryService;
import com.ylz.yx.pay.payment.service.impl.PayOrderService;
import com.ylz.yx.pay.utils.SpringBeansUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * 关闭订单 controller
 */
@RestController
public class CloseOrderController extends ApiController {

    private static final Logger log = new Logger(CloseOrderController.class.getName());

    @Autowired
    private PayOrderService payOrderService;
    @Autowired
    private ConfigContextQueryService configContextQueryService;

    /**
     * @author: xiaoyu
     * @date: 2022/1/25 9:19
     * @describe: 关闭订单
     */
    @RequestMapping("/api/pay/close")
    public ApiRes queryOrder() {

        //获取参数 & 验签
        ClosePayOrderRQ rq = getRQByWithMchSign(ClosePayOrderRQ.class);

        if (StringUtils.isEmpty(rq.getPayOrderId())) {
            throw new BizException("payOrderId不能为空");
        }

        PayZfdd00 payZfdd00 = payOrderService.queryMchOrder(rq.getPayOrderId(), rq.getMchOrderNo());
        if (payZfdd00 == null) {
            throw new BizException("订单不存在");
        }

        if (!payZfdd00.getDdzt00().equals(PayZfdd00.STATE_INIT) && !payZfdd00.getDdzt00().equals(PayZfdd00.STATE_ING)) {
            throw new BizException("当前订单不可关闭");
        }

        ClosePayOrderRS bizRes = new ClosePayOrderRS();


        if (payZfdd00.getDdzt00().equals(PayZfdd00.STATE_INIT)) {
            //根据系统订单号修改订单状态
            payOrderService.updateIng2Close(payZfdd00.getXtddh0());
            return ApiRes.okWithSign(bizRes, configContextQueryService.queryMchApp(rq.getAppId()).getQdmy00());
        }
        try {

            String payOrderId = payZfdd00.getXtddh0();

            //查询支付接口是否存在
            IPayOrderCloseService closeService = SpringBeansUtil.getBean(payZfdd00.getZfqd00() + "PayOrderCloseService", IPayOrderCloseService.class);

            // 支付通道接口实现不存在
            if (closeService == null) {
                log.error(payZfdd00.getZfqd00() + " interface not exists!");
                return null;
            }

            //查询出商户应用的配置信息
            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(payZfdd00.getFwqdid());

            ChannelRetMsg channelRetMsg = closeService.close(payZfdd00, mchAppConfigContext);
            if (channelRetMsg == null) {
                log.error("channelRetMsg is null");
                return null;
            }

            log.info("关闭订单[" + payOrderId + "]结果为：" + channelRetMsg);

            // 关闭订单 成功
            if (channelRetMsg.getChannelState().equals(ChannelRetMsg.ChannelState.CONFIRM_SUCCESS)) {
                payOrderService.updateIng2Close(payOrderId);
            } else {
                return ApiRes.customFail(channelRetMsg.getChannelErrMsg());
            }

            bizRes.setChannelRetMsg(channelRetMsg);
        } catch (Exception e) {  // 关闭订单异常
            log.error("error payOrderId = " + payZfdd00.getXtddh0(), e);
            return null;
        }

        return ApiRes.okWithSign(bizRes, configContextQueryService.queryMchApp(rq.getAppId()).getQdmy00());
    }

}
