package com.oauth.demo.reddit.service;

import com.oauth.demo.reddit.dto.RedditPostDto;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * Ranks Reddit posts by engagement, recency, and hiring relevance.
 */
public final class RedditPostRanker {

    private RedditPostRanker() {}

    public static double computeTrendingScore(
            int upvotes,
            int comments,
            long createdUtc,
            boolean hiring,
            String title,
            String subreddit,
            String selftext
    ) {
        double hours = hoursSince(createdUtc);
        double engagement = upvotes * 2.5 + comments * 4.0;
        double logBoost = Math.log10(Math.max(upvotes, 1) + 1) * 18.0;
        double recency = 120.0 / (hours + 1.5);
        double freshness = hours < 6 ? 30.0 : (hours < 24 ? 15.0 : 0.0);
        double hiringBoost = RedditHiringDetector.hiringBoost(title, subreddit, selftext);

        return engagement + logBoost + recency + freshness + hiringBoost;
    }

    public static List<RedditPostDto> sortByTrending(List<RedditPostDto> posts) {
        return posts.stream()
                .sorted(Comparator.comparingDouble(RedditPostDto::trendingScore).reversed()
                        .thenComparingInt(RedditPostDto::upvotes).reversed()
                        .thenComparingInt(RedditPostDto::commentCount).reversed())
                .toList();
    }

    private static double hoursSince(long createdUtc) {
        if (createdUtc <= 0) {
            return 48.0;
        }
        return Duration.between(Instant.ofEpochSecond(createdUtc), Instant.now()).toMinutes() / 60.0;
    }
}
