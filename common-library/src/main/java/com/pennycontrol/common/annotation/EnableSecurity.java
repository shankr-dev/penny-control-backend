package com.pennycontrol.common.annotation;

import com.pennycontrol.common.config.SecurityConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enable Common Security features including:
 * - Base Security Configuration (SecurityFilterChain)
 * - Password Encoder (BCrypt)
 * - Authentication Manager
 * - Security Properties Configuration
 * - Role-Based Access Control (RBAC) via AOP
 * <p>
 * Usage:
 * <pre>
 * {@code
 * @SpringBootApplication
 * @EnableSecurity
 * public class MyApplication {
 *     public static void main(String[] args) {
 *         SpringApplication.run(MyApplication.class, args);
 *     }
 * }
 * }
 * </pre>
 *
 * This will automatically configure:
 * - SecurityFilterChain with JWT authentication
 * - BCryptPasswordEncoder
 * - AuthenticationManager
 * - RoleCheckAspect for @RequireRole annotation
 * - SecurityProperties (from application.yml)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(SecurityConfiguration.class)
public @interface EnableSecurity {
}
