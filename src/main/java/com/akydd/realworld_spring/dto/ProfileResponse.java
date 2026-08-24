package com.akydd.realworld_spring.dto;

import java.util.Optional;

public record ProfileResponse(
        String username,
        Optional<String> bio,
        Optional<String> image,
        boolean following
) {
}
