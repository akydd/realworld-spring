package com.akydd.realworld_spring.service;

import com.akydd.realworld_spring.model.*;
import com.akydd.realworld_spring.repository.ArticleRepository;
import com.akydd.realworld_spring.repository.CommentRepository;
import com.akydd.realworld_spring.repository.TagRepository;
import com.akydd.realworld_spring.repository.UserRepository;
import com.akydd.realworld_spring.util.Slugs;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl implements ArticleService {
    private final ArticleRepository articleRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    public ArticleServiceImpl(ArticleRepository articleRepository, TagRepository tagRepository, UserRepository userRepository, CommentRepository commentRepository) {
        this.articleRepository = articleRepository;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional
    public ArticleView create(User author, Article article, List<String> tagNames) {
        article.setAuthor(author);

        // Tags must be handled separately from the rest of the Article model.
        // This handles saving new tags, not creating duplicate tags,
        // and creating an object that Hibernate can use to link the article
        // to those tags.
        Set<Tag> tags = tagNames.stream()
                .map(String::trim)
                .filter(n -> !n.isBlank())
                .distinct()
                .map(name -> tagRepository.findByName(name)
                        .orElseGet(() -> tagRepository.save(new Tag(name))))
                .collect(Collectors.toCollection(HashSet::new));

        // Link tags to the Article.
        article.setTags(tags);

        // Save the article with the tag links.
        Article newArticle = articleRepository.save(article);
        return new ArticleView(newArticle, false);
    }

    @Transactional
    public ArticleView update(User user, String slug, UpdateArticle updateArticle) {
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

        Article updatedArticle =  articleRepository.save(toUpdate);
        return new ArticleView(updatedArticle, articleRepository.isFavorited(updatedArticle.getId(), user.getId()));
    }

    @Transactional
    public ArticleView favorite(User user, String slug) {
        User me = userRepository.getReferenceById(user.getId());
        Article article = articleRepository.findBySlug(slug).orElseThrow();

        if (!me.getFavorites().contains(article)) {
            me.addFavorite(article);
            userRepository.save(me);
            articleRepository.increaseFavoriteCount(article.getId());
        }

        Article updatedArticle =  articleRepository.findById(article.getId()).orElseThrow();
        return new ArticleView(updatedArticle, true);
    }

    @Transactional
    public ArticleView unfavorite(User user, String slug) {
        User me = userRepository.getReferenceById(user.getId());
        Article article = articleRepository.findBySlug(slug).orElseThrow();

        if (me.getFavorites().contains(article)) {
            me.removeFavorite(article);
            userRepository.save(me);
            articleRepository.decreaseFavoriteCount(article.getId());
        }

        Article updatedArticle = articleRepository.findById(article.getId()).orElseThrow();
        return new ArticleView(updatedArticle, false);
    }

    public void delete(User user, String slug) {
        Article toDelete = articleRepository.findBySlugAndAuthorId(slug, user.getId()).orElseThrow();
        articleRepository.delete(toDelete);
    }

    public ArticleView getBySlug(@Nullable User user, String slug) {
        Article article = articleRepository.findBySlug(slug).orElseThrow();
        return new ArticleView(article, user != null && articleRepository.isFavorited(article.getId(), user.getId()));
    }

    @Transactional
    public CommentView addComment(User user, String slug, Comment comment) {
        Article article = articleRepository.findBySlug(slug).orElseThrow();
        comment.setArticle(article);
        comment.setAuthor(user);

        Comment savedComment = commentRepository.save(comment);
        return new CommentView(savedComment, true);
    }
}