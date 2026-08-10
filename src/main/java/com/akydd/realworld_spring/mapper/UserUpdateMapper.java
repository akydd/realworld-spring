package com.akydd.realworld_spring.mapper;

import com.akydd.realworld_spring.dto.UpdateUserRequest;
import com.akydd.realworld_spring.json.Tristate;
import com.akydd.realworld_spring.model.UpdateUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface UserUpdateMapper {
    @Mapping(target = "image", source = "image", qualifiedByName = "normalizeNullable")
    @Mapping(target = "bio", source = "bio", qualifiedByName = "normalizeNullable")
    UpdateUser toEntity(UpdateUserRequest request);

    @Named("normalizeNullable")
    default Tristate<String> normalize(Tristate<String> val) {
        if (val == null || !val.isPresent()) {
            return Tristate.undefined();
        }
        String v = val.get();
        return Tristate.of((v == null || v.isBlank()) ? null : v);
    }
}
