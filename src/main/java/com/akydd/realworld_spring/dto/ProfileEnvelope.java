package com.akydd.realworld_spring.dto;

/** RealWorld wraps the profile payload under a {@code "profile"} root key. */
public record ProfileEnvelope(ProfileResponse profile) {
}
