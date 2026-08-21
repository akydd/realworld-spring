package com.akydd.realworld_spring.dto;


import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;

@JsonRootName(value = "comments")
public record CommentsResponse(
        @JsonValue List<CommentResponse> comments
) {
}
