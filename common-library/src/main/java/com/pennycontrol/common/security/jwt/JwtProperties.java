package com.pennycontrol.common.security.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private long accessTokenExpiration = 3600000; // 1 hour in milliseconds
    private long refreshTokenExpiration = 2592000000L; // 30 days in milliseconds
    private String issuer = "penny-control";
    private String tokenPrefix = "Bearer ";
    private String headerName = "Authorization";
}
