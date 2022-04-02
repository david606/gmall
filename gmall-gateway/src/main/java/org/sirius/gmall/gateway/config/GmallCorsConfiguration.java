package org.sirius.gmall.gateway.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * @author david
 */
@Configuration
public class GmallCorsConfiguration {

    /**
     * WebFilter处理 CORS 预检请求并拦截 CORS 简单和实际请求，
     * 这要归功于CorsProcessor实现（默认为DefaultCorsProcessor ），
     * 以便使用提供的CorsConfigurationSource添加相关的 CORS 响应标头
     * （如Access-Control-Allow-Origin ）
     * （例如UrlBasedCorsConfigurationSource实例。
     * 这是 Spring WebFlux Java config CORS 配置的替代方案，主要用于使用函数式 API 的应用程序。
     *
     * @return
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        /*
          根据映射在路径模式上的CorsConfiguration集合，提供每个响应式请求CorsConfiguration实例。
          支持精确路径映射 URI（例如"/admin" ）以及 Ant 样式的路径模式（例如"/admin/**" ）
         */
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        /*
         用于 CORS 配置的容器以及检查给定请求的实际来源、HTTP 方法和标头的方法。
         默认情况下，新创建的CorsConfiguration不允许任何跨域请求，并且必须明确配置以指示应允许的内容。
         使用applyPermitDefaultValues()翻转初始化模型以从允许所有跨域请求的 GET、HEAD 和 POST 请求的开放默认值开始。
         */
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);

        // 为指定的路径模式注册一个CorsConfiguration
        source.registerCorsConfiguration("/**", configuration);

        return new CorsWebFilter(source);
    }
}
