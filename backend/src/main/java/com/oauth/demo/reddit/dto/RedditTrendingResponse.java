package com.oauth.demo.reddit.dto;

import java.time.Instant;
import java.util.List;

public record RedditTrendingResponse(
        List<RedditPostDto> posts,
        List<String> subreddits,
        String activeSubreddit,
        int page,
        int limit,
        int total,
        boolean hasMore,
        Instant cachedAt,
        boolean stale,
        String message
) {}
