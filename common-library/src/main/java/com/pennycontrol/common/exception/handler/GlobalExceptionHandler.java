package com.pennycontrol.common.exception.handler;

import com.pennycontrol.common.dto.ApiResponse;
import com.pennycontrol.common.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request) {
        log.error("Business exception: {}", ex.getMessage(), ex);

        HttpStatus status = determineHttpStatus(ex.getErrorCode());
        ApiResponse.ErrorDetails error = ApiResponse.ErrorDetails.of(
                ex.getErrorCode().getCode(),
                ex.getErrorCode().name(),
                ex.getMessage(),
                ex.getErrorCode().getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(ApiResponse.error(error));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(
            ResourceNotFoundException ex,
            HttpServletRequest request) {
        log.error("Resource not found: {}", ex.getMessage());

        ApiResponse.ErrorDetails error = ApiResponse.ErrorDetails.of(
                ex.getErrorCode().getCode(),
                ex.getErrorCode().name(),
                ex.getMessage(),
                ex.getErrorCode().getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(error));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorizedException(
            UnauthorizedException ex,
            HttpServletRequest request) {
        log.error("Unauthorized: {}", ex.getMessage());

        ApiResponse.ErrorDetails error = ApiResponse.ErrorDetails.of(
                ex.getErrorCode().getCode(),
                ex.getErrorCode().name(),
                ex.getMessage(),
                ex.getErrorCode().getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(error));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            ValidationException ex,
            HttpServletRequest request) {
        log.error("Validation exception: {}", ex.getMessage());

        // Convert Map<String, String> to List<ValidationError>
        List<ApiResponse.ValidationError> validationErrors = null;
        if (ex.getValidationErrors() != null && !ex.getValidationErrors().isEmpty()) {
            validationErrors = ex.getValidationErrors().entrySet().stream()
                    .map(entry -> ApiResponse.ValidationError.of(entry.getKey(), entry.getValue()))
                    .toList();
        }

        ApiResponse.ErrorDetails error = ApiResponse.ErrorDetails.of(
                ex.getErrorCode().getCode(),
                ex.getErrorCode().name(),
                ex.getMessage(),
                "Please check the errors field for specific validation issues",
                request.getRequestURI(),
                validationErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(error));
    }


    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {
        log.error("Type mismatch: {}", ex.getMessage());

        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s",
                ex.getValue(), ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");

        ApiResponse.ErrorDetails error = ApiResponse.ErrorDetails.of(
                ErrorCode.INVALID_INPUT.getCode(),
                ErrorCode.INVALID_INPUT.name(),
                message,
                ErrorCode.INVALID_INPUT.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(error));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request) {
        log.error("Authentication failed: {}", ex.getMessage());

        ErrorCode errorCode = ex instanceof BadCredentialsException
                ? ErrorCode.INVALID_CREDENTIALS
                : ErrorCode.UNAUTHORIZED;

        ApiResponse.ErrorDetails error = ApiResponse.ErrorDetails.of(
                errorCode.getCode(),
                errorCode.name(),
                ex.getMessage(),
                errorCode.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.error(error));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request) {
        log.error("Access denied: {}", ex.getMessage());

        ApiResponse.ErrorDetails error = ApiResponse.ErrorDetails.of(
                ErrorCode.ACCESS_DENIED.getCode(),
                ErrorCode.ACCESS_DENIED.name(),
                "You don't have permission to access this resource",
                ErrorCode.ACCESS_DENIED.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error(error));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        log.error("Validation failed: {}", ex.getMessage());

        List<ApiResponse.ValidationError> validationErrors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    return ApiResponse.ValidationError.of(fieldName, errorMessage);
                })
                .toList();

        ApiResponse.ErrorDetails error = ApiResponse.ErrorDetails.of(
                ErrorCode.VALIDATION_ERROR.getCode(),
                ErrorCode.VALIDATION_ERROR.name(),
                "Validation failed for one or more fields",
                "Please check the errors field for specific validation issues",
                request.getRequestURI(),
                validationErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(error));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(
            Exception ex,
            HttpServletRequest request) {
        log.error("Unexpected error occurred", ex);

        ApiResponse.ErrorDetails error = ApiResponse.ErrorDetails.of(
                ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
                ErrorCode.INTERNAL_SERVER_ERROR.name(),
                "An unexpected error occurred. Please try again later.",
                ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(error));
    }

    private HttpStatus determineHttpStatus(ErrorCode errorCode) {
        return switch (errorCode) {
            case UNAUTHORIZED, INVALID_TOKEN, INVALID_CREDENTIALS -> HttpStatus.UNAUTHORIZED;
            case ACCESS_DENIED -> HttpStatus.FORBIDDEN;
            case RESOURCE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case VALIDATION_ERROR, INVALID_INPUT, RESOURCE_ALREADY_EXISTS -> HttpStatus.BAD_REQUEST;
            case BUSINESS_RULE_VIOLATION, OPERATION_NOT_ALLOWED -> HttpStatus.CONFLICT;
            case SERVICE_UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
