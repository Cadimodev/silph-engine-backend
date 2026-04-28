package com.silphengine.application.mappers;

import com.silphengine.domain.dto.requests.UserProfileRequest;
import com.silphengine.domain.dto.requests.UserRequest;
import com.silphengine.domain.dto.responses.UserResponse;
import com.silphengine.domain.enums.Role;
import com.silphengine.domain.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;

@Mapper
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "collection", ignore = true)
    @Mapping(target = "decks", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "password", source = "encodedPassword")
    User toEntity(UserRequest userRequest, String encodedPassword);

    @Mapping(target = "created", source = "user.createdAt")
    UserResponse toResponse(User user);

    default void updateEntityFromRequest(User user, UserProfileRequest userProfileRequest) {

        if (user == null || userProfileRequest == null) {
            return;
        }

        user.updateProfile(userProfileRequest.nickname(), userProfileRequest.email());
    }
}
