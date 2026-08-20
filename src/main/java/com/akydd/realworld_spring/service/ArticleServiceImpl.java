package com.akydd.realworld_spring.service;

import com.akydd.realworld_spring.model.Article;
import com.akydd.realworld_spring.model.Tag;
import com.akydd.realworld_spring.model.UpdateArticle;
import com.akydd.realworld_spring.model.User;
import com.akydd.realworld_spring.repository.ArticleRepository;
import com.akydd.realworld_spring.repository.TagRepository;
import com.akydd.realworld_spring.repository.UserRepository;
import com.akydd.realworld_spring.util.Slugs;
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

    public ArticleServiceImpl(ArticleRepository articleRepository, TagRepository tagRepository, UserRepository userRepository) {
        this.articleRepository = articleRepository;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Article create(User author, Article article, List<String> tagNames) {
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
        return articleRepository.save(article);
    }

    @Transactional
    public Article update(User user, String slug, UpdateArticle updateArticle) {
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

        return articleRepository.save(toUpdate);
    }

    @Transactional
    public Article favorite(User user, String slug) {
        User me = userRepository.getReferenceById(user.getId());
        Article article = articleRepository.findBySlug(slug).orElseThrow();

        if (!me.getFavorites().contains(article)) {
            me.addFavorite(article);
            userRepository.save(me);
            articleRepository.increaseFavoriteCount(article.getId());
        }

        return articleRepository.findById(article.getId()).orElseThrow();
    }

    @Transactional
    public Article unfavorite(User user, String slug) {
        User me = userRepository.getReferenceById(user.getId());
        Article article = articleRepository.findBySlug(slug).orElseThrow();

        if (me.getFavorites().contains(article)) {
            me.removeFavorite(article);
            userRepository.save(me);
            articleRepository.decreaseFavoriteCount(article.getId());
        }

        return articleRepository.findById(article.getId()).orElseThrow();
    }
}