package com.akydd.realworld_spring.dto;

import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.Optional;

@JsonRootName(value = "user")
public record UserResponse(
        String email,
        String token,
        String username,
        Optional<String> bio,
        Optional<String> image
) {
}
