package com.akydd.realworld_spring.repository;

import com.akydd.realworld_spring.model.ArticleFavorites;
import com.akydd.realworld_spring.model.ArticleFavoritesId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleFavoritesRepository extends JpaRepository<ArticleFavorites, ArticleFavoritesId> {
}
