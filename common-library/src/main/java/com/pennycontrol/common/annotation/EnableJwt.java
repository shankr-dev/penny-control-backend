package com.pennycontrol.common.annotation;

import com.pennycontrol.common.config.JwtConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enable JWT Security features including:
 * - JWT Token Provider
 * - JWT Authentication Filter
 * - JWT Properties Configuration
 * - Exception Handler Filter
 *
 * Usage:
 * <pre>
 * {@code
 * @SpringBootApplication
 * @EnableJwt
 * public class MyApplication {
 *     public static void main(String[] args) {
 *         SpringApplication.run(MyApplication.class, args);
 *     }
 * }
 * }
 * </pre>
 *
 * This will automatically configure:
 * - JwtTokenProvider bean
 * - JwtAuthenticationFilter bean
 * - ExceptionHandlerFilter bean
 * - JwtProperties (from application.yml)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(JwtConfiguration.class)
public @interface EnableJwt {
}
