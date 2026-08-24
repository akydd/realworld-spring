package com.akydd.realworld_spring.exception;

import java.util.Map;

/**
 * The RealWorld error shape: serializes to {@code {"errors":{field:[messages]}}} — the record
 * component name {@code errors} is the root key, so no Jackson root-wrapping is involved.
 */
public record ValidationErrorResponse(Map<String, String[]> errors) {
}
