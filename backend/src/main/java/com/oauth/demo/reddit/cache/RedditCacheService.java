package com.oauth.demo.reddit.cache;

import com.oauth.demo.reddit.dto.RedditPostDto;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class RedditCacheService {

    private final CacheManager cacheManager;
    private final AtomicReference<List<RedditPostDto>> memoryFallback =
            new AtomicReference<>(Collections.emptyList());
    private volatile Instant cachedAt;

    public RedditCacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void put(List<RedditPostDto> posts) {
        List<RedditPostDto> safe = List.copyOf(posts);
        memoryFallback.set(safe);
        cachedAt = Instant.now();
        Cache cache = cacheManager.getCache(RedditCacheKeys.CACHE_NAME);
        if (cache != null) {
            cache.put(RedditCacheKeys.ALL_POSTS_KEY, safe);
        }
    }

    @SuppressWarnings("unchecked")
    public List<RedditPostDto> get() {
        Cache cache = cacheManager.getCache(RedditCacheKeys.CACHE_NAME);
        if (cache != null) {
            Cache.ValueWrapper wrapper = cache.get(RedditCacheKeys.ALL_POSTS_KEY);
            if (wrapper != null && wrapper.get() instanceof List<?> list) {
                return (List<RedditPostDto>) list;
            }
        }
        return memoryFallback.get();
    }

    public Instant getCachedAt() {
        return cachedAt;
    }

    public boolean isEmpty() {
        return get().isEmpty();
    }
}
