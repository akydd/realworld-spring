package com.akydd.realworld_spring.repository;

import com.akydd.realworld_spring.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
