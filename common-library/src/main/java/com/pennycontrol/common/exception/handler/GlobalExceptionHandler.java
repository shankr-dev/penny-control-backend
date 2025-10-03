package com.pennycontrol.common.exception.handler;

import com.pennycontrol.common.dto.ApiResponse;
import com.pennycontrol.common.exception.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Global Exception Handler for all REST API exceptions
 * Extends ResponseEntityExceptionHandler to handle Spring MVC exceptions
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

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

    /**
     * Override Spring MVC's handler for MethodArgumentNotValidException (400 - Validation errors)
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        log.error("Validation failed: {}", ex.getMessage());

        List<ApiResponse.ValidationError> validationErrors = ex.getBindingResult().getAllErrors().stream()
                .map(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    return ApiResponse.ValidationError.of(fieldName, errorMessage);
                })
                .toList();

        String path = extractPath(request);
        ApiResponse.ErrorDetails error = ApiResponse.ErrorDetails.of(
                ErrorCode.VALIDATION_ERROR.getCode(),
                ErrorCode.VALIDATION_ERROR.name(),
                "Validation failed for one or more fields",
                "Please check the errors field for specific validation issues",
                path,
                validationErrors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(error));
    }

    /**
     * Override Spring MVC's handler for HttpRequestMethodNotSupportedException (405 - Method Not Allowed)
     */
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        log.error("Method not supported: {}", ex.getMessage());

        String supportedMethods = ex.getSupportedHttpMethods() != null
                ? ex.getSupportedHttpMethods().stream()
                .map(HttpMethod::name)
                .collect(Collectors.joining(", "))
                : "N/A";

        String path = extractPath(request);
        ApiResponse.ErrorDetails error = ApiResponse.ErrorDetails.of(
                ErrorCode.METHOD_NOT_ALLOWED.getCode(),
                ErrorCode.METHOD_NOT_ALLOWED.name(),
                String.format("Request method '%s' is not supported", ex.getMethod()),
                String.format("Supported methods: %s", supportedMethods),
                path
        );

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(ApiResponse.error(error));
    }

    /**
     * Override Spring MVC's handler for HttpMediaTypeNotSupportedException (415 - Unsupported Media Type)
     */
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        log.error("Media type not supported: {}", ex.getMessage());

        String supportedTypes = String.join(", ", ex.getSupportedMediaTypes().stream().map(Object::toString).toList());

        String path = extractPath(request);
        ApiResponse.ErrorDetails error = ApiResponse.ErrorDetails.of(
                ErrorCode.UNSUPPORTED_MEDIA_TYPE.getCode(),
                ErrorCode.UNSUPPORTED_MEDIA_TYPE.name(),
                String.format("Media type '%s' is not supported", ex.getContentType()),
                String.format("Supported media types: %s", supportedTypes),
                path
        );

        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(ApiResponse.error(error));
    }

    /**
     * Override Spring MVC's handler for MissingServletRequestParameterException (400 - Missing Parameter)
     */
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        log.error("Missing request parameter: {}", ex.getMessage());

        String path = extractPath(request);
        ApiResponse.ErrorDetails error = ApiResponse.ErrorDetails.of(
                ErrorCode.MISSING_PARAMETER.getCode(),
                ErrorCode.MISSING_PARAMETER.name(),
                String.format("Required parameter '%s' is missing", ex.getParameterName()),
                String.format("Parameter '%s' of type '%s' is required", ex.getParameterName(), ex.getParameterType()),
                path
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(error));
    }

    /**
     * Override Spring MVC's handler for NoResourceFoundException (404 - Not Found) - Spring Boot 3.2+
     */
    @Override
    protected ResponseEntity<Object> handleNoResourceFoundException(
            NoResourceFoundException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        log.error("No resource found: {}", ex.getMessage());

        String path = extractPath(request);
        ApiResponse.ErrorDetails error = ApiResponse.ErrorDetails.of(
                ErrorCode.NOT_FOUND.getCode(),
                ErrorCode.NOT_FOUND.name(),
                ErrorCode.NOT_FOUND.getMessage(),
                String.format("No endpoint found for %s %s", ex.getHttpMethod(), ex.getResourcePath()),
                path
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(error));
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

    /**
     * Extract request path from WebRequest
     */
    private String extractPath(WebRequest request) {
        String description = request.getDescription(false);
        // description format: "uri=/api/v1/path"
        if (description.startsWith("uri=")) {
            return description.substring(4);
        }
        return description;
    }

    /**
     * Determine HTTP status from ErrorCode
     */
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
