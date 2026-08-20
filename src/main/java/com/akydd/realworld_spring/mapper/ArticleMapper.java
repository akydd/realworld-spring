package com.akydd.realworld_spring.mapper;

import com.akydd.realworld_spring.dto.ArticleResponse;
import com.akydd.realworld_spring.dto.CreateArticleRequest;
import com.akydd.realworld_spring.dto.ProfileResponse;
import com.akydd.realworld_spring.dto.UpdateArticleRequest;
import com.akydd.realworld_spring.model.*;
import com.akydd.realworld_spring.util.Slugs;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface ArticleMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "slug", source = "title", qualifiedByName = "strToSlug")
    @Mapping(target = "tags", ignore = true) // Tags are added manually by the ArticleService.
    @Mapping(target = "favoritesCount", ignore = true) // defaults to 0; maintained by the counter.
    Article toEntity(CreateArticleRequest request);

    @Named("strToSlug")
    default String strToSlug(String str) {
        return Slugs.slugify(str);
    }

    default ArticleResponse toResponse(ArticleView view) {
        return toResponse(view.article(), view.favorited());
    }

    @Mapping(source = "article.tags", target = "tagList")
    ArticleResponse toResponse(Article article, boolean favorited);

    /**
     * Need this to map the {@code Set<tag>} of the Article to
     * a {@code List<String>} for the ArticleResponse.
     *
     * @param tags a {@code Set<Tag>}
     * @return a {@code List<String>}
     */
    default List<String> tagsToNames(Set<Tag> tags) {
        if (tags == null) {
            return new ArrayList<>();
        }

        return tags.stream().map(Tag::getName).toList();
    }

    @Mapping(source = "realUsername", target = "username")
    @Mapping(target = "following", constant = "false")
    ProfileResponse toProfileResponse(User author);

    UpdateArticle toEntity(UpdateArticleRequest request);
}
