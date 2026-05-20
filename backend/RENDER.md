# Deploying HiVi Backend on Render

## 1. Create a PostgreSQL database

1. In Render Dashboard → **New** → **PostgreSQL**
2. Name it `hivi-db` (or any name)
3. After creation, copy the **Internal Database URL** (starts with `postgresql://`)

## 2. Create a Web Service (Docker)

1. **New** → **Web Service** → connect your GitHub repo
2. Settings:
   - **Root Directory**: `backend`
   - **Runtime**: Docker
   - **Instance type**: Free (or paid)

## 3. Environment variables (required)

| Key | Value |
|-----|--------|
| `DATABASE_URL` | **Required.** Must start with `postgresql://` (use **Internal** URL from Render, not localhost) |
| `DB_USERNAME` / `DB_PASSWORD` | Optional fallback if `DATABASE_URL` uses JDBC format without embedded credentials |
| `SPRING_PROFILES_ACTIVE` | `prod` (also set in Dockerfile; optional to repeat here) |
| `APP_FRONTEND_URL` | `https://hi-vi.vercel.app` |
| `JWT_SECRET` | A long random secret string |
| `GOOGLE_CLIENT_ID` | From Google Cloud Console |
| `GOOGLE_CLIENT_SECRET` | From Google Cloud Console |
| `GITHUB_CLIENT_ID` | From GitHub OAuth App |
| `GITHUB_CLIENT_SECRET` | From GitHub OAuth App |

`PORT` is set automatically by Render — do not override it.

## 4. Link database to web service

In your Web Service → **Environment** → **Add from Database** → select your Postgres.  
This sets `DATABASE_URL` automatically.

## 5. OAuth redirect URLs

Update Google/GitHub OAuth apps with your Render backend URL (exact match required):

- Google redirect: `https://hivi-idam.onrender.com/login/oauth2/code/google`
- GitHub redirect: `https://hivi-idam.onrender.com/login/oauth2/code/github`

Render sets `RENDER_EXTERNAL_URL` automatically — the prod profile uses it for OAuth redirects.

## 6. Vercel frontend

Set in Vercel environment variables:

```
REACT_APP_API_URL=https://YOUR-SERVICE.onrender.com
```

## Troubleshooting

| Error | Fix |
|-------|-----|
| `Connection to localhost:5432 refused` | Set `DATABASE_URL` and `SPRING_PROFILES_ACTIVE=prod` |
| `No open ports detected` | App must use `server.port=${PORT}` (already configured) |
| Redis connection errors | Prod profile disables Redis; ensure `SPRING_PROFILES_ACTIVE=prod` |
