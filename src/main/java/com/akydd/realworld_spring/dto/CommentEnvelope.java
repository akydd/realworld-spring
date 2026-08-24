package com.akydd.realworld_spring.dto;

/** RealWorld wraps a single comment payload under a {@code "comment"} root key. */
public record CommentEnvelope(CommentResponse comment) {
}
