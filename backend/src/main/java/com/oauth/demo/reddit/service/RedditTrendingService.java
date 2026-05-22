package com.oauth.demo.reddit.service;

import com.oauth.demo.reddit.cache.RedditCacheService;
import com.oauth.demo.reddit.config.RedditProperties;
import com.oauth.demo.reddit.dto.RedditPostDto;
import com.oauth.demo.reddit.dto.RedditTrendingResponse;
import com.oauth.demo.reddit.exception.RedditFetchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class RedditTrendingService {

    private static final Logger log = LoggerFactory.getLogger(RedditTrendingService.class);

    private final RedditApiClient redditApiClient;
    private final RedditCacheService cacheService;
    private final RedditProperties properties;
    private final AtomicBoolean refreshInProgress = new AtomicBoolean(false);

    public RedditTrendingService(
            RedditApiClient redditApiClient,
            RedditCacheService cacheService,
            RedditProperties properties) {
        this.redditApiClient = redditApiClient;
        this.cacheService = cacheService;
        this.properties = properties;
    }

    public RedditTrendingResponse getTrending(String subredditFilter, int page, int limit) {
        List<RedditPostDto> cached = cacheService.get();

        if (cached.isEmpty()) {
            log.info("Reddit cache empty — triggering synchronous refresh");
            refreshCache();
            cached = cacheService.get();
        }

        boolean stale = false;
        String message = null;

        if (cached.isEmpty()) {
            message = "Trending posts are temporarily unavailable. Please try again shortly.";
            return buildResponse(List.of(), subredditFilter, page, limit, stale, message);
        }

        List<RedditPostDto> filtered = filterBySubreddit(cached, subredditFilter);
        int safeLimit = Math.min(50, Math.max(1, limit));
        int safePage = Math.max(page, 0);
        int from = safePage * safeLimit;
        int to = Math.min(from + safeLimit, filtered.size());
        List<RedditPostDto> pageItems = from >= filtered.size()
                ? List.of()
                : filtered.subList(from, to);

        return buildResponse(pageItems, subredditFilter, safePage, safeLimit, stale, message, filtered.size());
    }

    public void refreshCache() {
        if (!refreshInProgress.compareAndSet(false, true)) {
            log.debug("Reddit refresh already in progress — skipping");
            return;
        }

        try {
            log.info("Refreshing Reddit trending cache for {} subreddits", properties.getSubreddits().size());
            List<RedditPostDto> posts = redditApiClient.fetchTrendingFromAllSubreddits();
            cacheService.put(posts);
            log.info("Reddit cache updated with {} posts", posts.size());
        } catch (RedditFetchException ex) {
            log.error("Reddit refresh failed: {}", ex.getMessage());
            if (!cacheService.isEmpty()) {
                log.warn("Serving stale Reddit cache after refresh failure");
            }
        } catch (Exception ex) {
            log.error("Unexpected Reddit refresh error", ex);
        } finally {
            refreshInProgress.set(false);
        }
    }

    private List<RedditPostDto> filterBySubreddit(List<RedditPostDto> posts, String subredditFilter) {
        if (subredditFilter == null || subredditFilter.isBlank()
                || "all".equalsIgnoreCase(subredditFilter)) {
            return posts;
        }

        String normalized = subredditFilter.toLowerCase(Locale.ROOT);
        if (!normalized.startsWith("r/")) {
            normalized = "r/" + normalized;
        }

        final String target = normalized;
        return posts.stream()
                .filter(p -> p.subreddit().toLowerCase(Locale.ROOT).equals(target))
                .toList();
    }

    private RedditTrendingResponse buildResponse(
            List<RedditPostDto> pageItems,
            String subredditFilter,
            int page,
            int limit,
            boolean stale,
            String message
    ) {
        return buildResponse(pageItems, subredditFilter, page, limit, stale, message, pageItems.size());
    }

    private RedditTrendingResponse buildResponse(
            List<RedditPostDto> pageItems,
            String subredditFilter,
            int page,
            int limit,
            boolean stale,
            String message,
            int totalFiltered
    ) {
        Instant cachedAt = cacheService.getCachedAt();
        boolean hasMore = (page + 1) * limit < totalFiltered;

        return new RedditTrendingResponse(
                pageItems,
                properties.getSubreddits().stream().map(s -> "r/" + s).toList(),
                subredditFilter == null || subredditFilter.isBlank() ? "all" : subredditFilter,
                page,
                limit,
                totalFiltered,
                hasMore,
                cachedAt,
                stale,
                message
        );
    }
}
