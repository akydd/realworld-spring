package com.akydd.realworld_spring.service;

import com.akydd.realworld_spring.exception.NotFoundException;
import com.akydd.realworld_spring.model.Follows;
import com.akydd.realworld_spring.model.FollowsId;
import com.akydd.realworld_spring.model.Profile;
import com.akydd.realworld_spring.model.User;
import com.akydd.realworld_spring.repository.FollowsRepository;
import com.akydd.realworld_spring.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final FollowsRepository followsRepository;

    public ProfileServiceImpl(UserRepository userRepository, FollowsRepository followsRepository) {
        this.userRepository = userRepository;
        this.followsRepository = followsRepository;
    }

    @Transactional
    public Profile follow(User user, String username) {
        User userToFollow = userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("profile"));

        // O(1) via the follows primary key — no collection load. Idempotent: following twice is a no-op.
        FollowsId id = new FollowsId(user.getId(), userToFollow.getId());
        if (!followsRepository.existsById(id)) {
            followsRepository.save(new Follows(userRepository.getReferenceById(user.getId()), userToFollow));
        }

        return toProfile(userToFollow, true);
    }

    @Transactional
    public Profile unfollow(User user, String username) {
        User userToUnfollow = userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("profile"));

        FollowsId id = new FollowsId(user.getId(), userToUnfollow.getId());
        if (followsRepository.existsById(id)) {
            followsRepository.deleteById(id);
        }

        return toProfile(userToUnfollow, false);
    }

    @Transactional
    public Profile get(User user, String username) {
        User profileUser = userRepository.findByUsername(username).orElseThrow(() -> new NotFoundException("profile"));
        return toProfile(profileUser, user != null && followsRepository.existsById(new FollowsId(user.getId(), profileUser.getId())));
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
