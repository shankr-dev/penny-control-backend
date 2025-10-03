package com.pennycontrol.userservice.controller;

import com.pennycontrol.common.dto.ApiResponse;
import com.pennycontrol.userservice.dto.request.UpdateUserProfileRequest;
import com.pennycontrol.userservice.dto.response.UserResponse;
import com.pennycontrol.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        log.info("Received request to get current user profile");

        UserResponse response = userService.getCurrentUserProfile();

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserProfile(
            @Valid @RequestBody UpdateUserProfileRequest request) {
        log.info("Received request to update current user profile");

        UserResponse response = userService.updateUserProfile(request);

        return ResponseEntity.ok(ApiResponse.success("User profile updated successfully", response));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteUserAccount() {
        log.info("Received request to delete current user account");

        userService.deleteUserAccount();

        return ResponseEntity.ok(ApiResponse.success("Account successfully deleted"));
    }
}
