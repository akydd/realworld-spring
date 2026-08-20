package com.akydd.realworld_spring.service;

import com.akydd.realworld_spring.model.Article;
import com.akydd.realworld_spring.model.ArticleView;
import com.akydd.realworld_spring.model.UpdateArticle;
import com.akydd.realworld_spring.model.User;

import java.util.List;

public interface ArticleService {
    ArticleView create(User author, Article article, List<String> tagNames);
    ArticleView update(User user, String slug, UpdateArticle update);
    ArticleView favorite(User user, String slug);
    ArticleView unfavorite(User user, String slug);
    void delete(User user, String slug);
}
