package com.ylz.yx.pay.payment.controller.payorder;

import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.yx.pay.core.constants.CS;
import com.ylz.yx.pay.core.exception.BizException;
import com.ylz.yx.pay.core.model.ApiRes;
import com.ylz.yx.pay.core.entity.PayZfdd00;
import com.ylz.yx.pay.payment.rqrs.payorder.UnifiedOrderRQ;
import com.ylz.yx.pay.payment.rqrs.payorder.UnifiedOrderRS;
import com.ylz.yx.pay.payment.rqrs.payorder.payway.AutoBarOrderRQ;
import com.ylz.yx.pay.payment.service.ConfigContextQueryService;
import com.ylz.yx.pay.utils.PayKit;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/*
* 统一支付下单 controller
*/
@RestController
public class UnifiedOrderController extends AbstractPayOrderController {

    @Autowired private ConfigContextQueryService configContextQueryService;
    @Autowired
    private JdbcGateway jdbcGateway;//自定义jdbc

    /**
     * 统一下单接口
     * **/
    @PostMapping("/api/pay/unifiedOrder")
    public ApiRes unifiedOrder(){

        //获取参数 & 验签
        UnifiedOrderRQ rq = getRQByWithMchSign(UnifiedOrderRQ.class);
        //得到对应支付方式(如ALI_JSAPI)所需要的参数
        UnifiedOrderRQ bizRQ = buildBizRQ(rq);

        //实现子类的res
        ApiRes apiRes = unifiedOrder(bizRQ.getWayCode(), bizRQ);
        if(apiRes.getData() == null){
            return apiRes;
        }

        UnifiedOrderRS bizRes = (UnifiedOrderRS)apiRes.getData();

        //聚合接口，返回的参数
        UnifiedOrderRS res = new UnifiedOrderRS();
        BeanUtils.copyProperties(bizRes, res);

        //只有 订单生成（QR_CASHIER） || 支付中 || 支付成功返回该数据
        if(bizRes.getOrderState() != null && (bizRes.getOrderState().equals(PayZfdd00.STATE_INIT) || bizRes.getOrderState().equals(PayZfdd00.STATE_ING) || bizRes.getOrderState().equals(PayZfdd00.STATE_SUCCESS)) ){
            res.setPayDataType(bizRes.buildPayDataType());
            res.setPayData(bizRes.buildPayData());
        }

        return ApiRes.okWithSign(res, configContextQueryService.queryMchApp(rq.getAppId()).getQdmy00());
    }

    /**
     * 订单统一请求参数
     * @param rq
     * @return
     */
    private UnifiedOrderRQ buildBizRQ(UnifiedOrderRQ rq){

        //支付方式  比如： ali_bar
        String wayCode = rq.getWayCode();

        //jsapi 收银台聚合支付场景 (不校验是否存在payWayCode)
        //用户扫医院的聚合码不需要校验支付方式
        if(CS.PAY_WAY_CODE.QR_CASHIER.equals(wayCode)){
            return rq.buildBizRQ();
        }

        //医院扫用户的聚合码需要判断二维码的支付渠道 微信/二维码/银联
        if(CS.PAY_WAY_CODE.AUTO_BAR.equals(wayCode)){

            AutoBarOrderRQ bizRQ = (AutoBarOrderRQ)rq.buildBizRQ();
            //验证条码是否合规,并得到返回的支付方式,微信/支付宝/银联
            wayCode = PayKit.getPayWayCodeByBarCode(bizRQ.getAuthCode());
            rq.setWayCode(wayCode.trim());
        }
        String id0000 = rq.getAppId();//服务渠道ID
        String zffs00 = jdbcGateway.selectOne("pay.fwzfgx.selectByFwqdid", id0000);
        if(zffs00 != null){
            if(!zffs00.contains(wayCode)){
                throw new BizException("不支持的支付方式");
            }
        }

        //转换为 bizRQ
        return rq.buildBizRQ();
    }


}
