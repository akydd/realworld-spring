package com.akydd.realworld_spring.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.NotBlank;

@JsonRootName(value = "user")
public record LoginUserRequest(
        @NotBlank
        String email,
        @NotBlank
        String password
) {
}
