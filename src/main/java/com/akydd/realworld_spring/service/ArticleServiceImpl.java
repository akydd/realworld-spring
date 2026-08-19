package com.akydd.realworld_spring.service;

import com.akydd.realworld_spring.model.Article;
import com.akydd.realworld_spring.model.User;
import com.akydd.realworld_spring.repository.ArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ArticleServiceImpl implements ArticleService {
    private final ArticleRepository articleRepository;

    public ArticleServiceImpl(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @Transactional
    public Article create(User author, Article article) {
        article.setAuthor(author);
        return articleRepository.save(article);
    }
}