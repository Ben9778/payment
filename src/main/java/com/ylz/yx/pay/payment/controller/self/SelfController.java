package com.ylz.yx.pay.payment.controller.self;

import cn.hutool.core.date.DateUtil;
import com.ylz.core.logging.Logger;
import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.yx.pay.core.constants.ApiCodeEnum;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.core.model.ApiRes;
import com.ylz.yx.pay.payment.channel.pospay.utils.StringUtil;
import com.ylz.yx.pay.payment.controller.ApiController;
import com.ylz.yx.pay.payment.rqrs.SelfSynchRQ;
import com.ylz.yx.pay.payment.rqrs.SelfSynchRS;
import com.ylz.yx.pay.payment.service.ConfigContextQueryService;
import com.ylz.yx.pay.utils.SeqKit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Date;
//自助机功能接口
@RestController
@RequestMapping("/api/self")
public class SelfController extends ApiController {
    private static final Logger log = new Logger(SelfController.class.getName());

    @Autowired
    private ConfigContextQueryService configContextQueryService;
    @Autowired
    private JdbcGateway jdbcGateway;

    /**
     * 自助机订单同步
     * **/
    @PostMapping("/synch")
    public ApiRes synch() {
        String logPrefix = "【自助机订单同步】";
        SelfSynchRQ rq = getRQByWithMchSign(SelfSynchRQ.class);
        SelfSynchRS res = new SelfSynchRS();
        PayZfdd00 payZfdd00 = new PayZfdd00();
        payZfdd00.setFwqdid(rq.getAppId()); //商户应用appId
        payZfdd00.setXtddh0(rq.getPayOrderId()); //生成订单ID
        payZfdd00.setFwddh0(rq.getMchOrderNo()); //商户订单号
        payZfdd00.setZfddh0(rq.getSystemRefNum()+"&"+rq.getVoucherNum());//支付订单号：系统参考号+凭证号
        payZfdd00.setHzxm00(rq.getUserName()); //患者姓名
        payZfdd00.setHzsjh0(rq.getPhone()); //患者手机号
        payZfdd00.setKhzyh0(rq.getCardNo()); //就诊卡号/住院号
        payZfdd00.setHzsfzh(rq.getIdCard()); //患者身份证号
        payZfdd00.setDdlx00(rq.getOrderType()); //订单类型
        if("01".equals(rq.getOrderType())){
            payZfdd00.setDdmc00("门诊-"+rq.getSubject()); //商品标题
        } else if("02".equals(rq.getOrderType())){
            payZfdd00.setDdmc00("住院-"+rq.getSubject()); //商品标题
        } else {
            payZfdd00.setDdmc00(rq.getSubject()); //商品标题
        }
        payZfdd00.setZfqd00("pospay"); //支付渠道
        payZfdd00.setZffs00(rq.getWayCode()); //支付方式
        payZfdd00.setZfje00(rq.getAmount()); //订单金额
        payZfdd00.setDdzt00(PayZfdd00.STATE_SUCCESS); //订单状态, 默认订单生成状态

        Date nowDate = new Date();
        payZfdd00.setDdcjsj(nowDate); //订单创建时间
        payZfdd00.setCzyid0(rq.getOperatorId()); //操作员ID
        payZfdd00.setCzyxm0(rq.getOperatorName()); //操作员姓名
        payZfdd00.setYyid00(rq.getHospitalId()); //医院ID
        jdbcGateway.insert("pay.zfdd00.insertSelective", payZfdd00);
        log.info(logPrefix + " 订单号："+rq.getMchOrderNo()+" 同步成功！！！");
        return ApiRes.okWithSign(res, configContextQueryService.queryMchApp(rq.getAppId()).getQdmy00());
    }
}
