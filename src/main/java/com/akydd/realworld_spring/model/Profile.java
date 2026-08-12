package com.akydd.realworld_spring.model;

import java.util.Optional;

public record Profile(
        String username,
        Optional<String> bio,
        Optional<String> image,
        Boolean following
) {
}
