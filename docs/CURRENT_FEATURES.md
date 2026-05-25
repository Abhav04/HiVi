# Current Features

**Living document** — update whenever a feature ships, breaks, or changes behavior.  
Cursor and contributors should edit this in the **same commit** as the code change.

_Last reviewed: dedicated Reddit trends page + HQ thumbnails_

**Legend:** ✅ Done · ⚠️ Partial · ❌ Not started

---

## Platform & infrastructure

| Feature | Status | Notes |
|---------|--------|-------|
| Monorepo (frontend + backend) | ✅ | `HiVi/` |
| Vercel frontend deploy | ✅ | https://hi-vi.vercel.app |
| Render backend deploy | ✅ | Docker, `prod` profile |
| PostgreSQL on Render | ✅ | `DATABASE_URL` |
| Health endpoint | ✅ | `/health` |
| OAuth diagnostics endpoint | ✅ | `/oauth/status` |
| Documentation system | ✅ | `/docs` folder |

---

## Authentication & security

| Feature | Status | Notes |
|---------|--------|-------|
| JWT generation/validation | ✅ | `JwtUtils`, `AuthTokenFilter` |
| BCrypt passwords | ✅ | `UserService` |
| Google OAuth | ⚠️ | Backend ready; needs correct secrets |
| GitHub OAuth | ⚠️ | Requires `GITHUB_CLIENT_SECRET` on Render |
| OAuth loading UX | ✅ | `AuthConnecting`, `AuthLoadingScreen` |
| OAuth error cards | ✅ | `OAuthErrorCard`, `oauthErrors.js` |
| Backend wake before OAuth | ✅ | `wakeBackend.js` |
| Email login UI | ⚠️ | Mock only; does not call `/auth/signin` |
| Email signup API | ✅ | `POST /auth/signup` |
| Email verification (Redis) | ⚠️ | Dev only; Redis disabled in prod |
| Protected route guards (React) | ❌ | Dashboard accessible without login |
| Role-based UI | ❌ | Role stored but not enforced in UI |

---

## User & profile

| Feature | Status | Notes |
|---------|--------|-------|
| User entity + DB | ✅ | `users` table |
| OAuth auto-registration | ✅ | `OAuth2LoginSuccessHandler` |
| `GET /user/me`, `/user/profile` | ✅ | JWT required |
| Dashboard user display | ✅ | From `localStorage` |
| Real profile sync from API | ❌ | Not calling `/user/me` |

---

## Content & feed

| Feature | Status | Notes |
|---------|--------|-------|
| Post entity + CRUD API | ✅ | `/posts` |
| Post list caching | ✅ | `@Cacheable("posts")` |
| Authenticated `/feed` | ✅ | Same data as posts |
| **Trends from Reddit (dedicated page)** | ✅ | `/reddit-trends`, sidebar featured nav; removed from Overview |
| Reddit trending API | ✅ | `/api/reddit/trending` |
| Reddit subreddit filters | ✅ | Tabs in `RedditTrendingFeed` |
| Reddit trending score ranking | ✅ | Upvotes, comments, recency, hiring boost |
| Reddit “Trending Now” hero | ✅ | Top post featured on `/reddit-trends` |
| Reddit hiring opportunities | ✅ | Auto-detect + horizontal hiring section |
| Reddit HD thumbnails | ✅ | Multi-source resolver + image fallbacks |

## Community platform (social)

| Feature | Status | Notes |
|---------|--------|-------|
| Community Feed page | ✅ | `/community`, sidebar nav |
| Create posts (text/image/video/portfolio) | ✅ | Multipart upload, drafts |
| Like / bookmark / comment | ✅ | JWT required |
| Creator profiles | ✅ | `/community/creator/:username` |
| Follow creators | ✅ | `POST /api/community/users/{id}/follow` |
| Trending creators section | ✅ | In feed response |
| Feed ranking (engagement) | ✅ | `CommunityFeedRanker` |
| Local media storage | ✅ | `uploads/community/` — migrate to S3/R2 for prod |
| Real-time notifications | ❌ | Planned |
| Cloud video transcoding | ❌ | Planned |
| Reddit infinite scroll | ✅ | IntersectionObserver |
| Editor discovery feed | ⚠️ | UI only; mock data in `Feed.js` |
| Post project (client) | ❌ | CTA links to `/feed` only |
| Video upload API | ⚠️ | Scaffold exists |

---

## UI / UX

| Feature | Status | Notes |
|---------|--------|-------|
| Marketing homepage | ✅ | Cinematic dark/gold design |
| Login / Signup pages | ✅ | `Auth.css` |
| Dashboard overview | ✅ | Stats mock; no embedded Reddit feed |
| Dashboard sidebar tabs | ⚠️ | Only overview implemented |
| Navbar navigation | ✅ | Home, discover, etc. |
| Purple neon grid (auth loading) | ✅ | Canvas `AuthGridPanel` |
| Mobile responsive (partial) | ⚠️ | Reddit + dashboard have breakpoints |

---

## Integrations

| Integration | Status | Notes |
|-------------|--------|-------|
| Reddit public JSON API | ✅ | Scheduled fetch + cache |
| Google OAuth provider | ✅ | Spring OAuth2 Client |
| GitHub OAuth provider | ✅ | + email fetch service |
| Redis (local) | ✅ | Cache + verify codes |
| Redis (production) | ❌ | Disabled in `prod` profile |
| Email SMTP (prod) | ❌ | Local Mailhog only |

---

## Developer tooling

| Item | Status |
|------|--------|
| Maven wrapper | ✅ |
| ESLint (CI strict on Vercel) | ✅ |
| GitHub Actions CI | ❌ |
| OpenAPI / Swagger | ❌ |
| Integration tests for OAuth | ❌ |

---

## Known gaps (intentional or pending)

1. **Frontend email auth** not wired to backend JWT flow  
2. **Editor marketplace** UI uses mock data — no `Editor` entity  
3. **Projects** on dashboard from `localStorage`, not API  
4. **`GITHUB_CLIENT_SECRET`** must be set manually on Render  
5. **No route protection** on `/dashboard`  
6. **`POST /posts`** security mismatch (permitAll vs `Authentication`)  

---

## Developer understanding

Use this file as a **truth table** when planning Cursor tasks. If you implement a feature, flip its status here in the same commit.

### How to classify status

| Status | Meaning |
|--------|---------|
| ✅ | Works in production or complete code path |
| ⚠️ | Code exists but incomplete, mock, or env-dependent |
| ❌ | Not implemented |
