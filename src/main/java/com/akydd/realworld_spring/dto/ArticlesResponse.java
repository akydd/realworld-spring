package com.akydd.realworld_spring.dto;

import java.util.List;

// Plain record: serialized by the controller with WRAP_ROOT_VALUE disabled so it renders as the
// two-key {"articles":[...],"articlesCount":N} the spec requires.
public record ArticlesResponse(
        List<ArticleSummaryResponse> articles,
        int articlesCount
) {
}

