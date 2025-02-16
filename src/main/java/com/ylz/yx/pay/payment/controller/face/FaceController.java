package com.ylz.yx.pay.payment.controller.face;

import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.yx.pay.config.ApplicationProperty;
import com.ylz.yx.pay.core.constants.ApiCodeEnum;
import com.ylz.yx.pay.core.exception.BizException;
import com.ylz.yx.pay.core.model.ApiRes;
import com.ylz.yx.pay.payment.channel.IFaceService;
import com.ylz.yx.pay.payment.controller.payorder.AbstractPayOrderController;
import com.ylz.yx.pay.payment.model.MchAppConfigContext;
import com.ylz.yx.pay.payment.rqrs.FaceInitRQ;
import com.ylz.yx.pay.payment.rqrs.FaceInitRS;
import com.ylz.yx.pay.payment.service.ConfigContextQueryService;
import com.ylz.yx.pay.utils.SpringBeansUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api/face")
public class FaceController extends AbstractPayOrderController {

    @Autowired
    private ConfigContextQueryService configContextQueryService;
    @Autowired private JdbcGateway jdbcGateway;
    @Resource
    private ApplicationProperty applicationProperty;

    /**
     * 刷脸初始化
     * **/
    @PostMapping("/init")
    public ApiRes init(){

        FaceInitRQ rq = getRQByWithMchSign(FaceInitRQ.class);

        String ifCode = rq.getIfCode();
        // 获取接口
        IFaceService faceService = SpringBeansUtil.getBean(ifCode + "FaceService", IFaceService.class);

        if(faceService == null){
            throw new BizException("不支持的客户端");
        }

        //获取商户配置信息
        MchAppConfigContext mchAppConfigContext = configContextQueryService.queryMchInfoAndAppInfo(rq.getAppId());

        FaceInitRS res = (FaceInitRS) faceService.init(getReqParamJSON(), mchAppConfigContext);

        if (res.getCode() == ApiCodeEnum.SUCCESS.getCode()) {
            return ApiRes.okWithSign(res, configContextQueryService.queryMchApp(rq.getAppId()).getQdmy00());
        } else {
            return ApiRes.customFail(res.getMsg());
        }
    }
}
