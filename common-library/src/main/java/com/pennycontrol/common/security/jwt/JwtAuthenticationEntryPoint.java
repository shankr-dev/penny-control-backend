package com.pennycontrol.common.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pennycontrol.common.dto.ApiResponse;
import com.pennycontrol.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * Custom Authentication Entry Point
 * Handles 401 Unauthorized responses with proper JSON error format
 */
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        log.warn("Unauthorized access attempt to: {} {}", request.getMethod(), request.getRequestURI());

        ApiResponse.ErrorDetails error = ApiResponse.ErrorDetails.of(
                ErrorCode.UNAUTHORIZED.getCode(),
                ErrorCode.UNAUTHORIZED.name(),
                "Authentication required. Please provide a valid access token.",
                "Access token is missing or invalid in the Authorization header",
                request.getRequestURI()
        );

        ApiResponse<Void> apiResponse = ApiResponse.error(error);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
