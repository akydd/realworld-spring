package com.akydd.realworld_spring.dto;

import java.util.List;

/** Serializes to {@code {"tags":[...]}} — the record component name is the root key. */
public record TagsResponse(
        List<String> tags
) {
}
