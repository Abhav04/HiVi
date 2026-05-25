package com.oauth.demo.reddit.dto;

import java.util.List;

/**
 * Normalized Reddit post for the HiVi trending feed.
 */
public record RedditPostDto(
        String id,
        String title,
        String subreddit,
        String author,
        int upvotes,
        int commentCount,
        String thumbnailUrl,
        List<String> imageUrls,
        String permalink,
        String redditUrl,
        long createdUtc,
        String timeAgo,
        double trendingScore,
        boolean hiring,
        String hiringBadge,
        String mediaType
) {}
