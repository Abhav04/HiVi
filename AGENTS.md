# HiVi — Agent / Cursor Context

When modifying this repository, **read the backend docs first**:

📁 **[docs/Backend_Flow.md](./docs/Backend_Flow.md)** — architecture, auth, APIs, database, feature modules, deployment

## Production URLs

- Frontend: `https://hi-vi.vercel.app`
- Backend: `https://hivi-idam.onrender.com`

## Rules for AI-assisted changes

1. **Update `docs/Backend_Flow.md` in the same task as backend code changes** — Cursor rule: `.cursor/rules/hivi-documentation.mdc` (`alwaysApply: true`).
2. **Read the doc before editing** — do not guess architecture.
3. **Do not commit secrets** — use environment variables only.
4. **Minimal scope** — avoid unrelated refactors in the same change.

## Common pitfalls

- `PostCard` = mock **editor** UI, not `Post` entity or Reddit posts
- GitHub OAuth requires `GITHUB_CLIENT_SECRET` on Render
- Prod uses in-memory cache, not Redis
- Community public API: `/api/public/community/feed` (not only `/api/community/feed`)
- Prod uploads use `/tmp/uploads/community` on Render
