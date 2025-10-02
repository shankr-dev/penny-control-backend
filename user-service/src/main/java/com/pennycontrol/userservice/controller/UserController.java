package com.pennycontrol.userservice.controller;

import com.pennycontrol.common.dto.ApiResponse;
import com.pennycontrol.userservice.dto.response.UserResponse;
import com.pennycontrol.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
