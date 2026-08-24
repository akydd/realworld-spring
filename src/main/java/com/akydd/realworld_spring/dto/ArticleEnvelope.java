package com.akydd.realworld_spring.dto;

/** RealWorld wraps the article payload under an {@code "article"} root key. */
public record ArticleEnvelope(ArticleResponse article) {
}
