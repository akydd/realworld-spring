package com.akydd.realworld_spring.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@JsonRootName(value = "article")
public record CreateArticleRequest(
        @NotBlank
        String title,
        @NotBlank
        String description,
        @NotBlank
        String body,
        List<String> tagList
) {
}
