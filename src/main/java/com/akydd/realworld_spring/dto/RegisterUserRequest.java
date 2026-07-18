package com.akydd.realworld_spring.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonRootName(value = "user")
public record RegisterUserRequest(
        @NotBlank(message = "username cannot be empty")
    String username,

    @Email(message = "provide a valid email")
    String email,

    @Size(min = 8, message = "get it right dude")
    String password
) {}
