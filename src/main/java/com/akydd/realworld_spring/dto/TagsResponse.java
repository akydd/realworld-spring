package com.akydd.realworld_spring.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.List;

/**
 * Serializes to {@code {"tags":[...]}} under the app's global WRAP_ROOT_VALUE:
 * {@code @JsonValue} makes the record serialize as the bare array, and the root wrap adds the
 * {@code "tags"} key (named by {@code @JsonRootName}). Without {@code @JsonValue} the wrap would
 * double it to {@code {"tags":{"tags":[...]}}}.
 */
@JsonRootName("tags")
public record TagsResponse(
        @JsonValue List<String> tags
) {
}
