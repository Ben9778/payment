package com.ylz.yx.pay.payment.controller.refund;

import com.ylz.core.logging.Logger;
import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.yx.pay.core.ctrl.AbstractCtrl;
import com.ylz.yx.pay.core.exception.BizException;
import com.ylz.yx.pay.core.exception.ResponseException;
import com.ylz.yx.pay.payment.channel.IChannelRefundNoticeService;
import com.ylz.yx.pay.core.entity.PayTkdd00;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.msg.ChannelRetMsg;
import com.ylz.yx.pay.payment.service.ConfigContextQueryService;
import com.ylz.yx.pay.payment.service.RefundOrderProcessService;
import com.ylz.yx.pay.utils.SpringBeansUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

/*
* 渠道侧的退款通知入口Controller 【异步回调(doNotify) 】
*/
@Controller
public class ChannelRefundNoticeController extends AbstractCtrl {

    private static final Logger log = new Logger(ChannelRefundNoticeController.class.getName());

    @Autowired private JdbcGateway jdbcGateway;
    @Autowired private ConfigContextQueryService configContextQueryService;
    @Autowired private RefundOrderProcessService refundOrderProcessService;

    /** 异步回调入口 **/
    @ResponseBody
    @RequestMapping(value= {"/api/refund/notify/{ifCode}", "/api/refund/notify/{ifCode}/{refundOrderId}"})
    public ResponseEntity doNotify(HttpServletRequest request, @PathVariable("ifCode") String ifCode, @PathVariable(value = "refundOrderId", required = false) String urlOrderId){

        String refundOrderId = null;
        String logPrefix = "进入[" +ifCode+ "]退款回调：urlOrderId：["+ StringUtils.defaultIfEmpty(urlOrderId, "") + "] ";
        log.info("===== "+logPrefix+" =====");

        try {

            // 参数有误
            if(StringUtils.isEmpty(ifCode)){
                return ResponseEntity.badRequest().body("ifCode is empty");
            }

            //查询退款接口是否存在
            IChannelRefundNoticeService refundNotifyService = SpringBeansUtil.getBean(ifCode + "ChannelRefundNoticeService", IChannelRefundNoticeService.class);

            // 支付通道接口实现不存在
            if(refundNotifyService == null){
                logger.info("{}, interface not exists ", logPrefix);
                return ResponseEntity.badRequest().body("[" + ifCode + "] interface not exists");
            }

            // 解析订单号 和 请求参数
            MutablePair<String, Object> mutablePair = refundNotifyService.parseParams(request, urlOrderId, IChannelRefundNoticeService.NoticeTypeEnum.DO_NOTIFY);
            if(mutablePair == null){ // 解析数据失败， 响应已处理
                logger.info("{}, mutablePair is null ", logPrefix);
                throw new BizException("解析数据异常！"); //需要实现类自行抛出ResponseException, 不应该在这抛此异常。
            }

            // 解析到订单号
            refundOrderId = mutablePair.left;
            log.info(logPrefix + ", 解析数据为：refundOrderId:"+refundOrderId+", params:"+ mutablePair.getRight());

            if(StringUtils.isNotEmpty(urlOrderId) && !urlOrderId.equals(refundOrderId)){
                log.error(logPrefix + ", 订单号不匹配. urlOrderId="+urlOrderId+", refundOrderId= "+ refundOrderId);
                throw new BizException("退款单号不匹配！");
            }

            //获取订单号 和 订单数据
            PayTkdd00 payTkdd00 = jdbcGateway.selectOne("pay.tkdd00.selectByXtddh0", refundOrderId);

            // 订单不存在
            if(payTkdd00 == null){
                log.error(logPrefix+", 退款订单不存在. refundOrderId= "+ refundOrderId);
                return refundNotifyService.doNotifyOrderNotExists(request);
            }

            //查询出商户应用的配置信息
            MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(payTkdd00.getFwqdid());

            //调起接口的回调判断
            ChannelRetMsg notifyResult = refundNotifyService.doNotice(request, mutablePair.getRight(), payTkdd00, mchAppConfigContext, IChannelRefundNoticeService.NoticeTypeEnum.DO_NOTIFY);

            // 返回null 表明出现异常， 无需处理通知下游等操作。
            if(notifyResult == null || notifyResult.getChannelState() == null || notifyResult.getResponseEntity() == null){
                log.error(logPrefix+ ", 处理回调事件异常  notifyResult data error, notifyResult = "+ notifyResult);
                throw new BizException("处理回调事件异常！"); //需要实现类自行抛出ResponseException, 不应该在这抛此异常。
            }
            // 处理退款订单
            boolean updateOrderSuccess = refundOrderProcessService.handleRefundOrder4Channel(notifyResult, payTkdd00);

            // 更新退款订单 异常
            if(!updateOrderSuccess){
                log.error(logPrefix+", updateOrderSuccess = "+updateOrderSuccess);
                return refundNotifyService.doNotifyOrderStateUpdateFail(request);
            }

            log.info("===== "+logPrefix+", 订单通知完成。 refundOrderId="+refundOrderId+", parseState = "+notifyResult.getChannelState()+" =====");

            return notifyResult.getResponseEntity();

        } catch (BizException e) {
            log.error(logPrefix+", refundOrderId="+refundOrderId+", BizException"+ e);
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (ResponseException e) {
            log.error(logPrefix+", refundOrderId="+refundOrderId+", ResponseException"+ e);
            return e.getResponseEntity();

        } catch (Exception e) {
            log.error(logPrefix+", refundOrderId="+refundOrderId+", 系统异常"+ e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
