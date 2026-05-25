package com.oauth.demo.opportunities.dto;

import java.util.List;

public record OpportunityFeedResponse(
        List<OpportunityDto> opportunities,
        List<OpportunityDto> trending,
        List<OpportunityDto> latest,
        int page,
        int size,
        long totalElements,
        boolean hasMore,
        String categoryFilter,
        String sourceFilter
) {}
