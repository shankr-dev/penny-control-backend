package com.pennycontrol.authservice.scheduler;

import com.pennycontrol.authservice.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Scheduled job to clean up expired and old revoked refresh tokens
 * Runs daily at 2 AM to prevent database bloat
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Cleanup old tokens
     * Runs daily at 2 AM
     * <p>
     * This job:
     * 1. Deletes expired tokens immediately
     * 2. Deletes revoked tokens older than 30 days (keeps recent ones for audit)
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupOldTokens() {
        log.info("Starting scheduled token cleanup job");

        // Delete expired tokens
        int expiredDeleted = refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        log.info("Deleted {} expired tokens", expiredDeleted);

        // Delete revoked tokens older than 30 days (keep recent ones for audit)
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        int revokedDeleted = refreshTokenRepository.deleteOldRevokedTokens(cutoffDate);
        log.info("Deleted {} old revoked tokens (older than 30 days)", revokedDeleted);

        log.info("Token cleanup completed. Total deleted: {}", expiredDeleted + revokedDeleted);
    }
}
