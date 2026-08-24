package com.akydd.realworld_spring.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ArticleResponse(
        String slug,
        String title,
        String description,
        String body,
        List<String> tagList,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean favorited,
        int favoritesCount,
        ProfileResponse author
) {
}
