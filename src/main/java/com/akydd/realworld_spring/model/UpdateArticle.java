package com.akydd.realworld_spring.model;

import com.akydd.realworld_spring.json.Tristate;

import java.util.List;

public record UpdateArticle(
        String title,
        String description,
        String body,
        Tristate<List<String>> tagList
) {
}
