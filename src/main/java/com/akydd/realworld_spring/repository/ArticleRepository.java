package com.akydd.realworld_spring.repository;

import com.akydd.realworld_spring.model.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {
}
