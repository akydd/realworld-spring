package com.akydd.realworld_spring.controller;

import com.akydd.realworld_spring.dto.RegisterUserRequest;
import com.akydd.realworld_spring.dto.UserResponse;
import com.akydd.realworld_spring.mapper.UserMapper;
import com.akydd.realworld_spring.model.User;
import com.akydd.realworld_spring.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserMapper userMapper;
    private final UserService userService;

    public UserController(UserMapper userMapper, UserService userService) {
        this.userMapper = userMapper;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> registerUser(@Valid @NotNull @RequestBody RegisterUserRequest registerUserRequest) {
        User model = userMapper.toEntity(registerUserRequest);
        User userResponse = userService.registerUser(model);
        return new ResponseEntity<>(userMapper.toDTO(userResponse), HttpStatus.CREATED);
    }
}
