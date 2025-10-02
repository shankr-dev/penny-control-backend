package com.pennycontrol.authservice.service.impl;

import com.pennycontrol.authservice.dto.request.UserLoginRequest;
import com.pennycontrol.authservice.dto.request.UserSignupRequest;
import com.pennycontrol.authservice.dto.response.AuthResponse;
import com.pennycontrol.authservice.dto.response.RegistrationResponse;
import com.pennycontrol.authservice.entity.RefreshToken;
import com.pennycontrol.authservice.entity.Role;
import com.pennycontrol.authservice.entity.User;
import com.pennycontrol.authservice.repository.RoleRepository;
import com.pennycontrol.authservice.repository.UserRepository;
import com.pennycontrol.authservice.service.AuthService;
import com.pennycontrol.authservice.service.RefreshTokenService;
import com.pennycontrol.common.dto.UserPrincipal;
import com.pennycontrol.common.exception.ErrorCode;
import com.pennycontrol.common.exception.UnauthorizedException;
import com.pennycontrol.common.exception.ValidationException;
import com.pennycontrol.common.security.jwt.JwtProperties;
import com.pennycontrol.common.security.jwt.JwtTokenProvider;
import com.pennycontrol.common.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;


    @Override
    @Transactional
    public RegistrationResponse signup(UserSignupRequest request) {
        log.info("Processing signup request for email: {}", request.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Signup failed: Email already exists - {}", request.getEmail());
            throw new ValidationException("Email already registered");
        }

        // Check if phone number already exists (if provided)
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().trim().isEmpty()
                && userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            log.warn("Signup failed: Phone number already exists - {}", request.getPhoneNumber());
            throw new ValidationException("Phone number already registered");
        }

        // Get default USER role
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new ValidationException("Default role ROLE_USER not found"));

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName() != null ? request.getName().trim() : null)
                .phoneNumber(request.getPhoneNumber() != null ? request.getPhoneNumber().trim() : null)
                .currency(request.getCurrency() != null ? request.getCurrency().toUpperCase() : "USD")
                .roles(roles)
                .emailVerified(false)
                .accountLocked(false)
                .enabled(true)
                .build();

        // Save user to database
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        // Return success response without tokens
        return RegistrationResponse.builder()
                .email(savedUser.getEmail())
                .emailVerificationRequired(false)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse login(UserLoginRequest request, HttpServletRequest httpRequest) {
        log.info("Processing login request for email: {}", request.getEmail());

        // Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: User not found - {}", request.getEmail());
                    return new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS, "Invalid email or password");
                });

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            log.warn("Login failed: Invalid password for user - {}", request.getEmail());
            throw new UnauthorizedException(ErrorCode.INVALID_CREDENTIALS, "Invalid email or password");
        }

        // Check if account is enabled
        if (!user.isEnabled()) {
            log.warn("Login failed: Account disabled - {}", request.getEmail());
            throw new UnauthorizedException(ErrorCode.ACCESS_DENIED, "Account is disabled");
        }

        // Check if account is locked
        if (user.isAccountLocked()) {
            log.warn("Login failed: Account locked - {}", request.getEmail());
            throw new UnauthorizedException(ErrorCode.ACCESS_DENIED, "Account is locked");
        }

        // Create UserPrincipal with role names
        UserPrincipal userPrincipal = createUserPrincipal(user);

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(userPrincipal);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userPrincipal);

        // Store refresh token in database
        String ipAddress = extractIpAddress(httpRequest);
        String userAgent = extractUserAgent(httpRequest);
        refreshTokenService.createRefreshToken(user, refreshToken, ipAddress, userAgent);

        log.info("User logged in successfully: {}", user.getEmail());

        // Build response with tokens
        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .roles(userPrincipal.getRoles())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenExpiration() / 1000)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(String refreshToken, HttpServletRequest httpRequest) {
        log.info("Processing refresh token request");

        // Validate refresh token
        RefreshToken storedToken = refreshTokenService.validateRefreshToken(refreshToken);
        User user = storedToken.getUser();

        // Mark token as used
        refreshTokenService.markTokenAsUsed(storedToken);

        // Revoke old refresh token (token rotation for security)
        refreshTokenService.revokeRefreshToken(refreshToken);

        // Generate new tokens
        UserPrincipal userPrincipal = createUserPrincipal(user);

        String newAccessToken = jwtTokenProvider.generateAccessToken(userPrincipal);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userPrincipal);

        // Store new refresh token
        String ipAddress = extractIpAddress(httpRequest);
        String userAgent = extractUserAgent(httpRequest);
        refreshTokenService.createRefreshToken(user, newRefreshToken, ipAddress, userAgent);

        log.info("Refresh token rotated successfully for user ID: {}", user.getId());

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .roles(userPrincipal.getRoles())
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProperties.getAccessTokenExpiration() / 1000)
                .build();
    }

    @Override
    @Transactional
    public void logout() {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Processing logout request for user ID: {}", userId);

        refreshTokenService.deleteAllUserTokens(userId);

        log.info("Logout successful - all refresh tokens deleted for user ID: {}", userId);
    }

    @Override
    @Transactional
    public void logoutSingleDevice(String refreshToken) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Processing single device logout request for user ID: {}", userId);

        refreshTokenService.deleteRefreshToken(refreshToken);

        log.info("Single device logout successful for user ID: {}", userId);
    }

    /**
     * Create UserPrincipal from User entity
     */
    private UserPrincipal createUserPrincipal(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return UserPrincipal.create(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                roleNames
        );
    }

    /**
     * Extract IP address from HTTP request
     */
    private String extractIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    /**
     * Extract user agent from HTTP request
     */
    private String extractUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}
