package com.akydd.realworld_spring.controllers;

import com.akydd.realworld_spring.dto.RegisterUserRequest;
import com.akydd.realworld_spring.dto.UserResponse;
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

    @PostMapping
    public ResponseEntity<UserResponse> registerUser(@Valid @NotNull @RequestBody RegisterUserRequest registerUserRequest) {
        UserResponse user = new UserResponse(registerUserRequest.email(), "token", registerUserRequest.username(), null, null);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }
}
