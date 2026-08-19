package com.akydd.realworld_spring.dto;

import com.fasterxml.jackson.annotation.JsonRootName;

import java.time.LocalDateTime;

@JsonRootName(value = "article")
public record ArticleResponse(
        String slug,
        String title,
        String description,
        String body,
        String[] tagList,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean favorited,
        int favoritesCount,
        ProfileResponse author
) {
}
