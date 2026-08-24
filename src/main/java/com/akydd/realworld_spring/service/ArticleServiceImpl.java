package com.akydd.realworld_spring.service;

import com.akydd.realworld_spring.dto.ArticleSummaryResponse;
import com.akydd.realworld_spring.dto.ArticlesResponse;
import com.akydd.realworld_spring.dto.ProfileResponse;
import com.akydd.realworld_spring.exception.ForbiddenException;
import com.akydd.realworld_spring.exception.NotFoundException;
import com.akydd.realworld_spring.model.*;
import com.akydd.realworld_spring.repository.*;
import com.akydd.realworld_spring.util.OffsetPageable;
import com.akydd.realworld_spring.util.Slugs;
import jakarta.annotation.Nullable;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ArticleServiceImpl implements ArticleService {
    // Retry ceiling for slug collisions (a collision at all is already rare).
    private static final int MAX_SLUG_ATTEMPTS = 20;
    private final ArticleRepository articleRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;
    private final ArticleFavoritesRepository articleFavoritesRepository;
    private final FollowsRepository followsRepository;
    // Self-reference so createOnce() is called THROUGH the Spring proxy: each retry attempt must run
    // in its own transaction. A unique-constraint violation marks the current transaction
    // rollback-only and poisons the persistence context, so we cannot catch it and re-save inside the
    // same @Transactional method. @Lazy breaks the constructor self-dependency cycle.
    private final ArticleServiceImpl self;

    public ArticleServiceImpl(ArticleRepository articleRepository, TagRepository tagRepository, UserRepository userRepository, CommentRepository commentRepository, ArticleFavoritesRepository articleFavoritesRepository, FollowsRepository followsRepository, @Lazy ArticleServiceImpl self) {
        this.articleRepository = articleRepository;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
        this.articleFavoritesRepository = articleFavoritesRepository;
        this.followsRepository = followsRepository;
        this.self = self;
    }

    /**
     * True when the failure is the articles.slug UNIQUE violation (Postgres names it articles_slug_key).
     */
    private static boolean isSlugConflict(DataIntegrityViolationException e) {
        return e.getCause() instanceof ConstraintViolationException cve
                && cve.getConstraintName() != null
                && cve.getConstraintName().toLowerCase().contains("slug");
    }

    // NOT @Transactional: this owns the retry loop, so each attempt below gets a fresh transaction.
    // The articles.slug UNIQUE constraint is the source of truth; on a losing race we bump the slug
    // suffix and try again rather than pre-checking (which would race anyway).
    public ArticleView create(User author, Article article, List<String> tagNames) {
        String baseSlug = article.getSlug();
        for (int attempt = 0; ; attempt++) {
            article.setSlug(attempt == 0 ? baseSlug : baseSlug + "-" + attempt);
            try {
                return self.createOnce(author, article, tagNames);
            } catch (DataIntegrityViolationException e) {
                if (attempt >= MAX_SLUG_ATTEMPTS || !isSlugConflict(e)) {
                    throw e;
                }
                // slug already taken: bump the suffix and retry in a new transaction
            }
        }
    }

    @Transactional
    public ArticleView createOnce(User author, Article article, List<String> tagNames) {
        article.setAuthor(author);

        // Tags must be handled separately from the rest of the Article model.
        // This handles saving new tags, not creating duplicate tags,
        // and creating an object that Hibernate can use to link the article
        // to those tags.
        // Preserve request order (LinkedHashSet) and tolerate an absent tagList (null). Rebuilt on
        // every attempt so a rolled-back attempt never reuses stale (detached) tag entities.
        Set<Tag> tags = (tagNames == null ? List.<String>of() : tagNames).stream()
                .map(String::trim)
                .filter(n -> !n.isBlank())
                .distinct()
                .map(name -> tagRepository.findByName(name)
                        .orElseGet(() -> tagRepository.save(new Tag(name))))
                .collect(Collectors.toCollection(LinkedHashSet::new));

        // Link tags to the Article.
        article.setTags(tags);

        // Save the article with the tag links. The id is IDENTITY-generated, so a slug collision
        // fails this INSERT and leaves the entity transient (id null) — safe to retry with a new slug.
        Article newArticle = articleRepository.save(article);
        return new ArticleView(newArticle, false, false);
    }

    @Transactional
    public ArticleView update(User user, String slug, UpdateArticle updateArticle) {
        Article toUpdate = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("article"));
        if (!toUpdate.getAuthor().getId().equals(user.getId())) {
            throw new ForbiddenException("article");
        }

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
                articleFavoritesRepository.existsById(new ArticleFavoritesId(user.getId(), updatedArticle.getId())),
                false
        );
    }

    @Transactional
    public ArticleView favorite(User user, String slug) {
        User me = userRepository.getReferenceById(user.getId());
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("article"));

        ArticleFavoritesId id = new ArticleFavoritesId(me.getId(), article.getId());
        if (!articleFavoritesRepository.existsById(id)) {
            articleFavoritesRepository.save(new ArticleFavorites(me, article));
            articleRepository.increaseFavoriteCount(article.getId());
        }

        Article updatedArticle = articleRepository.findById(article.getId())
                .orElseThrow(() -> new IllegalStateException("article " + article.getId() + " disappeared mid-transaction"));
        return new ArticleView(updatedArticle, true,
                followsRepository.existsById(new FollowsId(user.getId(), updatedArticle.getAuthor().getId())));
    }

    @Transactional
    public ArticleView unfavorite(User user, String slug) {
        User me = userRepository.getReferenceById(user.getId());
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("article"));

        ArticleFavoritesId id = new ArticleFavoritesId(me.getId(), article.getId());
        if (articleFavoritesRepository.existsById(id)) {
            articleFavoritesRepository.deleteById(id);
            articleRepository.decreaseFavoriteCount(article.getId());
        }

        Article updatedArticle = articleRepository.findById(article.getId())
                .orElseThrow(() -> new IllegalStateException("article " + article.getId() + " disappeared mid-transaction"));
        return new ArticleView(updatedArticle, false,
                followsRepository.existsById(new FollowsId(user.getId(), updatedArticle.getAuthor().getId())));
    }

    @Transactional
    public int reconcileFavoritesCounts() {
        return articleRepository.reconcileFavoritesCounts();
    }

    public void delete(User user, String slug) {
        Article toDelete = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("article"));
        if (!toDelete.getAuthor().getId().equals(user.getId())) {
            throw new ForbiddenException("article");
        }
        articleRepository.delete(toDelete);
    }

    public ArticleView getBySlug(@Nullable User user, String slug) {
        Article article = articleRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("article"));
        return new ArticleView(article,
                user != null && articleFavoritesRepository.existsById(new ArticleFavoritesId(user.getId(), article.getId())),
                user != null && followsRepository.existsById(new FollowsId(user.getId(), article.getAuthor().getId())));
    }

    @Transactional
    public CommentView addComment(User user, String slug, Comment comment) {
        Article article = articleRepository.findBySlug(slug).orElseThrow(() -> new NotFoundException("article"));
        comment.setArticle(article);
        comment.setAuthor(user);

        Comment savedComment = commentRepository.save(comment);
        return new CommentView(savedComment, true);
    }

    public void deleteComment(User user, String slug, Long commentId) {
        Article article = articleRepository.findBySlug(slug).orElseThrow(() -> new NotFoundException("article"));
        Comment comment = commentRepository.findById(commentId).orElseThrow(() -> new NotFoundException("comment"));

        if (!comment.getArticle().getId().equals(article.getId())) {
            throw new RuntimeException("nope");
        }

        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new ForbiddenException("comment");
        }

        commentRepository.delete(comment);
    }

    public List<CommentView> getComments(User user, String slug) {
        Article article = articleRepository.findBySlug(slug).orElseThrow(() -> new NotFoundException("article"));
        List<Comment> comments = commentRepository.findByArticleIdOrderByCreatedAtAsc(article.getId());

        return comments.stream()
                .map(comment -> new CommentView(comment, user != null && followsRepository.existsById(new FollowsId(user.getId(), comment.getAuthor().getId()))))
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

    /**
     * Stitch the batched tag names onto the projected rows and build the response DTO.
     */
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