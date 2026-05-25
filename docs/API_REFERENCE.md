# API Reference

**Base URL (production):** `https://hivi-idam.onrender.com`  
**Base URL (local):** `http://localhost:8080`

**Auth header (when required):** `Authorization: Bearer <JWT>`

---

## Health & diagnostics

### `GET /health`

| | |
|--|--|
| **Auth** | No |
| **Response** | `{ "status": "UP" }` |
| **Purpose** | Render health check, frontend wake-before-OAuth |

### `GET /oauth/status`

| | |
|--|--|
| **Auth** | No |
| **Response** | OAuth config diagnostics (no secrets) |
| **Purpose** | Verify deploy: secrets set, redirect URIs, JWT valid |

Example fields: `githubClientSecretSet`, `readyForGithubLogin`, `issues`, `githubRedirectUri`

---

## Authentication (`/auth`)

### `POST /auth/signup`

| | |
|--|--|
| **Auth** | No |
| **Body** | `SignupRequest` JSON (username, email, password, etc.) |
| **Response** | `{ "message": "User registered successfully" }` |
| **Purpose** | Register local user |

### `POST /auth/signin`

| | |
|--|--|
| **Auth** | No |
| **Body** | `{ "username": "...", "password": "..." }` |
| **Response** | `LoginResponse`: username, roles, **JWT token** |
| **Purpose** | Email/password login |

### `POST /auth/verify`

| | |
|--|--|
| **Auth** | No |
| **Query** | `email`, `code` |
| **Response** | Text message |
| **Purpose** | Verify email via Redis-stored code (requires Redis) |

---

## OAuth (browser redirects, not JSON API)

### `GET /oauth2/authorization/{registrationId}`

| | |
|--|--|
| **Auth** | No |
| **Params** | `registrationId`: `google` or `github` |
| **Response** | 302 redirect to provider |
| **Purpose** | Start OAuth flow |

### `GET /login/oauth2/code/{registrationId}`

| | |
|--|--|
| **Auth** | No |
| **Response** | 302 redirect to frontend `/oauth-success` or `/login?error=` |
| **Purpose** | OAuth callback (handled by Spring Security) |

### `GET /login`

| | |
|--|--|
| **Auth** | No |
| **Query** | Optional `error` |
| **Response** | 302 to frontend `/login?error=...` |
| **Purpose** | Bridge backend login errors to SPA |

---

## Users (`/user`)

### `GET /user/profile`

| | |
|--|--|
| **Auth** | **Yes** (JWT) |
| **Response** | User profile object |
| **Purpose** | Authenticated profile data |

### `GET /user/me`

| | |
|--|--|
| **Auth** | **Yes** (JWT) |
| **Response** | Current user info |
| **Purpose** | Session identity check |

---

## Posts (`/posts`)

### `GET /posts`

| | |
|--|--|
| **Auth** | No |
| **Response** | `List<Post>` JSON |
| **Purpose** | List all posts (cached via `@Cacheable("posts")`) |

### `POST /posts`

| | |
|--|--|
| **Auth** | Permitted without JWT in SecurityConfig, but uses `Authentication` |
| **Content-Type** | `multipart/form-data` |
| **Params** | `content` (required), `file` (optional) |
| **Response** | Created `Post` |
| **Purpose** | Create post (needs valid JWT in practice) |

---

## Feed (`/feed`)

### `GET /feed`

| | |
|--|--|
| **Auth** | **Yes** (JWT) |
| **Response** | `List<Post>` (same as getAllPosts today) |
| **Purpose** | Authenticated feed endpoint |

---

## Videos (`/videos`)

### `GET /videos`

| | |
|--|--|
| **Auth** | **Yes** |
| **Response** | Video list |
| **Purpose** | List videos |

### `POST /videos/upload`

| | |
|--|--|
| **Auth** | **Yes** |
| **Purpose** | Upload video metadata/file |

---

## Reddit (`/api/reddit`)

### `GET /api/reddit/trending`

| | |
|--|--|
| **Auth** | No |
| **Query** | |

| Param | Default | Description |
|-------|---------|-------------|
| `subreddit` | all | Filter e.g. `videoediting` or `r/videoediting` |
| `page` | 0 | Page index |
| `limit` | 20 | Page size (max 50) |

| | |
|--|--|
| **Response** | `RedditTrendingResponse` |

```json
{
  "posts": [ { "id", "title", "subreddit", "author", "upvotes", "commentCount", "thumbnailUrl", "redditUrl", "timeAgo", ... } ],
  "subreddits": ["r/videoediting", "..."],
  "activeSubreddit": "all",
  "page": 0,
  "limit": 12,
  "total": 48,
  "hasMore": true,
  "cachedAt": "2026-05-21T18:00:00Z",
  "stale": false,
  "message": null
}
```

| **Purpose** | Dashboard “Trending from Reddit” (served from cache, not live Reddit) |

---

## Misc

### `GET /hello`

| | |
|--|--|
| **Auth** | **Yes** |
| **Purpose** | Smoke test authenticated endpoint |

---

## Error responses

| Situation | Typical response |
|-----------|------------------|
| Missing JWT on protected route | 401 JSON via `AuthEntryPointJwt` |
| Invalid JWT | 401, filter logs error |
| Reddit upstream failure | 502 or empty cache message in body |

---

## Developer understanding

### Why `/posts` is public but `/feed` is not?

Historical inconsistency. Frontend does not use either yet. **Recommend:** align both to same auth policy when wiring UI.

### API versioning

No `/v1` prefix today. Add when breaking changes ship.

### Rate limiting

Not implemented at API gateway. Reddit rate limits handled inside scheduler only.

### OpenAPI / Swagger

Not configured. Adding `springdoc-openapi` would auto-generate docs from controllers.
