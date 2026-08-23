package com.akydd.realworld_spring.service;

import com.akydd.realworld_spring.model.UpdateUser;
import com.akydd.realworld_spring.model.User;

public interface UserService {
    User registerUser(User user);

    User loginUser(String email, String password);

    User updateUser(User user, UpdateUser updateUser);
}
