package com.akydd.realworld_spring.exception;

public class DuplicateFieldException extends RuntimeException {
    private final String field;

    public DuplicateFieldException(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
