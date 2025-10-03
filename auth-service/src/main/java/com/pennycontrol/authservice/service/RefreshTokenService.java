package com.pennycontrol.authservice.service;

import com.pennycontrol.authservice.entity.RefreshToken;
import com.pennycontrol.authservice.entity.User;

public interface RefreshTokenService {

    /**
     * Create and store a new refresh token
     *
     * @param user User for whom to create the token
     * @param token The actual JWT refresh token string
     * @param ipAddress Client IP address
     * @param userAgent Client user agent
     * @return Created RefreshToken entity
     */
    RefreshToken createRefreshToken(User user, String token, String ipAddress, String userAgent);

    /**
     * Validate and retrieve refresh token
     *
     * @param token The refresh token string to validate
     * @return RefreshToken entity if valid
     */
    RefreshToken validateRefreshToken(String token);

    /**
     * Revoke a specific refresh token (used during token rotation)
     * Marks token as revoked but keeps it in DB for audit trail
     *
     * @param token The token to revoke
     */
    void revokeRefreshToken(String token);

    /**
     * Delete a specific refresh token (used during logout)
     * Immediately removes token from database
     * Verifies token belongs to the user for security
     *
     * @param token The token to delete
     * @param userId The authenticated user ID
     */
    void deleteRefreshToken(String token, Long userId);

    /**
     * Delete all refresh tokens for a user (logout from all devices)
     * Used for: security incidents, password changes, user preference
     *
     * @param userId User ID
     */
    void deleteAllUserTokens(Long userId);

    /**
     * Increment usage count and update last used timestamp
     *
     * @param refreshToken The token that was used
     */
    void markTokenAsUsed(RefreshToken refreshToken);
}
