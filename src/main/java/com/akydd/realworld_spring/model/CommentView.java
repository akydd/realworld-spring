package com.akydd.realworld_spring.model;

public record CommentView(
        Comment comment,
        boolean following
) {
}
