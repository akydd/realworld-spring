package com.akydd.realworld_spring.mapper;

import com.akydd.realworld_spring.dto.ArticleSummaryResponse;
import com.akydd.realworld_spring.dto.ArticlesResponse;
import com.akydd.realworld_spring.dto.ProfileResponse;
import com.akydd.realworld_spring.model.ArticleSummary;
import com.akydd.realworld_spring.model.ArticleSummaryView;
import com.akydd.realworld_spring.model.Tag;
import com.akydd.realworld_spring.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface ArticleSummaryMapper {
    @Mapping(source = "summary.tags", target = "tagList")
    @Mapping(target = "author", expression = "java(toProfileResponse(summary.getAuthor(), following))")
    ArticleSummaryResponse toResponse(ArticleSummary summary, boolean favorited, boolean following);

    default ArticleSummaryResponse toResponse(ArticleSummaryView view) {
        return toResponse(view.article(), view.favorited(), view.following());
    }

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

    default ArticlesResponse toResponse(List<ArticleSummaryView> views) {
        List<ArticleSummaryResponse> summaries = views.stream()
                .map(view -> toResponse(view))
                .toList();

        return new ArticlesResponse(summaries, summaries.isEmpty() ? 0 : summaries.size());
    }

    @Mapping(source = "author.realUsername", target = "username")
    @Mapping(target = "following", source = "following")
    ProfileResponse toProfileResponse(User author, boolean following);
}
