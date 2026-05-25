package com.oauth.demo.community.dto;

import java.util.List;

public record CommunityFeedResponse(
        List<CommunityPostDto> posts,
        CommunityPostDto featuredPost,
        List<CommunityPostDto> topLikedPosts,
        List<AuthorSummaryDto> trendingCreators,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasMore
) {}
