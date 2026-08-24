package com.akydd.realworld_spring.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ArticleFavoritesId implements Serializable {
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "article_id")
    private Long articleId;

    public ArticleFavoritesId() {
    }

    public ArticleFavoritesId(Long userId, Long articleId) {
        this.userId = userId;
        this.articleId = articleId;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ArticleFavoritesId that = (ArticleFavoritesId) o;
        return userId.equals(that.userId) && articleId.equals(that.articleId);
    }

    public int hashCode() {
        return Objects.hash(userId, articleId);
    }
}
