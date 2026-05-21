# Reddit Trending Feed

## Folder structure

```
backend/src/main/java/com/oauth/demo/reddit/
├── config/
│   ├── RedditProperties.java      # Subreddits, delays, timeouts (application.properties)
│   └── RedditClientConfig.java    # RestTemplate bean for Reddit HTTP
├── controller/
│   └── RedditController.java      # GET /api/reddit/trending
├── service/
│   ├── RedditApiClient.java       # Fetches Reddit public JSON endpoints
│   └── RedditTrendingService.java # Cache read/write, filtering, pagination
├── cache/
│   ├── RedditCacheKeys.java
│   └── RedditCacheService.java    # Spring Cache + in-memory fallback
├── scheduler/
│   └── RedditRefreshScheduler.java # Startup warm + fixed-delay refresh
├── dto/
│   ├── RedditPostDto.java
│   └── RedditTrendingResponse.java
└── exception/
    ├── RedditFetchException.java
    └── RedditExceptionHandler.java
```

## API flow

1. **Startup / every 5 min** — `RedditRefreshScheduler` calls `RedditTrendingService.refreshCache()`.
2. **Refresh** — `RedditApiClient` fetches `/r/{sub}/hot.json` for each subreddit (with delay between calls).
3. **Cache** — Results stored in `reddit-trending` cache (Redis locally, in-memory on Render prod).
4. **Frontend** — `GET /api/reddit/trending?subreddit=&page=&limit=` reads cache only (no live Reddit call).
5. **Response** — Paginated, filterable `RedditTrendingResponse` JSON for the dashboard feed.

## Caching strategy

| Layer | Behavior |
|-------|----------|
| **Scheduled refresh** | Every `reddit.refresh-interval-ms` (default 5 min) |
| **Startup warm** | Cache populated on `ApplicationReadyEvent` |
| **API requests** | Serve cached data; sync refresh only if cache empty |
| **Redis (local)** | `reddit-trending` cache, 10 min TTL |
| **In-memory (prod)** | `ConcurrentMapCacheManager` when Redis disabled |
| **Fallback** | If refresh fails, stale cache continues to serve |

## Rate limits

Reddit public JSON endpoints (unauthenticated):

- ~**10 requests per minute** per client IP
- HiVi uses **~6 requests per refresh** (one per subreddit) with **1.1s delay** between them
- Refresh runs **every 5 minutes** → well under the limit
- User-facing API **never** calls Reddit directly → frontend traffic does not consume Reddit quota
- On **429 Too Many Requests**, refresh aborts and stale cache is served; error is logged

## Configuration

```properties
reddit.subreddits=videoediting,editors,AfterEffects,premiere,videography,Filmmakers
reddit.posts-per-subreddit=8
reddit.refresh-interval-ms=300000
reddit.request-delay-ms=1100
reddit.user-agent=HiVi/1.0 (Spring Boot; +https://hi-vi.vercel.app)
```

## Endpoint

`GET /api/reddit/trending`

Query params: `subreddit` (optional), `page` (default 0), `limit` (default 20, max 50).

Public (no JWT required) — read-only cached data.
