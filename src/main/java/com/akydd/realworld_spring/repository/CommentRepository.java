package com.akydd.realworld_spring.repository;

import com.akydd.realworld_spring.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByArticleIdOrderByCreatedAtAsc(Long articleId);
}
