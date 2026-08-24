package com.akydd.realworld_spring.dto;

/** RealWorld wraps the user payload under a {@code "user"} root key. */
public record UserEnvelope(UserResponse user) {
}
