package com.akydd.realworld_spring.exception;

public class ForbiddenException extends RuntimeException {
    private final String entity;

    public ForbiddenException(String entity) {
        this.entity = entity;
    }

    public String getEntity() {
        return entity;
    }
}
