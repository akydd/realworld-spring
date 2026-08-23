package com.akydd.realworld_spring.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleException(@NonNull MethodArgumentNotValidException ex) {
        Map<String, String[]> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, new String[]{errorMessage});
        });

        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_CONTENT)
                .body(new ValidationErrorResponse(errors));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ValidationErrorResponse> handleException(@NonNull InvalidCredentialsException ex) {
        Map<String, String[]> errors = new HashMap<>();
        errors.put("credentials", new String[]{"invalid"});

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ValidationErrorResponse(errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ValidationErrorResponse> handleException(@NonNull Exception ex, HttpServletRequest request) {
        // Log the error
        logger.error("An unexpected error occurred during {} request to URI {}",
                request.getMethod(), request.getRequestURI(), ex);

        //Return formatted error to user.
        Map<String, String[]> errors = new HashMap<>();
        errors.put("message", new String[]{ex.getMessage()});

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ValidationErrorResponse(errors));
    }
}
