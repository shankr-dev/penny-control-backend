package com.pennycontrol.common.config;

import com.pennycontrol.common.security.RoleCheckAspect;
import com.pennycontrol.common.security.SecurityProperties;
import com.pennycontrol.common.security.jwt.JwtAuthenticationEntryPoint;
import com.pennycontrol.common.security.jwt.SecurityLoggingFilter;
import com.pennycontrol.common.security.jwt.ExceptionHandlerFilter;
import com.pennycontrol.common.security.jwt.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Common Security Configuration
 * <p>
 * This configuration is automatically imported when @EnableSecurity is used.
 * <p>
 * Provides:
 * - SecurityFilterChain with JWT authentication
 * - PasswordEncoder (BCrypt)
 * - AuthenticationManager
 * - RoleCheckAspect for @RequireRole annotation
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityConfiguration {

    /**
     * Security Filter Chain
     * Configures HTTP security with JWT authentication
     */
    @Bean
    @ConditionalOnMissingBean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            ExceptionHandlerFilter exceptionHandlerFilter,
            SecurityLoggingFilter securityLoggingFilter,
            SecurityProperties securityProperties,
            @Qualifier("corsConfigurationSource") CorsConfigurationSource corsConfigurationSource,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint
    ) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                )
                .authorizeHttpRequests(auth -> {
                    // Public endpoints from configuration
                    if (securityProperties.getPublicEndpoints() != null
                            && !securityProperties.getPublicEndpoints().isEmpty()) {
                        auth.requestMatchers(
                                securityProperties.getPublicEndpoints().toArray(new String[0])
                        ).permitAll();
                    }

                    // Default public endpoints
                    auth.requestMatchers(
                            "/actuator/health"
                    ).permitAll();

                    // All other requests require authentication
                    auth.anyRequest().authenticated();
                })
                .addFilterBefore(exceptionHandlerFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jwtAuthenticationFilter, ExceptionHandlerFilter.class)
                .addFilterAfter(securityLoggingFilter, JwtAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCrypt Password Encoder
     * Used for hashing passwords
     */
    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication Manager
     * Required for authentication operations
     */
    @Bean
    @ConditionalOnMissingBean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Role Check Aspect
     * Enables @RequireRole annotation for method-level security
     */
    @Bean
    @ConditionalOnMissingBean
    public RoleCheckAspect roleCheckAspect() {
        return new RoleCheckAspect();
    }
}
