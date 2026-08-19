package com.akydd.realworld_spring.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.AssertTrue;

@JsonRootName(value = "article")
public record UpdateArticleRequest(
        String title,
        String description,
        String body
) {
    @AssertTrue(message = "Something's gotta be in here")
    public boolean isAtLeastOneFieldNotEmpty() {
        return (title != null && !title.isEmpty()) ||
                (description != null && !description.isEmpty()) ||
                (body != null && !body.isEmpty());
    }
}
