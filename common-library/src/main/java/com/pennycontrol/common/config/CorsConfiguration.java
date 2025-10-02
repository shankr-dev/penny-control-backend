package com.pennycontrol.common.config;

import com.pennycontrol.common.security.SecurityProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * CORS Configuration
 *
 * This configuration is automatically imported when @EnableCors is used.
 *
 * Configures CORS settings from SecurityProperties (application.yml)
 */
@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
public class CorsConfiguration {

    /**
     * CORS Configuration Source
     *
     * Configures allowed origins, methods, headers from application.yml
     *
     * @Primary ensures this bean is used over Spring MVC's auto-configured bean
     * @ConditionalOnMissingBean allows services to override with custom CORS config
     */
    @Bean
    @Primary
    @ConditionalOnMissingBean(name = "corsConfigurationSource")
    public CorsConfigurationSource corsConfigurationSource(SecurityProperties securityProperties) {
        org.springframework.web.cors.CorsConfiguration configuration = new org.springframework.web.cors.CorsConfiguration();

        // Allowed origins from application.yml
        configuration.setAllowedOrigins(securityProperties.getAllowedOrigins());

        // Allowed HTTP methods
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // Allowed headers
        configuration.setAllowedHeaders(List.of("*"));

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Expose Authorization header in response
        configuration.setExposedHeaders(List.of("Authorization"));

        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);

        // Apply configuration to all paths
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }
}
