package com.akydd.realworld_spring.dto;

import com.fasterxml.jackson.annotation.JsonRootName;

import java.time.LocalDateTime;

@JsonRootName(value = "comment")
public record CommentResponse(
        Long id,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        String body,
        ProfileResponse author
) {
}
