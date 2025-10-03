package com.pennycontrol.userservice.service.impl;

import com.pennycontrol.common.exception.BusinessException;
import com.pennycontrol.common.exception.ErrorCode;
import com.pennycontrol.common.exception.ResourceNotFoundException;
import com.pennycontrol.common.exception.ValidationException;
import com.pennycontrol.common.util.SecurityUtils;
import com.pennycontrol.userservice.dto.request.UpdateUserProfileRequest;
import com.pennycontrol.userservice.dto.response.UserResponse;
import com.pennycontrol.common.entity.User;
import com.pennycontrol.userservice.repository.UserRepository;
import com.pennycontrol.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile() {
        // Get current user ID from SecurityContext
        Long currentUserId = SecurityUtils.getCurrentUserId();

        log.info("Fetching profile for user ID: {}", currentUserId);

        // Find user by ID
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + currentUserId
                ));

        // Map to response DTO
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .phoneNumber(user.getPhoneNumber())
                .avatar(user.getAvatar())
                .currency(user.getCurrency())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(java.util.stream.Collectors.toSet()))
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public UserResponse updateUserProfile(UpdateUserProfileRequest request) {
        // Get current user ID from SecurityContext
        Long currentUserId = SecurityUtils.getCurrentUserId();

        log.info("Updating profile for user ID: {}", currentUserId);

        // Find user by ID
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + currentUserId
                ));

        // Check if phone number is being changed and if it's already in use by another user
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().equals(user.getPhoneNumber())) {
            userRepository.findByPhoneNumber(request.getPhoneNumber()).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(currentUserId)) {
                    throw new ValidationException("Phone number already in use");
                }
            });
        }

        // Update user fields (only update if not null)
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getCurrency() != null) {
            user.setCurrency(request.getCurrency());
        }
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }

        // Save updated user
        User updatedUser = userRepository.save(user);

        log.info("Successfully updated profile for user ID: {}", currentUserId);

        // Map to response DTO
        return UserResponse.builder()
                .id(updatedUser.getId())
                .email(updatedUser.getEmail())
                .name(updatedUser.getName())
                .phoneNumber(updatedUser.getPhoneNumber())
                .avatar(updatedUser.getAvatar())
                .currency(updatedUser.getCurrency())
                .roles(updatedUser.getRoles().stream()
                        .map(role -> role.getName())
                        .collect(java.util.stream.Collectors.toSet()))
                .emailVerified(updatedUser.getEmailVerified())
                .createdAt(updatedUser.getCreatedAt())
                .updatedAt(updatedUser.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public void deleteUserAccount() {
        // Get current user ID from SecurityContext
        Long currentUserId = SecurityUtils.getCurrentUserId();

        log.info("Deleting account for user ID: {}", currentUserId);

        // Find user by ID
        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + currentUserId
                ));

        // Perform soft delete by disabling the account
        user.setEnabled(false);
        user.setAccountLocked(true);
        userRepository.save(user);

        log.info("Successfully soft-deleted account for user ID: {}", currentUserId);
    }
}
