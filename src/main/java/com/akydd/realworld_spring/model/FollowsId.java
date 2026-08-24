package com.akydd.realworld_spring.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class FollowsId implements Serializable {
    @Column(name = "follower_id")
    private Long followerId;
    @Column(name = "following_id")
    private Long followingId;

    public FollowsId() {
    }

    public FollowsId(Long followerId, Long followingId) {
        this.followerId = followerId;
        this.followingId = followingId;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FollowsId that = (FollowsId) o;
        return Objects.equals(followerId, that.followerId) && Objects.equals(followingId, that.followingId);
    }

    public int hashCode() {
        return Objects.hash(followerId, followingId);
    }
}
