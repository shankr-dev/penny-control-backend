package com.pennycontrol.common.config;

import com.pennycontrol.common.security.jwt.JwtAuthenticationEntryPoint;
import com.pennycontrol.common.security.jwt.SecurityLoggingFilter;
import com.pennycontrol.common.security.jwt.ExceptionHandlerFilter;
import com.pennycontrol.common.security.jwt.JwtAuthenticationFilter;
import com.pennycontrol.common.security.jwt.JwtProperties;
import com.pennycontrol.common.security.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JWT Security Configuration
 *
 * This configuration is automatically imported when @EnableJwt is used.
 *
 * Provides:
 * - JwtTokenProvider: For generating and validating JWT tokens
 * - JwtAuthenticationFilter: For extracting and validating JWT from requests
 * - ExceptionHandlerFilter: For handling exceptions in filter chain
 * - JwtAuthenticationEntryPoint: For handling 401 unauthorized responses
 * - SecurityLoggingFilter: For logging authenticated vs public endpoints
 * - JwtProperties: Configuration properties from application.yml
 */
@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfiguration {

    /**
     * JWT Token Provider bean
     * Handles token generation, validation, and parsing
     *
     * @ConditionalOnMissingBean allows services to override with custom implementation
     */
    @Bean
    @ConditionalOnMissingBean
    public JwtTokenProvider jwtTokenProvider(JwtProperties jwtProperties) {
        return new JwtTokenProvider(jwtProperties);
    }

    /**
     * JWT Authentication Filter bean
     * Intercepts requests and validates JWT tokens
     *
     * @ConditionalOnMissingBean allows services to override with custom filter
     */
    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            JwtProperties jwtProperties) {
        return new JwtAuthenticationFilter(jwtTokenProvider, jwtProperties);
    }

    /**
     * Exception Handler Filter bean
     * Catches exceptions from other filters and converts to JSON responses
     *
     * @ConditionalOnMissingBean allows services to override with custom handler
     */
    @Bean
    @ConditionalOnMissingBean
    public ExceptionHandlerFilter exceptionHandlerFilter(ObjectMapper objectMapper) {
        return new ExceptionHandlerFilter(objectMapper);
    }

    /**
     * JWT Authentication Entry Point bean
     * Handles 401 unauthorized responses with proper JSON format
     *
     * @ConditionalOnMissingBean allows services to override with custom entry point
     */
    @Bean
    @ConditionalOnMissingBean
    public JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
        return new JwtAuthenticationEntryPoint(objectMapper);
    }

    /**
     * Security Logging Filter bean
     * Logs which endpoints are authenticated vs public
     *
     * @ConditionalOnMissingBean allows services to override with custom filter
     */
    @Bean
    @ConditionalOnMissingBean
    public SecurityLoggingFilter securityLoggingFilter() {
        return new SecurityLoggingFilter();
    }
}
