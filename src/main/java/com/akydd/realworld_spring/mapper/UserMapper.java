package com.akydd.realworld_spring.mapper;

import com.akydd.realworld_spring.dto.RegisterUserRequest;
import com.akydd.realworld_spring.dto.UpdateUserRequest;
import com.akydd.realworld_spring.dto.UserResponse;
import com.akydd.realworld_spring.model.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @BeanMapping(unmappedTargetPolicy = ReportingPolicy.IGNORE)
    User toEntity(RegisterUserRequest registerUserRequest);

    // This mapping is needed to get around the user having a username, but using the userid
    // in the userDetails.
    @Mapping(source="realUsername", target="username")
    UserResponse toDTO(User user);
}
