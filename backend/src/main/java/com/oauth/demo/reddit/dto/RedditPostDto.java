package com.oauth.demo.reddit.dto;

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
        String permalink,
        String redditUrl,
        long createdUtc,
        String timeAgo
) {}
