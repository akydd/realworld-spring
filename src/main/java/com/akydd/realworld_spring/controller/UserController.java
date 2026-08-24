package com.akydd.realworld_spring.controller;

import com.akydd.realworld_spring.dto.UpdateUserRequest;
import com.akydd.realworld_spring.dto.UserResponse;
import com.akydd.realworld_spring.mapper.UserMapper;
import com.akydd.realworld_spring.mapper.UserUpdateMapper;
import com.akydd.realworld_spring.model.User;
import com.akydd.realworld_spring.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserMapper userMapper;
    private final UserUpdateMapper userUpdateMapper;
    private final UserService userService;

    public UserController(UserMapper userMapper, UserUpdateMapper userUpdateMapper, UserService userService) {
        this.userMapper = userMapper;
        this.userUpdateMapper = userUpdateMapper;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<UserResponse> currentUser(@AuthenticationPrincipal User principle) {
        return ResponseEntity.ok(userMapper.toDTO(principle));
    }

    @PutMapping
    public ResponseEntity<UserResponse> updateUser(@AuthenticationPrincipal User principle, @Valid @RequestBody UpdateUserRequest updateUserRequest) {
        User user = userService.updateUser(principle, userUpdateMapper.toEntity(updateUserRequest));
        return ResponseEntity.ok(userMapper.toDTO(user));
    }
}
