package com.akydd.realworld_spring.model;

import jakarta.persistence.*;

@Entity
@Table(name = "follows")
public class Follows {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id")
    private User follower;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id")
    private User following;
}
