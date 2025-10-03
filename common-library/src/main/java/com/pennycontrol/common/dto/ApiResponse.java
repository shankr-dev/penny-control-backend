package com.pennycontrol.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Unified API Response wrapper for all endpoints
 * Provides consistent response structure for both success and error cases
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private LocalDateTime timestamp;
    private String message;
    private T data;
    private Map<String, Object> metadata;
    private ErrorDetails error;

    /**
     * Success response with data
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .timestamp(LocalDateTime.now())
                .data(data)
                .build();
    }

    /**
     * Success response with message and data
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .timestamp(LocalDateTime.now())
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Success response with message only
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .timestamp(LocalDateTime.now())
                .message(message)
                .build();
    }

    /**
     * Success response with message, data, and metadata (for pagination, etc.)
     */
    public static <T> ApiResponse<T> success(String message, T data, Map<String, Object> metadata) {
        return ApiResponse.<T>builder()
                .success(true)
                .timestamp(LocalDateTime.now())
                .message(message)
                .data(data)
                .metadata(metadata)
                .build();
    }

    /**
     * Error response with error details
     */
    public static <T> ApiResponse<T> error(ErrorDetails error) {
        return ApiResponse.<T>builder()
                .success(false)
                .timestamp(LocalDateTime.now())
                .error(error)
                .build();
    }

    /**
     * Nested error details class
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorDetails {
        private String code;
        private String type;
        private String message;
        private String detail;
        private String path;
        private List<ValidationError> errors;

        public static ErrorDetails of(String code, String type, String message, String path) {
            return ErrorDetails.builder()
                    .code(code)
                    .type(type)
                    .message(message)
                    .path(path)
                    .build();
        }

        public static ErrorDetails of(String code, String type, String message, String detail, String path) {
            return ErrorDetails.builder()
                    .code(code)
                    .type(type)
                    .message(message)
                    .detail(detail)
                    .path(path)
                    .build();
        }

        public static ErrorDetails of(String code, String type, String message, String detail, String path, List<ValidationError> errors) {
            return ErrorDetails.builder()
                    .code(code)
                    .type(type)
                    .message(message)
                    .detail(detail)
                    .path(path)
                    .errors(errors)
                    .build();
        }
    }

    /**
     * Validation error for field-level errors
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;

        public static ValidationError of(String field, String message) {
            return ValidationError.builder()
                    .field(field)
                    .message(message)
                    .build();
        }
    }
}
