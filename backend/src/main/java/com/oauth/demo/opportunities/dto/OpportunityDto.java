package com.oauth.demo.opportunities.dto;

import java.time.LocalDateTime;
import java.util.List;

public record OpportunityDto(
        Long id,
        String source,
        String title,
        String company,
        String logoUrl,
        String logoFallbackUrl,
        String companyInitials,
        String description,
        String applyUrl,
        String payLabel,
        String workMode,
        String category,
        List<String> tags,
        List<String> badges,
        double trendingScore,
        int engagementCount,
        LocalDateTime postedAt,
        String timeAgo
) {}
