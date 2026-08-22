package com.akydd.realworld_spring.dto;

import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.Optional;

@JsonRootName(value = "profile")
public record ProfileResponse(
        String username,
        Optional<String> bio,
        Optional<String> image,
        boolean following
) {
}
