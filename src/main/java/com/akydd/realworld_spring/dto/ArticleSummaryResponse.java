package com.akydd.realworld_spring.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ArticleSummaryResponse(
        String slug,
        String title,
        String description,
        List<String> tagList,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean favorited,
        int favoritesCount,
        ProfileResponse author
) {
}
