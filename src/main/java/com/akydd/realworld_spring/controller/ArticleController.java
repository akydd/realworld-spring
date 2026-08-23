package com.akydd.realworld_spring.controller;

import com.akydd.realworld_spring.dto.*;
import com.akydd.realworld_spring.mapper.ArticleMapper;
import com.akydd.realworld_spring.mapper.ArticleSummaryMapper;
import com.akydd.realworld_spring.mapper.CommentMapper;
import com.akydd.realworld_spring.model.ArticleSummaryView;
import com.akydd.realworld_spring.model.ArticleView;
import com.akydd.realworld_spring.model.CommentView;
import com.akydd.realworld_spring.model.User;
import com.akydd.realworld_spring.service.ArticleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {
    private final ArticleService articleService;
    private final ArticleMapper articleMapper;
    private final CommentMapper commentMapper;
    private final ArticleSummaryMapper articleSummaryMapper;

    public ArticleController(ArticleService articleService, ArticleMapper articleMapper, CommentMapper commentMapper, ArticleSummaryMapper articleSummaryMapper) {
        this.articleService = articleService;
        this.articleMapper = articleMapper;
        this.commentMapper = commentMapper;
        this.articleSummaryMapper = articleSummaryMapper;
    }

    @PostMapping
    public ResponseEntity<ArticleResponse> save(@AuthenticationPrincipal User author, @Valid @RequestBody CreateArticleRequest article) {
        ArticleView newArticle = articleService.create(author, articleMapper.toEntity(article), article.tagList());
        return ResponseEntity.status(HttpStatus.CREATED).body(articleMapper.toResponse(newArticle));
    }

    @PutMapping("{slug}")
    public ResponseEntity<ArticleResponse> update(@AuthenticationPrincipal User user, @PathVariable String slug, @Valid @RequestBody UpdateArticleRequest update) {
        ArticleView updatedArticle = articleService.update(user, slug, articleMapper.toEntity(update));
        return ResponseEntity.ok(articleMapper.toResponse(updatedArticle));
    }

    @DeleteMapping("{slug}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal User user, @PathVariable String slug) {
        articleService.delete(user, slug);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("{slug}/favorite")
    public ResponseEntity<ArticleResponse> favorite(@AuthenticationPrincipal User user, @PathVariable String slug) {
        ArticleView favoriteArticle = articleService.favorite(user, slug);
        return ResponseEntity.ok(articleMapper.toResponse(favoriteArticle));
    }

    @DeleteMapping("{slug}/favorite")
    public ResponseEntity<ArticleResponse> unfavorite(@AuthenticationPrincipal User user, @PathVariable String slug) {
        ArticleView favoriteArticle = articleService.unfavorite(user, slug);
        return ResponseEntity.ok(articleMapper.toResponse(favoriteArticle));
    }

    @GetMapping("{slug}")
    public ResponseEntity<ArticleResponse> getArticle(@AuthenticationPrincipal User user, @PathVariable String slug) {
        ArticleView articleView = articleService.getBySlug(user, slug);
        return ResponseEntity.ok(articleMapper.toResponse(articleView));
    }

    @PostMapping("{slug}/comments")
    public ResponseEntity<CommentResponse> addComment(@AuthenticationPrincipal User user, @PathVariable String slug, @Valid @RequestBody CreateCommentRequest comment) {
        CommentView newComment = articleService.addComment(user, slug, commentMapper.toEntity(comment));
        return ResponseEntity.ok(commentMapper.toResponse(newComment));
    }

    @DeleteMapping("{slug}/comments/{id}")
    public ResponseEntity<Void> deleteComment(@AuthenticationPrincipal User user, @PathVariable String slug, @PathVariable Long id) {
        articleService.deleteComment(user, slug, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("{slug}/comments")
    public ResponseEntity<CommentsResponse> getComments(@AuthenticationPrincipal User user, @PathVariable String slug) {
        List<CommentView> comments = articleService.getComments(user, slug);
        return ResponseEntity.ok(commentMapper.toResponse(comments));
    }

    @GetMapping
    public ResponseEntity<ArticlesResponse> getArticles(@AuthenticationPrincipal User user,
                                                        @RequestParam(required = false) String tag,
                                                        @RequestParam(required = false) String author,
                                                        @RequestParam(required = false) String favorited,
                                                        @RequestParam(required = false) Integer limit,
                                                        @RequestParam(required = false) Integer offset) {
        List<ArticleSummaryView> articles = articleService.getAllArticles(user, tag, author, favorited, limit, offset);
        return ResponseEntity.ok(articleSummaryMapper.toResponse(articles));
    }
}
