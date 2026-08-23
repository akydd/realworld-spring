package com.akydd.realworld_spring.dto;

import com.akydd.realworld_spring.json.Tristate;
import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.AssertTrue;

import java.util.List;

@JsonRootName(value = "article")
public record UpdateArticleRequest(
        String title,
        String description,
        String body,
        Tristate<List<String>> tagList
) {
    @AssertTrue(message = "Something's gotta be in here")
    public boolean isAtLeastOneFieldPresent() {
        return (title != null && !title.isEmpty()) ||
                (description != null && !description.isEmpty()) ||
                (body != null && !body.isEmpty()) ||
                (tagList != null && tagList.isPresent());
    }

    // tagList absent -> preserve; [] -> clear; [values] -> set; explicit null -> reject (422).
    @AssertTrue(message = "can't be null")
    public boolean isTagListNotNull() {
        return tagList == null || !tagList.isPresent() || tagList.get() != null;
    }
}
