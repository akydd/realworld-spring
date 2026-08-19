package com.akydd.realworld_spring.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.NotBlank;

@JsonRootName(value = "article")
public record CreateArticleRequest(
        @NotBlank
        String title,
        @NotBlank
        String description,
        @NotBlank
        String body,
        String[] tagList
) {
}
