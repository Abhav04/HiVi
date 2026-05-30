package com.oauth.demo.community.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CommunityPostDto(
        Long id,
        String title,
        String content,
        String postType,
        String status,
        String mediaUrl,
        String thumbnailUrl,
        String portfolioLink,
        String externalLink,
        List<String> tags,
        List<PostMediaDto> mediaItems,
        RepostSummaryDto repostOf,
        int repostCount,
        int likeCount,
        int commentCount,
        int viewCount,
        double trendingScore,
        boolean likedByMe,
        boolean bookmarkedByMe,
        boolean ownedByMe,
        AuthorSummaryDto author,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
