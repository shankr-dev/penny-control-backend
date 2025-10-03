package com.pennycontrol.authservice.service.impl;

import com.pennycontrol.authservice.entity.RefreshToken;
import com.pennycontrol.common.entity.User;
import com.pennycontrol.authservice.repository.RefreshTokenRepository;
import com.pennycontrol.authservice.service.RefreshTokenService;
import com.pennycontrol.common.exception.ErrorCode;
import com.pennycontrol.common.exception.UnauthorizedException;
import com.pennycontrol.common.exception.ValidationException;
import com.pennycontrol.common.security.jwt.JwtProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user, String token, String ipAddress, String userAgent) {
        String tokenHash = hashToken(token);
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(jwtProperties.getRefreshTokenExpiration() / 1000);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiresAt(expiresAt)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .isRevoked(false)
                .usageCount(0)
                .build();

        RefreshToken saved = refreshTokenRepository.save(refreshToken);
        log.info("Created refresh token for user ID: {}", user.getId());

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken validateRefreshToken(String token) {
        String tokenHash = hashToken(token);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> {
                    log.warn("Refresh token not found");
                    return new UnauthorizedException(ErrorCode.INVALID_TOKEN, "Invalid refresh token");
                });

        // Check if revoked
        if (refreshToken.getIsRevoked()) {
            log.warn("Attempted to use revoked refresh token for user ID: {}", refreshToken.getUser().getId());
            throw new UnauthorizedException(ErrorCode.INVALID_TOKEN, "Refresh token has been revoked");
        }

        // Check if expired
        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Attempted to use expired refresh token for user ID: {}", refreshToken.getUser().getId());
            throw new UnauthorizedException(ErrorCode.INVALID_TOKEN, "Refresh token has expired");
        }

        return refreshToken;
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String token) {
        String tokenHash = hashToken(token);

        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException(ErrorCode.INVALID_TOKEN, "Invalid refresh token"));

        refreshToken.setIsRevoked(true);
        refreshToken.setRevokedAt(LocalDateTime.now());

        refreshTokenRepository.save(refreshToken);
        log.info("Revoked refresh token for user ID: {} (kept for audit)", refreshToken.getUser().getId());
    }

    @Override
    @Transactional
    public void deleteRefreshToken(String token, Long userId) {
        String tokenHash = hashToken(token);

        // Find the token first to verify ownership
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> {
                    log.warn("Attempted to delete non-existent refresh token");
                    return new UnauthorizedException(ErrorCode.INVALID_TOKEN, "Invalid refresh token");
                });

        // Verify token belongs to the authenticated user
        if (!refreshToken.getUser().getId().equals(userId)) {
            log.warn("Security violation: User {} attempted to delete token belonging to user {}",
                    userId, refreshToken.getUser().getId());
            throw new UnauthorizedException(ErrorCode.INVALID_TOKEN, "Invalid refresh token");
        }

        // Delete the token
        refreshTokenRepository.delete(refreshToken);
        log.info("Deleted refresh token for user ID: {} (single device logout)", userId);
    }

    @Override
    @Transactional
    public void deleteAllUserTokens(Long userId) {
        int deletedCount = refreshTokenRepository.deleteByUserId(userId);
        log.info("Deleted {} refresh tokens for user ID: {} (logout from all devices)", deletedCount, userId);
    }

    @Override
    @Transactional
    public void markTokenAsUsed(RefreshToken refreshToken) {
        refreshToken.setLastUsedAt(LocalDateTime.now());
        refreshToken.setUsageCount(refreshToken.getUsageCount() + 1);
        refreshTokenRepository.save(refreshToken);
    }

    /**
     * Hash token using SHA-256
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 algorithm not available", e);
            throw new ValidationException("Token hashing failed: SHA-256 algorithm not available");
        }
    }
}
