package com.akydd.realworld_spring.model;

import jakarta.persistence.*;

@Entity
@Table(name = "follows")
public class Follows {
    @EmbeddedId
    private FollowsId id = new FollowsId();
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("followerId")
    @JoinColumn(name = "follower_id")
    private User follower;
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("followingId")
    @JoinColumn(name = "following_id")
    private User following;

    protected Follows() {
    }

    public Follows(User follower, User following) {
        this.id = new FollowsId(follower.getId(), following.getId());
        this.follower = follower;
        this.following = following;
    }

    public FollowsId getId() {
        return id;
    }

    public void setId(FollowsId id) {
        this.id = id;
    }

    public User getFollower() {
        return follower;
    }

    public void setFollower(User follower) {
        this.follower = follower;
    }

    public User getFollowing() {
        return following;
    }

    public void setFollowing(User following) {
        this.following = following;
    }
}
