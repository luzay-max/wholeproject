package com.lzy.lostandfound.config;

import com.lzy.lostandfound.interceptors.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 登录与注册放行
        registry.addInterceptor(loginInterceptor).excludePathPatterns(
                "/api/auth/login",
                "/api/auth/register",
                "/api/auth/captcha",
                "/api/auth/check-username",
                "/api/auth/refresh",

                // 静态资源
                "/static/**",
                "/css/**",
                "/js/**",
                "/images/**",

                // 公开的API接口
                "/api/lost/list",      // 失物列表
                "/api/find/list",      // 招领列表
                "/api/lost/detail/*",  // 失物详情
                "/api/find/detail/*",  // 招领详情
                "/api/statistics",     // 统计数据
                "/api/system/dict/data/type/*", // 字典数据公共接口

                // 首页和公开页面
                "/",
                "/index",
                "/about",
                "/contact",
                // swagger接口文档
                "/swagger-ui.html",
                "/webjars/**",
                "/v3/api-docs",
                "/swagger-resources/**",
                "/configuration/**",
                "/swagger-ui/index.html",
                "/v3/**",
                "/swagger-ui/**",
                "/api/v3/api-docs/**"
        );
    }

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // 仅对带有 @RestController 注解的类添加前缀
        configurer.addPathPrefix("/api", clazz -> clazz.isAnnotationPresent(RestController.class));
    }
}
