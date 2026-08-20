package com.akydd.realworld_spring.model;

import jakarta.persistence.*;

@Entity
@Table(name = "article_favorites")
public class ArticleFavorites {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id")
    private Article article;
}
