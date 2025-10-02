package com.pennycontrol.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // Authentication & Authorization
    UNAUTHORIZED("AUTH_001", "Authentication failed"),
    INVALID_TOKEN("AUTH_002", "Invalid or expired token"),
    ACCESS_DENIED("AUTH_003", "Access denied"),
    INVALID_CREDENTIALS("AUTH_004", "Invalid username or password"),

    // Resource
    RESOURCE_NOT_FOUND("RES_001", "Resource not found"),
    RESOURCE_ALREADY_EXISTS("RES_002", "Resource already exists"),

    // Validation
    VALIDATION_ERROR("VAL_001", "Validation failed"),
    INVALID_INPUT("VAL_002", "Invalid input provided"),

    // Business Logic
    BUSINESS_RULE_VIOLATION("BUS_001", "Business rule violation"),
    OPERATION_NOT_ALLOWED("BUS_002", "Operation not allowed"),

    // System
    INTERNAL_SERVER_ERROR("SYS_001", "Internal server error"),
    SERVICE_UNAVAILABLE("SYS_002", "Service temporarily unavailable"),
    DATABASE_ERROR("SYS_003", "Database error occurred"),
    METHOD_NOT_ALLOWED("SYS_004", "HTTP method not allowed for this endpoint");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
