package com.pennycontrol.authservice.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private Long userId;
    private String email;
    private String name;
    private Set<String> roles;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
}
