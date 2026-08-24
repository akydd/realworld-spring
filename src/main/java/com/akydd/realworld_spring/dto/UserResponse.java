package com.akydd.realworld_spring.dto;

import java.util.Optional;

public record UserResponse(
        String email,
        String token,
        String username,
        Optional<String> bio,
        Optional<String> image
) {
}
