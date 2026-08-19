package com.akydd.realworld_spring.service;

import com.akydd.realworld_spring.model.Article;
import com.akydd.realworld_spring.model.UpdateArticle;
import com.akydd.realworld_spring.model.User;

import java.util.List;

public interface ArticleService {
    Article create(User author, Article article, List<String> tagNames);
    Article update(User user, String slug, UpdateArticle update);
}
