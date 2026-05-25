package com.oauth.demo.community.dto;

import java.util.List;

public record CreatorProfileDto(
        Long userId,
        String username,
        String displayName,
        String bio,
        String niche,
        String tools,
        String avatarUrl,
        String bannerUrl,
        String portfolioUrl,
        String instagramUrl,
        String youtubeUrl,
        String websiteUrl,
        boolean availableForWork,
        int totalPosts,
        int totalLikes,
        long totalViews,
        long followerCount,
        long followingCount,
        boolean following,
        List<CommunityPostDto> recentPosts
) {}
