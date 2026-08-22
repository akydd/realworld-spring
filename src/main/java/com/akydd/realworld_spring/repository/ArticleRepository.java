package com.akydd.realworld_spring.repository;

import com.akydd.realworld_spring.model.Article;
import com.akydd.realworld_spring.model.ArticleSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    Optional<Article> findBySlugAndAuthorId(String slug, Long authorId);
    Optional<Article> findBySlug(String slug);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Article a set a.favoritesCount = a.favoritesCount + 1 where a.id = :id")
    void increaseFavoriteCount(@Param("id") Long articleId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Article a set a.favoritesCount = a.favoritesCount - 1 where a.id = :id")
    void decreaseFavoriteCount(@Param("id") Long articleId);

    @Query("select count(f) > 0 from ArticleFavorites f where f.article.id = :articleId and f.user.id = :userId")
    boolean isFavorited(@Param("articleId") Long articleId, @Param("userId") Long userId);

    List<ArticleSummary> findAllBy();
}