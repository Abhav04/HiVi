package com.oauth.demo.reddit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oauth.demo.reddit.config.RedditProperties;
import com.oauth.demo.reddit.dto.RedditPostDto;
import com.oauth.demo.reddit.exception.RedditFetchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Fetches hot posts from Reddit's public JSON endpoints (no API key required).
 * Respects rate limits via configurable delay between subreddit requests.
 */
@Component
public class RedditApiClient {

    private static final Logger log = LoggerFactory.getLogger(RedditApiClient.class);
    private final RestTemplate redditRestTemplate;
    private final RedditProperties properties;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RedditApiClient(
            @Qualifier("redditRestTemplate") RestTemplate redditRestTemplate,
            RedditProperties properties) {
        this.redditRestTemplate = redditRestTemplate;
        this.properties = properties;
    }

    public List<RedditPostDto> fetchTrendingFromAllSubreddits() {
        List<RedditPostDto> all = new ArrayList<>();
        List<String> subreddits = properties.getSubreddits();

        for (int i = 0; i < subreddits.size(); i++) {
            String subreddit = subreddits.get(i);
            try {
                all.addAll(fetchHotPosts(subreddit));
            } catch (RedditFetchException ex) {
                log.warn("Skipping r/{} after fetch failure: {}", subreddit, ex.getMessage());
                if (ex.isRateLimited()) {
                    throw ex;
                }
            }

            if (i < subreddits.size() - 1) {
                sleepBetweenRequests();
            }
        }

        return dedupeAndSort(all);
    }

    public List<RedditPostDto> fetchHotPosts(String subreddit) {
        String url = String.format(
                Locale.US,
                "%s/r/%s/hot.json?limit=%d&raw_json=1",
                trimTrailingSlash(properties.getBaseUrl()),
                subreddit,
                properties.getPostsPerSubreddit()
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, properties.getUserAgent());
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = redditRestTemplate.exchange(
                    URI.create(url),
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new RedditFetchException("Unexpected Reddit response for r/" + subreddit, false);
            }

            String body = response.getBody().trim();
            if (body.startsWith("<") || !body.startsWith("{")) {
                throw new RedditFetchException(
                        "Reddit returned HTML instead of JSON for r/" + subreddit + " (blocked or rate limited)",
                        false
                );
            }

            return parseListing(body, subreddit);
        } catch (HttpStatusCodeException ex) {
            boolean rateLimited = ex.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS;
            log.error("Reddit HTTP {} for r/{}: {}", ex.getStatusCode(), subreddit, ex.getMessage());
            throw new RedditFetchException(
                    "Reddit API error for r/" + subreddit + ": " + ex.getStatusCode(),
                    ex,
                    rateLimited
            );
        } catch (RedditFetchException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to fetch r/{}", subreddit, ex);
            throw new RedditFetchException("Failed to fetch r/" + subreddit, ex, false);
        }
    }

    private List<RedditPostDto> parseListing(String json, String subreddit) throws Exception {
        JsonNode root = objectMapper.readTree(json);
        JsonNode children = root.path("data").path("children");
        List<RedditPostDto> posts = new ArrayList<>();

        if (!children.isArray()) {
            return posts;
        }

        for (JsonNode child : children) {
            JsonNode data = child.path("data");
            if (data.isMissingNode() || data.isNull()) {
                continue;
            }

            if (data.path("stickied").asBoolean(false)) {
                continue;
            }

            String title = data.path("title").asText("");
            if (title.isBlank()) {
                continue;
            }

            String id = data.path("id").asText("");
            String author = data.path("author").asText("[deleted]");
            int score = data.path("score").asInt(0);
            int comments = data.path("num_comments").asInt(0);
            long createdUtc = (long) data.path("created_utc").asDouble(0);
            String permalink = data.path("permalink").asText("");
            String redditUrl = permalink.startsWith("http")
                    ? permalink
                    : "https://www.reddit.com" + permalink;

            String selftext = data.path("selftext").asText("");
            if ("[deleted]".equals(selftext) || "[removed]".equals(selftext)) {
                continue;
            }
            RedditImageResolver.ResolvedImage image = RedditImageResolver.resolve(data);
            List<String> imageUrls = new ArrayList<>();
            if (image.primaryUrl() != null) {
                imageUrls.add(image.primaryUrl());
            }
            imageUrls.addAll(image.fallbackUrls());

            boolean hiring = RedditHiringDetector.isHiringPost(title, subreddit, selftext);
            String hiringBadge = RedditHiringDetector.resolveBadge(title, subreddit, selftext);
            double trendingScore = RedditPostRanker.computeTrendingScore(
                    score, comments, createdUtc, hiring, title, subreddit, selftext
            );

            posts.add(new RedditPostDto(
                    id,
                    title,
                    "r/" + subreddit,
                    author,
                    score,
                    comments,
                    image.primaryUrl(),
                    List.copyOf(imageUrls),
                    redditUrl,
                    redditUrl,
                    createdUtc,
                    formatTimeAgo(createdUtc),
                    trendingScore,
                    hiring,
                    hiringBadge,
                    image.mediaType()
            ));
        }

        log.info("Fetched {} posts from r/{}", posts.size(), subreddit);
        return posts;
    }

    private List<RedditPostDto> dedupeAndSort(List<RedditPostDto> posts) {
        Set<String> seen = new LinkedHashSet<>();
        List<RedditPostDto> unique = new ArrayList<>();

        for (RedditPostDto post : posts) {
            if (seen.add(post.id())) {
                unique.add(post);
            }
        }

        return RedditPostRanker.sortByTrending(unique);
    }

    private void sleepBetweenRequests() {
        try {
            Thread.sleep(properties.getRequestDelayMs());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private String trimTrailingSlash(String baseUrl) {
        if (baseUrl == null) {
            return "https://www.reddit.com";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    static String formatTimeAgo(long createdUtc) {
        if (createdUtc <= 0) {
            return "recently";
        }
        Duration ago = Duration.between(Instant.ofEpochSecond(createdUtc), Instant.now());
        long hours = ago.toHours();
        if (hours < 1) {
            long mins = Math.max(ago.toMinutes(), 1);
            return mins + "m ago";
        }
        if (hours < 48) {
            return hours + "h ago";
        }
        long days = ago.toDays();
        return days + "d ago";
    }
}
