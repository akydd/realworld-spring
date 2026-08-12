package com.akydd.realworld_spring.service;

import com.akydd.realworld_spring.model.Profile;
import com.akydd.realworld_spring.model.User;
import com.akydd.realworld_spring.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;

    public ProfileServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public Profile follow(User user, String username) {
        User userToFollow = userRepository.findByUsername(username).orElseThrow();
        User me = userRepository.findById(user.getId()).orElseThrow();

        if (!me.getFollowing().contains(userToFollow)) {
            me.addFollowing(userToFollow);
        }

        return toProfile(me, true);
    }

    private Profile toProfile(User user, Boolean following) {
        return new Profile(
                user.getRealUsername(),
                user.getBio(),
                user.getImage(),
                following)
                ;
    }
}
