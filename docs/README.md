# HiVi Project Documentation

This folder is the **permanent context system** for the HiVi project. Use it when working with Cursor AI or onboarding new developers.

## How to use these docs

| When you need to… | Read |
|-------------------|------|
| Understand what HiVi is | [PROJECT_OVERVIEW.md](./PROJECT_OVERVIEW.md) |
| See the big picture | [ARCHITECTURE.md](./ARCHITECTURE.md) |
| Trace backend code | [BACKEND_FLOW.md](./BACKEND_FLOW.md) |
| Trace frontend code | [FRONTEND_FLOW.md](./FRONTEND_FLOW.md) |
| Fix login / OAuth | [AUTH_FLOW.md](./AUTH_FLOW.md) |
| Understand database tables | [DATABASE_SCHEMA.md](./DATABASE_SCHEMA.md) |
| Look up an API | [API_REFERENCE.md](./API_REFERENCE.md) |
| Deploy or configure env vars | [DEPLOYMENT.md](./DEPLOYMENT.md) |
| Find a file or folder | [FOLDER_STRUCTURE.md](./FOLDER_STRUCTURE.md) |
| See what's built vs planned | [CURRENT_FEATURES.md](./CURRENT_FEATURES.md) |
| Plan next work | [FUTURE_ROADMAP.md](./FUTURE_ROADMAP.md) |
| Reddit trending feed | [REDDIT_INTEGRATION.md](./REDDIT_INTEGRATION.md) |
| Community social platform | [COMMUNITY_PLATFORM.md](./COMMUNITY_PLATFORM.md) |

## Production URLs

| Service | URL |
|---------|-----|
| Frontend (Vercel) | https://hi-vi.vercel.app |
| Backend (Render) | https://hivi-idam.onrender.com |
| Health check | https://hivi-idam.onrender.com/health |
| OAuth diagnostics | https://hivi-idam.onrender.com/oauth/status |

## Quick architecture

```text
React (Vercel)  →  REST API (Render)  →  PostgreSQL
                         ↓
                    Redis (local) / in-memory cache (prod)
                         ↓
                    Reddit JSON API (scheduled, cached)
```

## Keeping docs updated (required)

Documentation is the **project memory**. It must stay in sync with the code.

### Who updates docs?

- **You** — when merging features manually  
- **Cursor AI** — same session/task as the code change (see `AGENTS.md` and `.cursor/rules/hivi-documentation.mdc`)

### When to update (same commit / PR)

| Change type | Update these files |
|-------------|-------------------|
| New or changed REST endpoint | `API_REFERENCE.md` |
| OAuth, JWT, login, CORS | `AUTH_FLOW.md`, possibly `DEPLOYMENT.md` |
| Database entity or table | `DATABASE_SCHEMA.md` |
| Backend structure or security | `BACKEND_FLOW.md`, `FOLDER_STRUCTURE.md` |
| React page, route, or major component | `FRONTEND_FLOW.md`, `FOLDER_STRUCTURE.md` |
| Architecture, caching, third-party API | `ARCHITECTURE.md` |
| Deploy or environment variable | `DEPLOYMENT.md` |
| Feature completed or behavior change | `CURRENT_FEATURES.md` |
| Reddit feed behavior | `REDDIT_INTEGRATION_PLAN.md`, `backend/REDDIT.md` |
| New planned work (no code yet) | `FUTURE_ROADMAP.md` only |

### Quick checklist before closing a task

- [ ] Code change has a matching doc update (or “no doc impact” is intentional)
- [ ] `CURRENT_FEATURES.md` status reflects reality (✅ / ⚠️ / ❌)
- [ ] `API_REFERENCE.md` lists any new endpoints
- [ ] Diagrams in `ARCHITECTURE.md` / `AUTH_FLOW.md` still match the flow

### Do not

- Add a second doc for the same topic — extend the existing file  
- Leave outdated env vars in `DEPLOYMENT.md`  
- Document secrets or real API keys  

### Cursor rules (auto-reminder)

| Rule | Scope |
|------|--------|
| `.cursor/rules/hivi-documentation.mdc` | Always on — doc maintenance |
| `.cursor/rules/hivi-backend.mdc` | When editing `backend/**` |
| `.cursor/rules/hivi-frontend.mdc` | When editing `frontend/**` |
