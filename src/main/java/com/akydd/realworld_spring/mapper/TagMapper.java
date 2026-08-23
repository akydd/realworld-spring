package com.akydd.realworld_spring.mapper;

import com.akydd.realworld_spring.dto.TagsResponse;
import com.akydd.realworld_spring.model.Tag;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TagMapper {
    default TagsResponse toResponse(List<Tag> tags) {
        return new TagsResponse(tags.stream().map(Tag::getName).toList());
    }
}
