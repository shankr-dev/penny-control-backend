package com.pennycontrol.userservice.service.impl;

import com.pennycontrol.common.exception.ErrorCode;
import com.pennycontrol.common.exception.ResourceNotFoundException;
import com.pennycontrol.common.util.SecurityUtils;
import com.pennycontrol.userservice.dto.response.UserResponse;
import com.pennycontrol.userservice.entity.User;
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
                .currency(user.getCurrency())
                .roles(user.getRoles())
                .emailVerified(user.getEmailVerified())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
