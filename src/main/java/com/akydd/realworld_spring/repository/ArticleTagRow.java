package com.akydd.realworld_spring.repository;

/** Projection for the batched tag lookup: (article id, tag name) pairs, grouped in the service. */
public interface ArticleTagRow {
    Long getArticleId();

    String getName();
}
