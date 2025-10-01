package com.pennycontrol.common.util;

import com.pennycontrol.common.exception.ValidationException;

import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^[+]?[(]?[0-9]{1,4}[)]?[-\\s.]?[(]?[0-9]{1,4}[)]?[-\\s.]?[0-9]{1,9}$"
    );

    private ValidationUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate phone number format
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validate that a string is not null or empty
     */
    public static void requireNonEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " cannot be empty");
        }
    }

    /**
     * Validate that an object is not null
     */
    public static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new ValidationException(fieldName + " cannot be null");
        }
    }

    /**
     * Validate string length
     */
    public static void validateLength(String value, String fieldName, int min, int max) {
        if (value != null) {
            int length = value.length();
            if (length < min || length > max) {
                throw new ValidationException(
                        String.format("%s must be between %d and %d characters", fieldName, min, max)
                );
            }
        }
    }

    /**
     * Validate that a number is within range
     */
    public static void validateRange(Number value, String fieldName, Number min, Number max) {
        if (value != null) {
            double doubleValue = value.doubleValue();
            if (doubleValue < min.doubleValue() || doubleValue > max.doubleValue()) {
                throw new ValidationException(
                        String.format("%s must be between %s and %s", fieldName, min, max)
                );
            }
        }
    }

    /**
     * Validate email and throw exception if invalid
     */
    public static void validateEmail(String email) {
        if (!isValidEmail(email)) {
            throw new ValidationException("Invalid email format");
        }
    }

    /**
     * Validate phone and throw exception if invalid
     */
    public static void validatePhone(String phone) {
        if (!isValidPhone(phone)) {
            throw new ValidationException("Invalid phone number format");
        }
    }
}
