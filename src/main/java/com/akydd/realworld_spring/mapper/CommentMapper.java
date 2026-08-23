package com.akydd.realworld_spring.mapper;

import com.akydd.realworld_spring.dto.CommentResponse;
import com.akydd.realworld_spring.dto.CommentsResponse;
import com.akydd.realworld_spring.dto.CreateCommentRequest;
import com.akydd.realworld_spring.dto.ProfileResponse;
import com.akydd.realworld_spring.model.Comment;
import com.akydd.realworld_spring.model.CommentView;
import com.akydd.realworld_spring.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "article", ignore = true)
    @Mapping(target = "author", ignore = true)
    Comment toEntity(CreateCommentRequest request);

    @Mapping(target = "author", expression = "java(toProfileResponse(comment.getAuthor(), following))")
    CommentResponse toResponse(Comment comment, boolean following);

    default CommentResponse toResponse(CommentView view) {
        return toResponse(view.comment(), view.following());
    }

    @Mapping(source = "following", target = "following")
    @Mapping(source = "author.realUsername", target = "username")
    ProfileResponse toProfileResponse(User author, boolean following);

    default CommentsResponse toResponse(List<CommentView> views) {
        List<CommentResponse> responses = views.stream().map(this::toResponse).toList();
        return new CommentsResponse(responses);
    }
}
