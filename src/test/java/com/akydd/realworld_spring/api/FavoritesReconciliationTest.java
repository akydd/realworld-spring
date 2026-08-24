package com.akydd.realworld_spring.api;

import com.akydd.realworld_spring.model.Article;
import com.akydd.realworld_spring.repository.ArticleRepository;
import com.akydd.realworld_spring.service.ArticleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The counter-cache reconciliation ("reset_counters" equivalent): a drifted favoritesCount is
 * rebuilt from the article_favorites source of truth.
 */
class FavoritesReconciliationTest extends ApiTestSupport {

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    ArticleService articleService;

    @Test
    void reconcileRebuildsDriftedFavoritesCount() {
        String uid = uid();
        String author = register("recon-author-" + uid, "recon-author-" + uid + "@example.com", "password123");
        String slug = createArticle(author, "Reconcile " + uid, "desc", "body");

        String fan = register("recon-fan-" + uid, "recon-fan-" + uid + "@example.com", "password123");
        expect(post("/api/articles/" + slug + "/favorite", null, fan), HttpStatus.OK, "favorite article");

        Article article = articleRepository.findBySlug(slug).orElseThrow();
        assertThat(article.getFavoritesCount()).as("favorite bumped the counter").isEqualTo(1);

        // Simulate drift by corrupting the cached counter directly.
        article.setFavoritesCount(999);
        articleRepository.save(article);
        assertThat(articleRepository.findBySlug(slug).orElseThrow().getFavoritesCount())
                .as("counter is now drifted").isEqualTo(999);

        // Reconcile rebuilds it from article_favorites (the source of truth).
        articleService.reconcileFavoritesCounts();

        assertThat(articleRepository.findBySlug(slug).orElseThrow().getFavoritesCount())
                .as("reconcile restored the true count").isEqualTo(1);
    }
}
