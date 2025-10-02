package com.pennycontrol.authservice.service;

import com.pennycontrol.authservice.dto.request.UserLoginRequest;
import com.pennycontrol.authservice.dto.request.UserSignupRequest;
import com.pennycontrol.authservice.dto.response.AuthResponse;
import com.pennycontrol.authservice.dto.response.RegistrationResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    /**
     * Register a new user
     *
     * @param request Signup request containing user details
     * @return RegistrationResponse with success message
     */
    RegistrationResponse signup(UserSignupRequest request);

    /**
     * Authenticate user and generate tokens
     *
     * @param request Login request containing credentials
     * @param httpRequest HTTP request for IP and user agent extraction
     * @return AuthResponse with user info and JWT tokens
     */
    AuthResponse login(UserLoginRequest request, HttpServletRequest httpRequest);

    /**
     * Refresh access token using refresh token
     *
     * @param refreshToken Refresh token
     * @param httpRequest HTTP request for IP and user agent extraction
     * @return AuthResponse with new access token and refresh token
     */
    AuthResponse refreshToken(String refreshToken, HttpServletRequest httpRequest);

    /**
     * Logout user from all devices
     * Deletes all refresh tokens for the authenticated user
     */
    void logout();

    /**
     * Logout user from a single device
     * Deletes the specific refresh token provided
     *
     * @param refreshToken The refresh token to delete
     */
    void logoutSingleDevice(String refreshToken);
}
