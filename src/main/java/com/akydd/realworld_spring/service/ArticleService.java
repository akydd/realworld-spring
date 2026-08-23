package com.akydd.realworld_spring.service;

import com.akydd.realworld_spring.dto.ArticlesResponse;
import com.akydd.realworld_spring.model.*;
import jakarta.annotation.Nullable;

import java.util.List;

public interface ArticleService {
    ArticleView create(User author, Article article, List<String> tagNames);

    ArticleView update(User user, String slug, UpdateArticle update);

    ArticleView favorite(User user, String slug);

    ArticleView unfavorite(User user, String slug);

    void delete(User user, String slug);

    ArticleView getBySlug(@Nullable User user, String slug);

    CommentView addComment(User user, String slug, Comment comment);

    void deleteComment(User user, String slug, Long commentId);

    List<CommentView> getComments(User user, String slug);

    ArticlesResponse getAllArticles(@Nullable User user, String tag, String author, String favorited, Integer limit, Integer offset);

    ArticlesResponse getFeed(User user, Integer limit, Integer offset);
}
