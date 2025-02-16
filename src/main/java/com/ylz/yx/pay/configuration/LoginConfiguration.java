package com.ylz.yx.pay.configuration;

import com.ylz.yx.pay.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class LoginConfiguration implements WebMvcConfigurer {

    @Value("${file.staticAccessPath}")
    private String staticAccessPath;
    @Value("${file.uploadFolder}")
    private String uploadFolder;
    
    @Bean
    public LoginInterceptor securityInterceptor() {
        return new LoginInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        if ("1".equals(System.getProperty("isPathPatterns"))) {
            // 注册拦截器
            InterceptorRegistration loginRegistry = registry.addInterceptor(securityInterceptor());
            // 拦截路径
            loginRegistry.addPathPatterns("/api/user/**");
            loginRegistry.addPathPatterns("/api/role/**");
            loginRegistry.addPathPatterns("/api/operLog/**");
            loginRegistry.addPathPatterns("/api/ftp/**");
            loginRegistry.addPathPatterns("/api/dict/**");
            loginRegistry.addPathPatterns("/api/channel/**");
            loginRegistry.addPathPatterns("/api/record/**");
            loginRegistry.addPathPatterns("/api/payorder/**");
            loginRegistry.addPathPatterns("/api/refundorder/**");
            loginRegistry.addPathPatterns("/api/index/**");
            loginRegistry.addPathPatterns("/api/files/**");
            //不拦截路径
            loginRegistry.excludePathPatterns("/api/record/expzfqddzhz");
            loginRegistry.excludePathPatterns("/api/record/expfwqddzhz");
            loginRegistry.excludePathPatterns("/api/record/expdzmxtj");
            loginRegistry.excludePathPatterns("/api/record/expcyztj");
            loginRegistry.excludePathPatterns("/api/payorder/expList");
            loginRegistry.excludePathPatterns("/api/refundorder/expList");
            loginRegistry.excludePathPatterns("/api/user/login");
            loginRegistry.excludePathPatterns("/uploadFiles/excel/**");
            loginRegistry.excludePathPatterns("/api/pay/**");
            loginRegistry.excludePathPatterns("/api/refund/**");
            loginRegistry.excludePathPatterns("/api/cashier/**");
            loginRegistry.excludePathPatterns("/api/channelUserId/**");
            loginRegistry.excludePathPatterns("/api/scan/**");
            loginRegistry.excludePathPatterns("/ylzUP/**");
        }
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.setUseSuffixPatternMatch(false);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler(staticAccessPath).addResourceLocations("file:" + uploadFolder);
        registry.addResourceHandler(staticAccessPath).addResourceLocations("file:///" + uploadFolder);
    }
}
