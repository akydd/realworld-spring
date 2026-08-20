package com.akydd.realworld_spring.model;

public record ArticleView(
        Article article,
        boolean favorited
) {
}
