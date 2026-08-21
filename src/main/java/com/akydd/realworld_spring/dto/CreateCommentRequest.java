package com.akydd.realworld_spring.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import jakarta.validation.constraints.NotBlank;

@JsonRootName(value = "comment")
public record CreateCommentRequest(
        @NotBlank
        String body
) {
}
