package com.pennycontrol.common.util;

import com.pennycontrol.common.dto.UserPrincipal;
import com.pennycontrol.common.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class SecurityUtils {

    private SecurityUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Get the current authenticated user's principal
     */
    public static Optional<UserPrincipal> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            return Optional.empty();
        }

        if (authentication.getPrincipal() instanceof UserPrincipal userPrincipal) {
            return Optional.of(userPrincipal);
        }

        return Optional.empty();
    }

    /**
     * Get the current authenticated user's ID
     */
    public static Long getCurrentUserId() {
        return getCurrentUser()
                .map(UserPrincipal::getId)
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));
    }

    /**
     * Get the current authenticated user's email
     */
    public static String getCurrentUserEmail() {
        return getCurrentUser()
                .map(UserPrincipal::getEmail)
                .orElseThrow(() -> new UnauthorizedException("User not authenticated"));
    }

    /**
     * Check if the current user has a specific role
     */
    public static boolean hasRole(String role) {
        return getCurrentUser()
                .map(user -> user.getRoles().contains(role))
                .orElse(false);
    }

    /**
     * Check if the current user has any of the specified roles
     */
    public static boolean hasAnyRole(String... roles) {
        return getCurrentUser()
                .map(user -> {
                    for (String role : roles) {
                        if (user.getRoles().contains(role)) {
                            return true;
                        }
                    }
                    return false;
                })
                .orElse(false);
    }

    /**
     * Get the current authentication object
     */
    public static Optional<Authentication> getAuthentication() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication());
    }
}
