package com.akydd.realworld_spring.model;

import java.time.LocalDateTime;

/**
 * Flat JPQL constructor-projection row for the article list/feed. {@code following} and
 * {@code favorited} are computed per-request from the viewer id; tags are stitched in separately
 * (a to-many collection can't be flattened into one projection row). {@code id} is kept only as the
 * correlation key for that tag batch and is not exposed in the response.
 */
public record ArticleSummaryRow(
        Long id,
        String slug,
        String title,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        int favoritesCount,
        String authorUsername,
        String authorBio,
        String authorImage,
        Boolean favorited,
        Boolean following
) {
}
