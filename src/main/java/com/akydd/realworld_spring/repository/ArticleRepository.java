package com.akydd.realworld_spring.repository;

import com.akydd.realworld_spring.model.Article;
import com.akydd.realworld_spring.model.ArticleSummaryRow;
import org.springframework.data.domain.Pageable;
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

    /**
     * Rebuilds every article's cached {@code favoritesCount} from the {@code article_favorites}
     * source of truth — the counter-cache "reset_counters" equivalent, run to correct any drift.
     * Returns the number of articles updated.
     */
    @Modifying(clearAutomatically = true)
    @Query("update Article a set a.favoritesCount = (select count(f) from ArticleFavorites f where f.article.id = a.id)")
    int reconcileFavoritesCounts();

    @Query("select count(f) > 0 from ArticleFavorites f where f.article.id = :articleId and f.user.id = :userId")
    boolean isFavorited(@Param("articleId") Long articleId, @Param("userId") Long userId);

    // --- List with optional filters (author / tag / favorited-by), viewer-relative flags computed ---

    @Query("""
            select new com.akydd.realworld_spring.model.ArticleSummaryRow(
                a.id, a.slug, a.title, a.description, a.createdAt, a.updatedAt, a.favoritesCount,
                a.author.username, a.author.bio, a.author.image,
                (case when exists (select 1 from ArticleFavorites fv where fv.article.id = a.id and fv.user.id = :viewerId) then true else false end),
                (case when exists (select 1 from Follows fo where fo.following.id = a.author.id and fo.follower.id = :viewerId) then true else false end))
            from Article a
            where (:author is null or a.author.username = :author)
              and (:tag is null or exists (select 1 from a.tags t where t.name = :tag))
              and (:favorited is null or exists (select 1 from ArticleFavorites ff where ff.article.id = a.id and ff.user.username = :favorited))
            order by a.createdAt desc
            """)
    List<ArticleSummaryRow> searchArticles(@Param("viewerId") Long viewerId,
                                           @Param("author") String author,
                                           @Param("tag") String tag,
                                           @Param("favorited") String favorited,
                                           Pageable pageable);

    @Query("""
            select count(a) from Article a
            where (:author is null or a.author.username = :author)
              and (:tag is null or exists (select 1 from a.tags t where t.name = :tag))
              and (:favorited is null or exists (select 1 from ArticleFavorites ff where ff.article.id = a.id and ff.user.username = :favorited))
            """)
    long countArticles(@Param("author") String author, @Param("tag") String tag, @Param("favorited") String favorited);

    // --- Feed: articles by followed authors (following is always true here) ---

    @Query("""
            select new com.akydd.realworld_spring.model.ArticleSummaryRow(
                a.id, a.slug, a.title, a.description, a.createdAt, a.updatedAt, a.favoritesCount,
                a.author.username, a.author.bio, a.author.image,
                (case when exists (select 1 from ArticleFavorites fv where fv.article.id = a.id and fv.user.id = :viewerId) then true else false end),
                true)
            from Article a
            where exists (select 1 from Follows fo where fo.following.id = a.author.id and fo.follower.id = :viewerId)
            order by a.createdAt desc
            """)
    List<ArticleSummaryRow> feedArticles(@Param("viewerId") Long viewerId, Pageable pageable);

    @Query("select count(a) from Article a where exists (select 1 from Follows fo where fo.following.id = a.author.id and fo.follower.id = :viewerId)")
    long countFeed(@Param("viewerId") Long viewerId);

    // --- Batched tag names for a page of article ids ---

    @Query("select a.id as articleId, t.name as name from Article a join a.tags t where a.id in :ids")
    List<ArticleTagRow> tagRows(@Param("ids") List<Long> ids);
}