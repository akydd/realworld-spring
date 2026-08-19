package com.akydd.realworld_spring.controller;

import com.akydd.realworld_spring.dto.TagsResponse;
import com.akydd.realworld_spring.mapper.TagMapper;
import com.akydd.realworld_spring.model.Tag;
import com.akydd.realworld_spring.service.TagService;
import org.springframework.core.metrics.StartupStep;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
public class TagController {
    private final TagService tagService;
    private final TagMapper tagMapper;

    public TagController(TagService tagService, TagMapper tagMapper) {
        this.tagService = tagService;
        this.tagMapper = tagMapper;
    }

    @GetMapping
    public ResponseEntity<TagsResponse> findAll() {
        List<Tag> tags = tagService.findAll();
        return ResponseEntity.ok(tagMapper.toResponse(tags));
    }
}
