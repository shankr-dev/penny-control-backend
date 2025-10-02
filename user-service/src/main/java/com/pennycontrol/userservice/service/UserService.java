package com.pennycontrol.userservice.service;

import com.pennycontrol.userservice.dto.response.UserResponse;

public interface UserService {

    /**
     * Get current authenticated user's profile
     *
     * @return UserResponse with current user information
     */
    UserResponse getCurrentUserProfile();
}
