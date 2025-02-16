package com.ylz.yx.pay.payment.util;

import com.ylz.yx.pay.config.ApplicationProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;

/*
* 支付平台 获取系统文件工具类
*/
@Component
public class ChannelCertConfigKitBean {

    @Resource
    private ApplicationProperty applicationProperty;

    public String getCertFilePath(String certFilePath){
        return getCertFile(certFilePath).getAbsolutePath();
    }

    public File getCertFile(String certFilePath){
        File certFile = new File(applicationProperty.getFileStoragePath() + certFilePath);

        if(certFile.exists()){ // 本地存在直接返回
            return certFile;
        }
        return null;
    }

}
