package com.akydd.realworld_spring.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;

@JsonRootName(value = "articles")
public record ArticlesResponse(
        @JsonValue List<ArticleSummaryResponse> articles,
        int articlesCount
) {
}

