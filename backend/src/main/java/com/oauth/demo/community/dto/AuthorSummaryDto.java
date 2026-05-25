package com.oauth.demo.community.dto;

public record AuthorSummaryDto(
        Long id,
        String username,
        String displayName,
        String avatarUrl,
        String niche,
        boolean availableForWork,
        boolean following
) {}
