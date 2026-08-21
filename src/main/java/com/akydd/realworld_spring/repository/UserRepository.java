package com.akydd.realworld_spring.repository;

import com.akydd.realworld_spring.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);

    @Query("select count(f) > 0 from Follows f where f.follower.id = :followerId and f.following.id = :followingId")
    boolean isFollowing(@Param("followerId") Long followerId, @Param("followingId") Long followingId);
}
