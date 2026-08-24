package com.akydd.realworld_spring.repository;

import com.akydd.realworld_spring.model.Follows;
import com.akydd.realworld_spring.model.FollowsId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowsRepository extends JpaRepository<Follows, FollowsId> {
}
