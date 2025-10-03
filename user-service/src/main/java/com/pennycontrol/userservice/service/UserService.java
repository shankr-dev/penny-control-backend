package com.pennycontrol.userservice.service;

import com.pennycontrol.userservice.dto.request.UpdateUserProfileRequest;
import com.pennycontrol.userservice.dto.response.UserResponse;

public interface UserService {

    /**
     * Get current authenticated user's profile
     *
     * @return UserResponse with current user information
     */
    UserResponse getCurrentUserProfile();

    /**
     * Update current authenticated user's profile
     *
     * @param request UpdateUserProfileRequest with updated user information
     * @return UserResponse with updated user information
     */
    UserResponse updateUserProfile(UpdateUserProfileRequest request);

    /**
     * Delete current authenticated user's account
     */
    void deleteUserAccount();
}
