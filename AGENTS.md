# HiVi — Agent / Cursor Context

When modifying this repository, **read the docs first**:

📁 **[docs/README.md](./docs/README.md)** — full documentation index

## Quick reference

| Topic | Document |
|-------|----------|
| System design | [docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md) |
| Backend code map | [docs/BACKEND_FLOW.md](./docs/BACKEND_FLOW.md) |
| Frontend code map | [docs/FRONTEND_FLOW.md](./docs/FRONTEND_FLOW.md) |
| Auth / OAuth | [docs/AUTH_FLOW.md](./docs/AUTH_FLOW.md) |
| All APIs | [docs/API_REFERENCE.md](./docs/API_REFERENCE.md) |
| Deploy & env vars | [docs/DEPLOYMENT.md](./docs/DEPLOYMENT.md) |
| What's implemented | [docs/CURRENT_FEATURES.md](./docs/CURRENT_FEATURES.md) |

## Production URLs

- Frontend: `https://hi-vi.vercel.app`
- Backend: `https://hivi-idam.onrender.com`

## Rules for AI-assisted changes

1. **Always update documentation in the same task as code changes** — see [docs/README.md § Keeping docs updated](./docs/README.md#keeping-docs-updated-required). Cursor rule: `.cursor/rules/hivi-documentation.mdc` (`alwaysApply: true`).
2. **Read relevant docs before editing** — do not guess architecture.
3. **Do not commit secrets** — use environment variables only.
4. **Match existing patterns** — see [docs/FOLDER_STRUCTURE.md](./docs/FOLDER_STRUCTURE.md).
5. **Minimal scope** — avoid unrelated refactors in the same change.
6. **Ship checklist:** `CURRENT_FEATURES.md` + `API_REFERENCE.md` (if APIs) + area-specific doc (AUTH, DEPLOYMENT, etc.).

### Doc update map (quick)

| Code area | Docs to touch |
|-----------|----------------|
| `backend/.../controller/` | `API_REFERENCE.md`, `CURRENT_FEATURES.md` |
| `backend/.../security/` | `AUTH_FLOW.md`, `ARCHITECTURE.md` |
| `backend/.../entity/` | `DATABASE_SCHEMA.md` |
| `frontend/src/pages/` | `FRONTEND_FLOW.md`, `CURRENT_FEATURES.md` |
| `frontend/src/App.js` routes | `FRONTEND_FLOW.md` |
| `render.yaml`, Dockerfile, env | `DEPLOYMENT.md` |

## Common pitfalls

- `PostCard` = mock **editor** UI, not `Post` entity or Reddit posts
- GitHub OAuth requires `GITHUB_CLIENT_SECRET` on Render
- Prod uses in-memory cache, not Redis
- Login page email flow is mock; real JWT is via OAuth or `/auth/signin`
