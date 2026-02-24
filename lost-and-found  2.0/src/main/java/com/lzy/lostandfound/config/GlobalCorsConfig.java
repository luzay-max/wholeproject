package com.lzy.lostandfound.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class GlobalCorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        // 1. 创建CORS配置对象
        CorsConfiguration config = new CorsConfiguration();
        // 允许的源（必须是前端实际的源，不带路径）
        // 开发环境
        config.addAllowedOrigin("http://localhost:5173");
        // 生产环境 - 宝塔 Nginx
        config.addAllowedOrigin("http://47.110.144.114");
        // 备用：也允许直接用 IP:端口 访问
        config.addAllowedOrigin("http://47.110.144.114:80");
        // 允许携带cookie（前后端需一致）
        config.setAllowCredentials(true);
        // 允许的请求方法（包含所有方法，包括OPTIONS）
        config.addAllowedMethod("*");
        // 允许的请求头（特别是前端可能带的Authorization等自定义头）
        config.addAllowedHeader("*");
        // 暴露的响应头（如果前端需要读取自定义响应头，需添加）
        config.addExposedHeader("*");
        // 预检请求缓存时间（减少重复预检）
        config.setMaxAge(3600L);

        // 2. 配置生效路径（所有接口）
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        // 3. 返回过滤器（优先级高于拦截器）
        return new CorsFilter(source);
    }
}