package com.akydd.realworld_spring.exception;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.HashMap;
import java.util.Map;

@JsonRootName("errors")
public class ValidationErrorResponse {
    private final Map<String, String[]> errors = new HashMap<>();

    public ValidationErrorResponse(Map<String, String[]> errors) {
        this.errors.putAll(errors);
    }

    @JsonAnyGetter
    public Map<String, String[]> getErrors() {
        return errors;
    }
}