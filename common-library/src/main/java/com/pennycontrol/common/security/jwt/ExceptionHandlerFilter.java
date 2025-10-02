package com.pennycontrol.common.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pennycontrol.common.exception.ErrorCode;
import com.pennycontrol.common.exception.ErrorResponse;
import com.pennycontrol.common.exception.UnauthorizedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class ExceptionHandlerFilter extends OncePerRequestFilter {
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            filterChain.doFilter(request, response);
        } catch (UnauthorizedException ex) {
            log.error("Unauthorized exception in filter: {}", ex.getMessage());
            handleException(response, request, ex.getErrorCode(), ex.getMessage(), HttpStatus.UNAUTHORIZED);
        } catch (Exception ex) {
            log.error("Unexpected exception in filter", ex);
            handleException(response, request, ErrorCode.INTERNAL_SERVER_ERROR,
                    "An error occurred during authentication", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void handleException(
            HttpServletResponse response,
            HttpServletRequest request,
            ErrorCode errorCode,
            String message,
            HttpStatus status) throws IOException {

        ErrorResponse errorResponse = ErrorResponse.of(
                status.value(),
                errorCode,
                message,
                request.getRequestURI()
        );

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
