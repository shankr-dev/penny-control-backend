package com.pennycontrol.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {
    private List<String> publicEndpoints = new ArrayList<>();
    private List<String> allowedOrigins = new ArrayList<>();
    private boolean csrfEnabled = false;
}
