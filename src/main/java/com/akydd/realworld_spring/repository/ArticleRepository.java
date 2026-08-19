package com.akydd.realworld_spring.repository;

import com.akydd.realworld_spring.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    Optional<Article> findBySlugAndAuthorId(String slug, Long authorId);
}
