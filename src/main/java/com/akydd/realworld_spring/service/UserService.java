package com.akydd.realworld_spring.service;

import com.akydd.realworld_spring.model.User;
import com.akydd.realworld_spring.model.UpdateUser;

public interface UserService {
    User registerUser(User user);
    User loginUser(String email, String password);
    User updateUser(long userId, UpdateUser updateUser);
}
