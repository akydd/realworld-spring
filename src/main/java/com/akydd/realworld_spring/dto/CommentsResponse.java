package com.akydd.realworld_spring.dto;

import java.util.List;

/** Serializes to {@code {"comments":[...]}} — the record component name is the root key. */
public record CommentsResponse(
        List<CommentResponse> comments
) {
}
