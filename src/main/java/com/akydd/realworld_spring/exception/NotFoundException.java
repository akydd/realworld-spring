package com.akydd.realworld_spring.exception;

public class NotFoundException extends RuntimeException {
    private final String field;

    public NotFoundException(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }

    public String getMessage() {
        return "Could not find " + field;
    }
}
