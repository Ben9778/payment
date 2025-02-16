package com.ylz.yx.pay.deploy;

import cn.hutool.core.date.DatePattern;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.ylz.svc.data.dao.JdbcGateway;
import com.ylz.yx.pay.config.ApplicationProperty;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/*
 *  项目初始化操作
*/
@Component
@Order(value = 1)
public class InitRunner implements ApplicationRunner {

    @Autowired
    private JdbcGateway jdbcGateway;
    @Resource
    private ApplicationProperty applicationProperty;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        // 配置是否使用缓存模式
        String isCache = jdbcGateway.selectOne("system.zd0000.getSysParam", "isCache");
        ApplicationProperty.IS_USE_CACHE = isCache.equals("true");
        //是否校验地址 true/false
        String isCheckUrl = jdbcGateway.selectOne("system.zd0000.getSysParam", "isCheckUrl");
        applicationProperty.setIsCheckUrl(isCheckUrl.equals("true"));
        applicationProperty.setIntranetIp(jdbcGateway.selectOne("system.zd0000.getSysParam", "intranetIp"));//允许访问的地址网段
        applicationProperty.setPaySiteBackUrl(jdbcGateway.selectOne("system.zd0000.getSysParam", "paySiteBackUrl"));//支付网关后端地址
        applicationProperty.setPaySiteFrontUrl(jdbcGateway.selectOne("system.zd0000.getSysParam", "paySiteFrontUrl"));//支付网关前端地址
        applicationProperty.setFileStoragePath(jdbcGateway.selectOne("system.zd0000.getSysParam", "fileStoragePath"));//文件存放地址
        applicationProperty.setMchName(jdbcGateway.selectOne("system.zd0000.getSysParam", "mchName"));//商户名称
        applicationProperty.setYlzCommonUrl(jdbcGateway.selectOne("system.zd0000.getSysParam", "ylzCommonUrl"));//通用接口地址
        applicationProperty.setHisManufacturer(jdbcGateway.selectOne("system.zd0000.getSysParam", "hisManufacturer"));//HIS系统厂家
        applicationProperty.setHisOperator(jdbcGateway.selectOne("system.zd0000.getSysParam", "hisOperator"));//HIS系统操作员
        //是否直接获取HIS对账数据 true/false
        String isHisViewData = jdbcGateway.selectOne("system.zd0000.getSysParam", "isHisViewData");
        applicationProperty.setIsHisViewData(isHisViewData.equals("true"));
        applicationProperty.setHisBillFileUrl(jdbcGateway.selectOne("system.zd0000.getSysParam", "hisBillFileUrl"));//请求HIS对账数据地址
        //判断HIS是否存在订单 true/false
        String isHisOrder = jdbcGateway.selectOne("system.zd0000.getSysParam", "isHisOrder");
        applicationProperty.setIsHisOrder(isHisOrder.equals("true"));
        //是否需要走退款审批流程 true/false
        //String isRefundApproval = jdbcGateway.selectOne("system.zd0000.getSysParam", "isRefundApproval");
        //applicationProperty.setIsRefundApproval(isRefundApproval.equals("true"));

        //初始化处理fastjson格式
        SerializeConfig serializeConfig = SerializeConfig.getGlobalInstance();
        serializeConfig.put(Date.class, new SimpleDateFormatSerializer(DatePattern.NORM_DATETIME_PATTERN));

        //解决json 序列化时候的  $ref：问题
        JSON.DEFAULT_GENERATE_FEATURE |= SerializerFeature.DisableCircularReferenceDetect.getMask();

    }
}
