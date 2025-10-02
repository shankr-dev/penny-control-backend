package com.pennycontrol.common.annotation;

import com.pennycontrol.common.config.CorsConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * Enable CORS (Cross-Origin Resource Sharing) configuration
 *
 * Configures CORS settings from application.yml security properties.
 *
 * Usage:
 * <pre>
 * {@code
 * @SpringBootApplication
 * @EnableCors
 * public class MyApplication {
 *     public static void main(String[] args) {
 *         SpringApplication.run(MyApplication.class, args);
 *     }
 * }
 * }
 * </pre>
 *
 * Configuration in application.yml:
 * <pre>
 * security:
 *   allowed-origins:
 *     - http://localhost:3000
 *     - http://localhost:5173
 * </pre>
 *
 * This will automatically configure:
 * - CorsConfigurationSource bean with settings from YAML
 * - Allowed origins, methods, headers
 * - Credentials support
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(CorsConfiguration.class)
public @interface EnableCors {
}
