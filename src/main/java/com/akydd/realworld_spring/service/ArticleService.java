package com.akydd.realworld_spring.service;

import com.akydd.realworld_spring.model.Article;
import com.akydd.realworld_spring.model.User;

public interface ArticleService {
    Article create(User author, Article article);
}
