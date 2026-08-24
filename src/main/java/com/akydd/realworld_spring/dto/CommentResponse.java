package com.akydd.realworld_spring.dto;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String body,
        ProfileResponse author
) {
}
