package com.akydd.realworld_spring.service;

public interface JwtService {
    String generateToken(Long userid);
    String extractUserId(String token);
}
