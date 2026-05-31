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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class RedditTrendingService {

    private static final Logger log = LoggerFactory.getLogger(RedditTrendingService.class);
    private static final int MAX_HIRING_SECTION = 8;

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
            List<RedditPostDto> fallback = RedditFallbackPosts.curated();
            cacheService.put(fallback);
            cached = fallback;
            stale = true;
            message = "Showing curated editor highlights while live Reddit sync catches up.";
            log.warn("Reddit cache empty after refresh — serving {} fallback posts", fallback.size());
        }

        List<RedditPostDto> ranked = RedditPostRanker.sortByTrending(cached);
        List<RedditPostDto> filtered = filterBySubreddit(ranked, subredditFilter);

        int safeLimit = Math.min(50, Math.max(1, limit));
        int safePage = Math.max(page, 0);

        RedditPostDto featured = safePage == 0 && !filtered.isEmpty() ? filtered.get(0) : null;
        List<RedditPostDto> hiringPosts = extractHiringPosts(filtered, featured);

        List<RedditPostDto> gridSource = new ArrayList<>(filtered);
        if (featured != null) {
            gridSource.removeIf(p -> p.id().equals(featured.id()));
        }

        int from = safePage * safeLimit;
        int to = Math.min(from + safeLimit, gridSource.size());
        List<RedditPostDto> pageItems = from >= gridSource.size()
                ? List.of()
                : gridSource.subList(from, to);

        return buildResponse(
                pageItems,
                featured,
                hiringPosts,
                subredditFilter,
                safePage,
                safeLimit,
                stale,
                message,
                gridSource.size()
        );
    }

    public void refreshCache() {
        if (!refreshInProgress.compareAndSet(false, true)) {
            log.debug("Reddit refresh already in progress — skipping");
            return;
        }

        try {
            log.info("Refreshing Reddit trending cache for {} subreddits", properties.getSubreddits().size());
            List<RedditPostDto> posts = redditApiClient.fetchTrendingFromAllSubreddits();
            List<RedditPostDto> ranked = RedditPostRanker.sortByTrending(posts);
            cacheService.put(ranked);
            log.info("Reddit cache updated with {} posts (ranked)", ranked.size());
        } catch (RedditFetchException ex) {
            log.error("Reddit refresh failed: {}", ex.getMessage());
            if (cacheService.isEmpty()) {
                List<RedditPostDto> fallback = RedditFallbackPosts.curated();
                cacheService.put(fallback);
                log.warn("Loaded {} Reddit fallback posts after API failure", fallback.size());
            } else {
                log.warn("Serving stale Reddit cache after refresh failure");
            }
        } catch (Exception ex) {
            log.error("Unexpected Reddit refresh error", ex);
        } finally {
            refreshInProgress.set(false);
        }
    }

    private List<RedditPostDto> extractHiringPosts(List<RedditPostDto> filtered, RedditPostDto featured) {
        String featuredId = featured != null ? featured.id() : null;
        return filtered.stream()
                .filter(RedditPostDto::hiring)
                .filter(p -> featuredId == null || !p.id().equals(featuredId))
                .limit(MAX_HIRING_SECTION)
                .toList();
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
            RedditPostDto featured,
            List<RedditPostDto> hiringPosts,
            String subredditFilter,
            int page,
            int limit,
            boolean stale,
            String message,
            int totalGrid
    ) {
        Instant cachedAt = cacheService.getCachedAt();
        boolean hasMore = (page + 1) * limit < totalGrid;

        return new RedditTrendingResponse(
                pageItems,
                featured,
                hiringPosts,
                properties.getSubreddits().stream().map(s -> "r/" + s).toList(),
                subredditFilter == null || subredditFilter.isBlank() ? "all" : subredditFilter,
                page,
                limit,
                totalGrid,
                hasMore,
                cachedAt,
                stale,
                message
        );
    }
}
