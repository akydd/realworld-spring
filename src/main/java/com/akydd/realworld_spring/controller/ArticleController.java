package com.akydd.realworld_spring.controller;

import com.akydd.realworld_spring.dto.ArticleResponse;
import com.akydd.realworld_spring.dto.CreateArticleRequest;
import com.akydd.realworld_spring.dto.UpdateArticleRequest;
import com.akydd.realworld_spring.mapper.ArticleMapper;
import com.akydd.realworld_spring.model.Article;
import com.akydd.realworld_spring.model.User;
import com.akydd.realworld_spring.service.ArticleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {
    private final ArticleService articleService;
    private final ArticleMapper articleMapper;

    public ArticleController(ArticleService articleService, ArticleMapper articleMapper) {
        this.articleService = articleService;
        this.articleMapper = articleMapper;
    }

    @PostMapping
    public ResponseEntity<ArticleResponse> save(@AuthenticationPrincipal User author, @Valid @RequestBody CreateArticleRequest article) {
        Article newArticle = articleService.create(author, articleMapper.toEntity(article), article.tagList());
        return ResponseEntity.status(HttpStatus.CREATED).body(articleMapper.toResponse(newArticle));
    }

    @PutMapping("{slug}")
    public ResponseEntity<ArticleResponse> update(@AuthenticationPrincipal User user, @PathVariable String slug, @Valid @RequestBody UpdateArticleRequest update) {
        Article updatedArticle = articleService.update(user, slug, articleMapper.toEntity(update));
        return ResponseEntity.ok(articleMapper.toResponse(updatedArticle));
    }
}
