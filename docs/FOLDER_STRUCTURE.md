# Folder Structure

## Repository root

```text
HiVi/
├── backend/                 # Spring Boot API
├── frontend/                # React SPA
├── docs/                    # Project documentation (this system)
├── README.md                # GitHub overview
└── .git/                    # Version control
```

---

## Backend (`backend/`)

```text
backend/
├── Dockerfile               # Render production image
├── docker-compose.yml       # Local Postgres/Redis (if used)
├── render.yaml              # Render blueprint (optional)
├── RENDER.md                # Deploy quick reference
├── REDDIT.md                # Reddit feature technical notes
├── pom.xml                  # Maven dependencies
├── mvnw                     # Maven wrapper
└── src/main/
    ├── java/com/oauth/demo/
    │   ├── DemoApplication.java
    │   ├── WebConfig.java              # CORS
    │   ├── config/
    │   │   ├── CacheConfig.java        # Redis + simple cache
    │   │   ├── DatabaseConfig.java     # Render DATABASE_URL
    │   │   ├── DatabaseUrlParser.java
    │   │   ├── OAuth2ClientConfig.java # OAuth session repository
    │   │   └── StartupConfig.java      # Startup diagnostics
    │   ├── controller/
    │   │   ├── AuthController.java
    │   │   ├── FeedController.java
    │   │   ├── HealthController.java
    │   │   ├── HelloController.java
    │   │   ├── LoginRedirectController.java
    │   │   ├── OAuthStatusController.java
    │   │   ├── PostController.java
    │   │   ├── UserController.java
    │   │   └── VideoController.java
    │   ├── dto/
    │   │   ├── LoginRequest.java
    │   │   └── LoginResponse.java
    │   ├── entity/
    │   │   ├── Post.java
    │   │   ├── User.java
    │   │   └── Video.java
    │   ├── jwt/
    │   │   ├── JwtUtils.java
    │   │   └── filter/AuthTokenFilter.java
    │   ├── payload/request/
    │   │   └── SignupRequest.java
    │   ├── repository/
    │   │   ├── PostRepository.java
    │   │   ├── UserRepository.java
    │   │   └── VideoRepository.java
    │   ├── reddit/                     # Reddit trending feature
    │   │   ├── cache/
    │   │   ├── config/
    │   │   ├── controller/RedditController.java
    │   │   ├── dto/
    │   │   ├── exception/
    │   │   ├── scheduler/RedditRefreshScheduler.java
    │   │   └── service/
    │   ├── security/
    │   │   ├── SecurityConfig.java
    │   │   ├── OAuth2LoginSuccessHandler.java
    │   │   ├── OAuth2LoginFailureHandler.java
    │   │   ├── CustomOAuth2UserService.java
    │   │   └── AuthEntryPointJwt.java
    │   └── service/
    │       ├── PostService.java
    │       ├── UserService.java
    │       ├── VideoService.java
    │       ├── EmailService.java
    │       └── CustomUserDetailsService.java
    └── resources/
        ├── application.properties
        ├── application-local.properties
        └── application-prod.properties
```

### Important backend files

| File | Why it matters |
|------|----------------|
| `SecurityConfig.java` | Who can access what; OAuth login |
| `application-prod.properties` | Render behavior |
| `OAuth2LoginSuccessHandler.java` | Post-OAuth redirect + JWT |
| `reddit/service/RedditTrendingService.java` | Cached Reddit feed logic |
| `DatabaseConfig.java` | Fixes localhost DB on Render |

---

## Frontend (`frontend/`)

```text
frontend/
├── public/                  # Static assets (index.html, favicon)
├── vercel.json              # SPA rewrites for Vercel
├── .env.example             # REACT_APP_API_URL template
├── package.json
└── src/
    ├── index.js             # React entry
    ├── App.js               # Routes
    ├── App.css              # Design tokens + global styles
    ├── services/
    │   ├── api.js           # Axios instance
    │   └── redditApi.js     # Reddit trending API
    ├── utils/
    │   ├── auth.js          # User + API URL helpers
    │   ├── oauthErrors.js   # Friendly OAuth error copy
    │   ├── oauthStatus.js   # /oauth/status preflight
    │   └── wakeBackend.js   # /health ping before OAuth
    ├── pages/
    │   ├── Home.js          # Landing page
    │   ├── Login.js
    │   ├── Signup.js
    │   ├── Dashboard.js     # Post-login hub + Reddit feed
    │   ├── Dashboard.css
    │   ├── HomeFeed.js      # Editor discovery (mock)
    │   ├── AuthConnecting.js
    │   ├── OAuthSuccess.js
    │   ├── Auth.css
    │   └── Home.css
    └── components/
        ├── Navbar.js
        ├── Feed.js          # Mock editor list
        ├── PostCard.js      # Editor card (not Reddit)
        ├── PostBox.js       # Search/filter bar
        ├── AuthLoadingScreen.js
        ├── AuthGridPanel.js
        ├── OAuthErrorCard.js
        └── reddit/
            ├── RedditTrendingFeed.js
            ├── RedditPostCard.js
            └── *.css
```

### Important frontend files

| File | Why it matters |
|------|----------------|
| `utils/auth.js` | API URL + localStorage user |
| `App.js` | All routes |
| `Login.js` | OAuth entry + errors |
| `AuthConnecting.js` | Backend wake + OAuth redirect |
| `OAuthSuccess.js` | Token handoff |
| `Dashboard.js` | Reddit section integration |

---

## Documentation (`docs/`)

| File | Contents |
|------|----------|
| `README.md` | Index of all docs |
| `PROJECT_OVERVIEW.md` | What HiVi is |
| `ARCHITECTURE.md` | System design |
| `BACKEND_FLOW.md` | Spring layers |
| `FRONTEND_FLOW.md` | React structure |
| `AUTH_FLOW.md` | JWT + OAuth detail |
| `DATABASE_SCHEMA.md` | Tables/entities |
| `API_REFERENCE.md` | Endpoints |
| `DEPLOYMENT.md` | Vercel + Render |
| `FOLDER_STRUCTURE.md` | This file |
| `CURRENT_FEATURES.md` | Feature status |
| `FUTURE_ROADMAP.md` | Planned work |
| `REDDIT_INTEGRATION_PLAN.md` | Reddit feature spec + status |

---

## Cursor / AI usage

When asking Cursor to change code, reference docs:

```text
@docs/AUTH_FLOW.md fix GitHub OAuth
@docs/API_REFERENCE.md add new endpoint
@docs/FOLDER_STRUCTURE.md where should Reddit code live?
```

Update the relevant doc when architecture changes.

---

## Developer understanding

### Naming confusion to avoid

| Name in UI | Backend entity | Folder |
|------------|----------------|--------|
| `PostCard` | Editor profile (mock) | `components/PostCard.js` |
| `Post` entity | Social post | `entity/Post.java` |
| `RedditPostCard` | Reddit item | `components/reddit/` |

### Where to add new features

| Feature type | Backend | Frontend |
|--------------|---------|----------|
| REST API | `controller/` + `service/` | `services/` |
| External API | New package like `reddit/` | `utils/` or `services/` |
| New page | — | `pages/` + route in `App.js` |
| DB table | `entity/` + `repository/` | — |
