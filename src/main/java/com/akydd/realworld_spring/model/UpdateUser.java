package com.akydd.realworld_spring.model;

import com.akydd.realworld_spring.json.Tristate;

public record UpdateUser(
        String email,
        String username,
        String password,
        Tristate<String> bio,
        Tristate<String> image
) {
}
