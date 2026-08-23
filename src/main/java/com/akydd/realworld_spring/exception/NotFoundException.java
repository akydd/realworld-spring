package com.akydd.realworld_spring.exception;

/** A resource wasn't found; maps to 404 with {@code {"errors":{field:["not found"]}}}. */
public class NotFoundException extends RuntimeException {
    private final String field;

    public NotFoundException(String field) {
        super("not found");
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
