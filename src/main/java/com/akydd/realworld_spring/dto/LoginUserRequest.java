package com.akydd.realworld_spring.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.NotBlank;

@JsonRootName(value="user")
public record LoginUserRequest(
        @NotBlank(message = "must not be blank")
        String email,
        @NotBlank(message = "must not be blank")
        String password
) {}
