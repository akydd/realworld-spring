package com.akydd.realworld_spring.model;

public record ArticleSummaryView(
        ArticleSummary article,
        boolean favorited,
        boolean following
) {
}
