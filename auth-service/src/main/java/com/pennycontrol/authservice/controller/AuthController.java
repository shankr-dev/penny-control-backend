package com.pennycontrol.authservice.controller;

import com.pennycontrol.authservice.dto.request.RefreshTokenRequest;
import com.pennycontrol.authservice.dto.request.UserLoginRequest;
import com.pennycontrol.authservice.dto.request.UserSignupRequest;
import com.pennycontrol.authservice.dto.response.AuthResponse;
import com.pennycontrol.authservice.dto.response.RegistrationResponse;
import com.pennycontrol.authservice.service.AuthService;
import com.pennycontrol.common.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegistrationResponse>> register(
            @Valid @RequestBody UserSignupRequest request) {
        log.info("Received signup request for email: {}", request.getEmail());

        RegistrationResponse response = authService.signup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody UserLoginRequest request,
            HttpServletRequest httpRequest) {
        log.info("Received login request for email: {}", request.getEmail());

        AuthResponse response = authService.login(request, httpRequest);

        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        log.info("Received token refresh request");

        AuthResponse response = authService.refreshToken(request.getRefreshToken(), httpRequest);

        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @DeleteMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        log.info("Received logout request");

        authService.logout();

        return ResponseEntity.ok(ApiResponse.success("Successfully logged out from all devices", null));
    }

    @DeleteMapping("/logout-device")
    public ResponseEntity<ApiResponse<Void>> logoutSingleDevice(
            @Valid @RequestBody RefreshTokenRequest request) {
        log.info("Received single device logout request");

        authService.logoutSingleDevice(request.getRefreshToken());

        return ResponseEntity.ok(ApiResponse.success("Logged out from this device", null));
    }
}
