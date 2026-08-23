package com.akydd.realworld_spring.service;

import com.akydd.realworld_spring.model.Profile;
import com.akydd.realworld_spring.model.User;

public interface ProfileService {
    Profile follow(User user, String username);

    Profile unfollow(User user, String username);

    Profile get(User user, String username);
}
