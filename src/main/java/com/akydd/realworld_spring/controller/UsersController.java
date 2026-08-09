package com.akydd.realworld_spring.controller;

import com.akydd.realworld_spring.dto.LoginUserRequest;
import com.akydd.realworld_spring.dto.RegisterUserRequest;
import com.akydd.realworld_spring.dto.UserResponse;
import com.akydd.realworld_spring.mapper.UserMapper;
import com.akydd.realworld_spring.model.User;
import com.akydd.realworld_spring.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UsersController {

    private final UserMapper userMapper;
    private final UserService userService;

    public UsersController(UserMapper userMapper, UserService userService) {
        this.userMapper = userMapper;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody RegisterUserRequest registerUserRequest) {
        User model = userMapper.toEntity(registerUserRequest);
        User userResponse = userService.registerUser(model);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userMapper.toDTO(userResponse));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> loginUser(@Valid @RequestBody LoginUserRequest loginUserRequest) {
        User user = userService.loginUser(loginUserRequest.email(), loginUserRequest.password());
        return ResponseEntity.ok(userMapper.toDTO(user));
    }
}
