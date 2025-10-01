package com.pennycontrol.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for role-based access control
 * Usage: @RequireRole({"ROLE_ADMIN", "ROLE_MANAGER"})
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    String[] value();

    boolean requireAll() default false; // If true, user must have all roles; if false, any role is sufficient
}
