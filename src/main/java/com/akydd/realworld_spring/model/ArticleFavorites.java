package com.akydd.realworld_spring.model;

import jakarta.persistence.*;

@Entity
@Table(name = "article_favorites")
public class ArticleFavorites {
    @EmbeddedId
    private ArticleFavoritesId id = new ArticleFavoritesId();
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("articleId")
    @JoinColumn(name = "article_id")
    private Article article;

    protected ArticleFavorites() {
    }

    public ArticleFavorites(User user, Article article) {
        this.id = new ArticleFavoritesId(user.getId(), article.getId());
        this.user = user;
        this.article = article;
    }

    public ArticleFavoritesId getId() {
        return id;
    }

    public void setId(ArticleFavoritesId id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Article getArticle() {
        return article;
    }

    public void setArticle(Article article) {
        this.article = article;
    }
}
