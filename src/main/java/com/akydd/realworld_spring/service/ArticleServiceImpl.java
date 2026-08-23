package com.akydd.realworld_spring.service;

import com.akydd.realworld_spring.dto.ArticleSummaryResponse;
import com.akydd.realworld_spring.dto.ArticlesResponse;
import com.akydd.realworld_spring.dto.ProfileResponse;
import com.akydd.realworld_spring.exception.NotFoundException;
import com.akydd.realworld_spring.model.*;
import com.akydd.realworld_spring.repository.ArticleRepository;
import com.akydd.realworld_spring.repository.ArticleTagRow;
import com.akydd.realworld_spring.repository.CommentRepository;
import com.akydd.realworld_spring.repository.TagRepository;
import com.akydd.realworld_spring.repository.UserRepository;
import com.akydd.realworld_spring.util.OffsetPageable;
import com.akydd.realworld_spring.util.Slugs;
import jakarta.annotation.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        // Preserve request order (LinkedHashSet) and tolerate an absent tagList (null).
        Set<Tag> tags = (tagNames == null ? List.<String>of() : tagNames).stream()
                .map(String::trim)
                .filter(n -> !n.isBlank())
                .distinct()
                .map(name -> tagRepository.findByName(name)
                        .orElseGet(() -> tagRepository.save(new Tag(name))))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // Link tags to the Article.
        article.setTags(tags);

        // Save the article with the tag links.
        Article newArticle = articleRepository.save(article);
        return new ArticleView(newArticle, false, false);
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

        // tagList tri-state: absent -> preserve; present (including []) -> replace with resolved tags.
        if (updateArticle.tagList() != null && updateArticle.tagList().isPresent()) {
            List<String> names = updateArticle.tagList().get();
            Set<Tag> tags = (names == null ? List.<String>of() : names).stream()
                    .map(String::trim)
                    .filter(n -> !n.isBlank())
                    .distinct()
                    .map(name -> tagRepository.findByName(name)
                            .orElseGet(() -> tagRepository.save(new Tag(name))))
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            toUpdate.setTags(tags);
        }

        Article updatedArticle = articleRepository.save(toUpdate);
        return new ArticleView(updatedArticle,
                articleRepository.isFavorited(updatedArticle.getId(), user.getId()),
                false
        );
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

        Article updatedArticle = articleRepository.findById(article.getId()).orElseThrow();
        return new ArticleView(updatedArticle, true,
                userRepository.isFollowing(user.getId(), updatedArticle.getAuthor().getId()));
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
        return new ArticleView(updatedArticle, false,
                userRepository.isFollowing(user.getId(), updatedArticle.getAuthor().getId()));
    }

    public void delete(User user, String slug) {
        Article toDelete = articleRepository.findBySlugAndAuthorId(slug, user.getId()).orElseThrow();
        articleRepository.delete(toDelete);
    }

    public ArticleView getBySlug(@Nullable User user, String slug) {
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("article"));
        return new ArticleView(article,
                user != null && articleRepository.isFavorited(article.getId(), user.getId()),
                user != null && userRepository.isFollowing(user.getId(), article.getAuthor().getId()));
    }

    @Transactional
    public CommentView addComment(User user, String slug, Comment comment) {
        Article article = articleRepository.findBySlug(slug).orElseThrow();
        comment.setArticle(article);
        comment.setAuthor(user);

        Comment savedComment = commentRepository.save(comment);
        return new CommentView(savedComment, true);
    }

    public void deleteComment(User user, String slug, Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow();
        Article article = articleRepository.findBySlug(slug).orElseThrow();

        if (!comment.getArticle().getId().equals(article.getId())) {
            throw new RuntimeException("nope");
        }

        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new RuntimeException("nope");
        }

        commentRepository.delete(comment);
    }

    public List<CommentView> getComments(User user, String slug) {
        Article article = articleRepository.findBySlug(slug).orElseThrow();
        List<Comment> comments = commentRepository.findByArticleIdOrderByCreatedAtAsc(article.getId());

        return comments.stream()
                .map(comment -> new CommentView(comment, user != null && userRepository.isFollowing(user.getId(), comment.getAuthor().getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public ArticlesResponse getAllArticles(User user, String tag, String author, String favorited, Integer limit, Integer offset) {
        Long viewerId = (user != null) ? user.getId() : null;
        Pageable pageable = new OffsetPageable(offset != null ? offset : 0, limit != null ? limit : 20);
        List<ArticleSummaryRow> rows = articleRepository.searchArticles(viewerId, author, tag, favorited, pageable);
        long total = articleRepository.countArticles(author, tag, favorited);
        return assemble(rows, total);
    }

    @Transactional(readOnly = true)
    public ArticlesResponse getFeed(User user, Integer limit, Integer offset) {
        Pageable pageable = new OffsetPageable(offset != null ? offset : 0, limit != null ? limit : 20);
        List<ArticleSummaryRow> rows = articleRepository.feedArticles(user.getId(), pageable);
        long total = articleRepository.countFeed(user.getId());
        return assemble(rows, total);
    }

    /** Stitch the batched tag names onto the projected rows and build the response DTO. */
    private ArticlesResponse assemble(List<ArticleSummaryRow> rows, long total) {
        List<Long> ids = rows.stream().map(ArticleSummaryRow::id).toList();
        Map<Long, List<String>> tagsById = ids.isEmpty()
                ? Map.of()
                : articleRepository.tagRows(ids).stream()
                        .collect(Collectors.groupingBy(ArticleTagRow::getArticleId,
                                Collectors.mapping(ArticleTagRow::getName, Collectors.toList())));

        List<ArticleSummaryResponse> items = rows.stream()
                .map(r -> new ArticleSummaryResponse(
                        r.slug(), r.title(), r.description(),
                        tagsById.getOrDefault(r.id(), List.of()),
                        r.createdAt(), r.updatedAt(), r.favorited(), r.favoritesCount(),
                        new ProfileResponse(r.authorUsername(),
                                Optional.ofNullable(r.authorBio()),
                                Optional.ofNullable(r.authorImage()),
                                r.following())))
                .toList();

        return new ArticlesResponse(items, (int) total);
    }
}