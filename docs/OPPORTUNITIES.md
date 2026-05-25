# HiVi Opportunities

Curated career discovery for video editors, motion designers, and reel creators. HiVi summarizes roles and sends users to the **original platform** to apply — not a scraped job board.

## Architecture

```
┌─────────────┐     ┌──────────────────────┐     ┌─────────────────┐
│  Schedulers │────▶│ OpportunityIngestion │────▶│  PostgreSQL     │
│  (15 min)   │     │  upsert by source+id │     │  opportunities  │
└─────────────┘     └──────────────────────┘     └────────┬────────┘
        ▲                      ▲                           │
        │                      │                           ▼
┌───────┴───────┐    ┌────────┴────────┐         ┌─────────────────┐
│ Reddit JSON   │    │ curated-oppor-  │         │ OpportunityService│
│ (public API)  │    │ tunities.json   │         │ + Redis/simple  │
└───────────────┘    └─────────────────┘         │ cache           │
                                                 └────────┬────────┘
                                                          ▼
                                                 GET /api/opportunities*
```

## How opportunities are fetched

| Source | Method | What we store |
|--------|--------|----------------|
| **Reddit** | Public `.json` hot listings from editor hiring subreddits (`VideoEditor_forhire`, `forhire`, etc.). Filtered with `RedditHiringDetector`. | Title, short summary, subreddit as company, permalink as `applyUrl`, engagement for ranking. |
| **Internshala / LinkedIn** | Static curated entries in `curated-opportunities.json` — hub/search URLs only. **No HTML scraping.** | Discovery cards that link to official Internshala/LinkedIn pages. |
| **User** | `POST /api/opportunities` (authenticated). | Full user-provided fields; `source=USER`. |

Refresh: `OpportunityRefreshScheduler` runs on `opportunities.refresh-interval-ms` (default 15 minutes). First boot seeds DB if empty via `OpportunityBootstrap`.

## Ranking

`OpportunityRanker` computes `trendingScore` from:

- Reddit upvotes and comment count (capped to limit outliers)
- Recency (boost for posts &lt; 72 hours old)
- Badges: Urgent, Paid, Remote, Internship

Feed sort: `trendingScore DESC`, then `postedAt DESC`. Trending sidebar uses top 12 by score; latest list uses top 8 by `postedAt`.

## Caching

- Cache names: `opportunities-feed`, `opportunities-trending`
- **Production (Redis):** 8-minute TTL per cache entry
- **Local (`spring.cache.type=simple`):** in-memory `ConcurrentMapCacheManager`
- Invalidated on ingestion refresh and user posts (`@CacheEvict`)

## APIs

| Endpoint | Auth | Description |
|----------|------|-------------|
| `GET /api/opportunities` | Public | Paginated feed; `category`, `source` query params |
| `GET /api/opportunities/trending` | Public | Top trending DTOs |
| `GET /api/opportunities/category/{type}` | Public | Category-filtered feed |
| `POST /api/opportunities` | JWT | User-posted opportunity |

## Safe / legal integration

1. **No full-platform scraping** — we do not copy LinkedIn/Internshala job HTML or republish full listings.
2. **Reddit** — public JSON endpoints with configured `User-Agent`, rate-limited delays (reuse `reddit.request-delay-ms` pattern).
3. **Attribution** — every card shows `source` and **Apply now** opens the canonical external URL in a new tab.
4. **User content** — posters supply their own apply links; HiVi is a discovery layer only.
5. **Future sources** — add new `OpportunitySource` + fetcher interface; keep upsert key `(source, externalId)`.

## Scalability

- Stateless API servers; shared Redis + PostgreSQL
- Pagination on all list endpoints
- Scheduled ingestion decoupled from read path (reads hit cache + DB)
- Deduplication via `(source, externalId)` unique index
- Extensible categories/badges via `OpportunityClassifier` without schema churn for new tags

## Company logos

Each card includes `logoUrl`, `logoFallbackUrl`, and `companyInitials` (API DTO fields).

Resolution order (`CompanyLogoResolver`):

1. Manual mapping in `company-logo-domains.json` (company name → domain)
2. Domain extracted from `applyUrl` (Clearbit logo when not a generic platform host)
3. Source branding: Reddit → `reddit.com`, LinkedIn → `linkedin.com`, Internshala → `internshala.com`
4. Client name inferred from Reddit post titles where possible

- Primary image: `https://logo.clearbit.com/{domain}`
- Fallback: Google favicon service (`sz=128`)
- UI fallback: branded initials placeholder (source-colored)

Logos are stored on ingest and recomputed at read time if missing.

## Frontend

Route: `/opportunities` — sidebar **Opportunities** in `DashboardLayout`.

Components: `OpportunityFeed`, `OpportunityCard`, `CompanyLogo`, `TrendingHiringSection`.

## Configuration

```properties
opportunities.enabled=true
opportunities.refresh-interval-ms=900000
opportunities.reddit-hiring-subreddits=VideoEditor_forhire,forhire,...
```

Edit `backend/src/main/resources/curated-opportunities.json` to add partner discovery links without code changes.
