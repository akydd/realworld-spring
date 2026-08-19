package com.akydd.realworld_spring.mapper;

import com.akydd.realworld_spring.dto.ArticleResponse;
import com.akydd.realworld_spring.dto.CreateArticleRequest;
import com.akydd.realworld_spring.dto.ProfileResponse;
import com.akydd.realworld_spring.model.Article;
import com.akydd.realworld_spring.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ArticleMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "slug", source = "title", qualifiedByName = "strToSlug")
    Article toEntity(CreateArticleRequest request);

    @Named("strToSlug")
    default String strToSlug(String str) {
        return str.replace(' ', '-').toLowerCase();
    }

    ArticleResponse toResponse(Article article);

    @Mapping(source = "realUsername", target = "username")
    @Mapping(target = "following", constant = "false")
    ProfileResponse toProfileResponse(User author);
}
