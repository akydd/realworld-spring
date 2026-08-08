package com.akydd.realworld_spring.service;

import jakarta.servlet.http.HttpServletRequest;

public interface JwtService {
    String generateToken(Long userid);
    String extractUserId(String token);
}
