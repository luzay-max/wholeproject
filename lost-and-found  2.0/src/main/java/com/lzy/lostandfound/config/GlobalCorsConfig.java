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
        // 允许浏览器从本地开发、Docker 端口映射和线上同机反向代理发起跨域请求。
        // 使用 pattern 而不是固定 origin，避免前端端口切到 18081/5174 等场景时直接被 Spring 拦成 403。
        config.addAllowedOriginPattern("http://localhost:*");
        config.addAllowedOriginPattern("http://127.0.0.1:*");
        config.addAllowedOriginPattern("http://47.110.144.114:*");
        config.addAllowedOriginPattern("https://47.110.144.114:*");
        config.addAllowedOriginPattern("http://47.110.144.114");
        config.addAllowedOriginPattern("https://47.110.144.114");
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
