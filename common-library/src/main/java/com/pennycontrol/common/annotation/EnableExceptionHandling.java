package com.pennycontrol.common.annotation;

import com.pennycontrol.common.config.ExceptionHandlingConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enable Common Exception Handling features including:
 * - Global Exception Handler for REST APIs
 * - Validation error handling
 * - Business exception handling
 * - Authentication/Authorization exception handling
 *
 * Usage:
 * <pre>
 * {@code
 * @SpringBootApplication
 * @EnableExceptionHandling
 * public class MyApplication {
 *     public static void main(String[] args) {
 *         SpringApplication.run(MyApplication.class, args);
 *     }
 * }
 * }
 * </pre>
 *
 * This will automatically configure:
 * - GlobalExceptionHandler (@RestControllerAdvice)
 * - Standardized error responses (ErrorResponse)
 * - Validation error mapping
 * - HTTP status code mapping for different exception types
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(ExceptionHandlingConfiguration.class)
public @interface EnableExceptionHandling {
}
