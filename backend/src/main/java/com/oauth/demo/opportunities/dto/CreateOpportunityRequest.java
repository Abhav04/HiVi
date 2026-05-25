package com.oauth.demo.opportunities.dto;

public record CreateOpportunityRequest(
        String title,
        String company,
        String description,
        String applyUrl,
        String payLabel,
        String workMode,
        String category,
        String tags
) {}
