package com.oauth.demo.community.service;

import com.oauth.demo.community.entity.CommunityPost;

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
        double portfolioBoost = post.getPostType() == com.oauth.demo.community.entity.PostType.PORTFOLIO ? 25.0 : 0;
        double videoBoost = post.getPostType() == com.oauth.demo.community.entity.PostType.VIDEO ? 15.0 : 0;

        return engagement + logBoost + recency + portfolioBoost + videoBoost;
    }

    private static double hoursSince(LocalDateTime createdAt) {
        if (createdAt == null) {
            return 48.0;
        }
        return Duration.between(createdAt, LocalDateTime.now()).toMinutes() / 60.0;
    }
}
