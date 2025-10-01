package com.pennycontrol.common.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public class ValidationException extends BusinessException {
    private final Map<String, String> validationErrors;

    public ValidationException(String message) {
        super(ErrorCode.VALIDATION_ERROR, message);
        this.validationErrors = null;
    }

    public ValidationException(Map<String, String> validationErrors) {
        super(ErrorCode.VALIDATION_ERROR, "Validation failed for one or more fields");
        this.validationErrors = validationErrors;
    }

    public ValidationException(String message, Map<String, String> validationErrors) {
        super(ErrorCode.VALIDATION_ERROR, message);
        this.validationErrors = validationErrors;
    }
}
