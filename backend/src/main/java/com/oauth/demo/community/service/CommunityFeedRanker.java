package com.oauth.demo.community.service;

import com.oauth.demo.community.entity.CommunityPost;
import com.oauth.demo.community.entity.PostType;

import java.time.Duration;
import java.time.LocalDateTime;

public final class CommunityFeedRanker {

    private CommunityFeedRanker() {}

    public static double computeScore(CommunityPost post) {
        int likes = post.getLikeCount();
        int comments = post.getCommentCount();
        int views = post.getViewCount();

        double hours = hoursSince(post.getCreatedAt());
        double engagement = likes * 3.0 + comments * 5.0 + views * 0.05;
        double logBoost = Math.log10(Math.max(likes, 1) + 1) * 20.0;
        double recency = 150.0 / (hours + 2.0);
        double typeBoost = typeBoost(post.getPostType());
        double repostBoost = post.getRepostCount() * 2.0;
        boolean hasMedia = post.getMediaUrl() != null
                || (post.getMediaItems() != null && !post.getMediaItems().isEmpty());

        return engagement + logBoost + recency + typeBoost + repostBoost + (hasMedia ? 10.0 : 0);
    }

    private static double typeBoost(PostType type) {
        if (type == null) return 0;
        return switch (type) {
            case PORTFOLIO, PORTFOLIO_SHOWCASE, CLIENT_WORK, BEFORE_AFTER -> 28.0;
            case VIDEO, EDITING_BREAKDOWN -> 22.0;
            case TUTORIAL, ACHIEVEMENT -> 18.0;
            case AVAILABLE_FOR_WORK, HIRING, COLLABORATION -> 16.0;
            case IMAGE -> 12.0;
            case REPOST -> 8.0;
            case LINK -> 10.0;
            default -> 0;
        };
    }

    private static double hoursSince(LocalDateTime createdAt) {
        if (createdAt == null) {
            return 48.0;
        }
        return Duration.between(createdAt, LocalDateTime.now()).toMinutes() / 60.0;
    }
}
