package com.akydd.realworld_spring.service;

import com.akydd.realworld_spring.model.Profile;
import com.akydd.realworld_spring.model.User;
import jakarta.annotation.Nullable;

public interface ProfileService {
    Profile follow(User user, String username);

    Profile unfollow(User user, String username);

    Profile get(@Nullable User user, String username);
}
