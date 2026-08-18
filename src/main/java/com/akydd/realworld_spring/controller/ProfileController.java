package com.akydd.realworld_spring.controller;

import com.akydd.realworld_spring.dto.ProfileResponse;
import com.akydd.realworld_spring.mapper.ProfileMapper;
import com.akydd.realworld_spring.model.Profile;
import com.akydd.realworld_spring.model.User;
import com.akydd.realworld_spring.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    private final ProfileService profileService;
    private final ProfileMapper profileMapper;

    public ProfileController(ProfileService profileService, ProfileMapper profileMapper) {
        this.profileService = profileService;
        this.profileMapper = profileMapper;
    }

    @PostMapping("{username}/follow")
    public ResponseEntity<ProfileResponse> follow(@AuthenticationPrincipal User principal, @PathVariable String username) {
        Profile profile = profileService.follow(principal, username);
        return ResponseEntity.ok(profileMapper.toDTO(profile));
    }

    @DeleteMapping("{username}/follow")
    public ResponseEntity<ProfileResponse> unfollow(@AuthenticationPrincipal User principal, @PathVariable String username) {
        Profile profile = profileService.unfollow(principal, username);
        return ResponseEntity.ok(profileMapper.toDTO(profile));
    }

    @GetMapping("{username}")
    public ResponseEntity<ProfileResponse> getProfile(@AuthenticationPrincipal User principal, @PathVariable String username) {
        Profile profile = profileService.get(principal, username);
        return ResponseEntity.ok(profileMapper.toDTO(profile));
    }
}
