package com.akydd.realworld_spring.mapper;

import com.akydd.realworld_spring.dto.ProfileResponse;
import com.akydd.realworld_spring.model.Profile;
import org.mapstruct.Mapper;

@Mapper(componentModel ="spring")
public interface ProfileMapper {
    ProfileResponse toDTO(Profile profile);
}
