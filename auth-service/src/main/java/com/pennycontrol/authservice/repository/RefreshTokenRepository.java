package com.pennycontrol.authservice.repository;

import com.pennycontrol.authservice.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Find refresh token by token hash
     */
    Optional<RefreshToken> findByTokenHash(String tokenHash);

    /**
     * Delete expired tokens (cleanup job)
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    int deleteExpiredTokens(LocalDateTime now);

    /**
     * Delete revoked tokens older than cutoff date (cleanup job)
     * This removes old revoked tokens while keeping recent ones for audit
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.isRevoked = true AND rt.revokedAt < :cutoffDate")
    int deleteOldRevokedTokens(LocalDateTime cutoffDate);

    /**
     * Delete refresh token by token hash (immediate deletion for logout)
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.tokenHash = :tokenHash")
    int deleteByTokenHash(String tokenHash);

    /**
     * Delete all refresh tokens for a user (hard logout from all devices)
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
    int deleteByUserId(Long userId);

}
