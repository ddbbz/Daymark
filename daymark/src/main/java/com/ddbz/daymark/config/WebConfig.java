package com.ddbz.daymark.config;


import com.ddbz.daymark.interceptor.JwtTokenUserInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


/**
 * 配置Token拦截对象
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtTokenUserInterceptor jwtTokenUserInterceptor;


    /**
     * 注册拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 静态图片路径放行：<img> 标签请求不带 token，必须绕过令牌校验
        registry.addInterceptor(jwtTokenUserInterceptor).addPathPatterns("/**")
                .excludePathPatterns("/images/**",
                        "/file/**",
                        "/login/**",
                        "/register/**",
                        "/register",
                        "/reviewerRegister",
                        "/reviewerLogin",
                        "/error",
                        "/image/**"
                );
    }

    /**
     * 静态资源映射：/images/** 和 /file/** 都指向本地上传目录
     * 前端 <img src> 可以直接访问 http://localhost:8080/images/xxx.jpg
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:D:/zxc/project/Daymark/upload/");
        registry.addResourceHandler("/file/**")
                .addResourceLocations("file:D:/zxc/project/Daymark/upload/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**").allowedOriginPatterns("*")
                .allowedMethods("*").allowedHeaders("*");
    }


}
