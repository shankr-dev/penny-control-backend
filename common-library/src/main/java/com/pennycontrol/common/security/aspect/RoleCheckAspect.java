package com.pennycontrol.common.security.aspect;

import com.pennycontrol.common.annotation.RequireRole;
import com.pennycontrol.common.dto.UserPrincipal;
import com.pennycontrol.common.exception.ErrorCode;
import com.pennycontrol.common.exception.UnauthorizedException;
import com.pennycontrol.common.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

/**
 * Aspect to enforce role-based access control using @RequireRole annotation
 */
@Slf4j
@Aspect
@Component
public class RoleCheckAspect {

    /**
     * Intercepts all methods annotated with @RequireRole
     * Executes BEFORE the actual method
     */
    @Before("@annotation(com.pennycontrol.common.annotation.RequireRole)")
    public void checkRole(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // Get the annotation from the method
        RequireRole requireRole = method.getAnnotation(RequireRole.class);

        if (requireRole != null) {
            String[] requiredRoles = requireRole.value();
            boolean requireAll = requireRole.requireAll();

            log.debug("Checking roles for method: {}.{}",
                    method.getDeclaringClass().getSimpleName(),
                    method.getName());
            log.debug("Required roles: {}, requireAll: {}",
                    Arrays.toString(requiredRoles), requireAll);

            // Get current user
            UserPrincipal currentUser = SecurityUtils.getCurrentUser()
                    .orElseThrow(() -> new UnauthorizedException("User not authenticated"));

            Set<String> userRoles = currentUser.getRoles();

            // Check if user has required roles
            boolean hasAccess = requireAll
                    ? hasAllRoles(userRoles, requiredRoles)
                    : hasAnyRole(userRoles, requiredRoles);

            if (!hasAccess) {
                log.warn("Access denied for user: {} to method: {}.{}",
                        currentUser.getUsername(),
                        method.getDeclaringClass().getSimpleName(),
                        method.getName());

                throw new UnauthorizedException(
                        ErrorCode.ACCESS_DENIED,
                        "You don't have the required role(s) to access this resource"
                );
            }

            log.debug("Access granted for user: {}", currentUser.getUsername());
        }
    }

    /**
     * Check if user has ANY of the required roles
     */
    private boolean hasAnyRole(Set<String> userRoles, String[] requiredRoles) {
        return Arrays.stream(requiredRoles)
                .anyMatch(userRoles::contains);
    }

    /**
     * Check if user has ALL required roles
     */
    private boolean hasAllRoles(Set<String> userRoles, String[] requiredRoles) {
        return Arrays.stream(requiredRoles)
                .allMatch(userRoles::contains);
    }
}
