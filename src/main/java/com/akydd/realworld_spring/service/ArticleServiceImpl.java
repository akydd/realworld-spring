package com.akydd.realworld_spring.service;

import com.akydd.realworld_spring.model.Article;
import com.akydd.realworld_spring.model.UpdateArticle;
import com.akydd.realworld_spring.model.User;
import com.akydd.realworld_spring.repository.ArticleRepository;
import com.akydd.realworld_spring.util.Slugs;
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

    @Transactional
    public Article update(User user, String slug, UpdateArticle updateArticle) {
        // This makes sure that 1: the article exists, and 2: the article is authored by the caller.
        Article toUpdate = articleRepository.findBySlugAndAuthorId(slug, user.getId()).orElseThrow();

        // When the title changes, the slug must also change.
        if (updateArticle.title() != null && !updateArticle.title().isEmpty()) {
            toUpdate.setTitle(updateArticle.title());
            // TODO: handle the case when the new slug is not unique!
            toUpdate.setSlug(Slugs.slugify(updateArticle.title()));
        }

        if (updateArticle.description() != null && !updateArticle.description().isEmpty()) {
            toUpdate.setDescription(updateArticle.description());
        }

        if (updateArticle.body() != null && !updateArticle.body().isEmpty()) {
            toUpdate.setBody(updateArticle.body());
        }

       return articleRepository.save(toUpdate);
    }
}