package com.akydd.realworld_spring.controller;

import com.akydd.realworld_spring.dto.UserResponse;
import com.akydd.realworld_spring.mapper.UserMapper;
import com.akydd.realworld_spring.model.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserMapper userMapper;

    public UserController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @GetMapping
    public ResponseEntity<UserResponse> currentUser(@AuthenticationPrincipal User principle) {
        return ResponseEntity.ok(userMapper.toDTO(principle));
    }
}
