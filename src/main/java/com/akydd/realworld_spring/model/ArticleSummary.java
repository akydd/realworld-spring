package com.akydd.realworld_spring.model;

import java.time.LocalDateTime;
import java.util.Set;

public interface ArticleSummary
{
    Long getId();
    String getSlug();
    String getTitle();
    String getDescription();
    Set<Tag> getTags();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
    int getFavoritesCount();
    User getAuthor();
}
