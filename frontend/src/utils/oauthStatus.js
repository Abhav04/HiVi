import { getApiUrl } from './auth';

/**
 * Fetches backend OAuth readiness (no secrets exposed).
 */
export async function fetchOAuthStatus(apiUrl = getApiUrl()) {
  const res = await fetch(`${apiUrl}/oauth/status`, {
    method: 'GET',
    cache: 'no-store',
  });
  if (!res.ok) {
    throw new Error(`OAuth status check failed (${res.status})`);
  }
  return res.json();
}

function isLocalApi(apiUrl) {
  try {
    const host = new URL(apiUrl).hostname;
    return host === 'localhost' || host === '127.0.0.1';
  } catch {
    return apiUrl.includes('localhost') || apiUrl.includes('127.0.0.1');
  }
}

export function getOAuthBlockers(status, provider, apiUrl = getApiUrl()) {
  if (!status) return [];

  const local = isLocalApi(apiUrl);

  if (status.oauthPlaceholdersDetected) {
    return [
      local
        ? 'backend/local.env still has example OAuth values (your-google-client-id). Copy the real GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET from Render → your web service → Environment, then restart ./run-local.sh.'
        : 'OAuth credentials on the server are invalid placeholders. Update environment variables and redeploy.',
    ];
  }
  const secretHint = local
    ? 'Add GITHUB_CLIENT_SECRET to backend/local.env (copy from Render), then restart the backend.'
    : 'Add GITHUB_CLIENT_SECRET in your Render web service environment variables, then redeploy.';
  const googleSecretHint = local
    ? 'Add GOOGLE_CLIENT_SECRET to backend/local.env (copy from Render), then restart the backend.'
    : 'Add GOOGLE_CLIENT_SECRET in your Render web service environment variables, then redeploy.';
  const idHint = local
    ? 'Copy OAuth keys from Render into backend/local.env (see local.env.example), then restart ./run-local.sh'
    : 'Set GOOGLE_CLIENT_ID / GITHUB_CLIENT_ID on the server.';

  const blockers = [];
  if (provider === 'github') {
    if (!status.githubOAuthConfigured) {
      blockers.push(`GitHub Client ID is not configured (GITHUB_CLIENT_ID). ${idHint}`);
    } else if (!status.githubClientSecretSet) {
      blockers.push(`GitHub Client Secret is missing. ${secretHint}`);
    } else if (!status.readyForGithubLogin) {
      blockers.push('GitHub OAuth is not ready on the server.');
    }
  }
  if (provider === 'google') {
    if (!status.googleOAuthConfigured) {
      blockers.push(`Google Client ID is not configured (GOOGLE_CLIENT_ID). ${idHint}`);
    } else if (!status.googleClientSecretSet) {
      blockers.push(`Google Client Secret is missing. ${googleSecretHint}`);
    } else if (!status.readyForGoogleLogin) {
      blockers.push('Google OAuth is not ready on the server.');
    }
  }
  if (!status.jwtSigningKeyValid) {
    blockers.push(
      local
        ? 'JWT signing key is invalid. Set JWT_SECRET in backend/local.env (32+ characters).'
        : 'JWT signing key is invalid. Set JWT_SECRET on Render.'
    );
  }
  return blockers;
}

export function getOAuthConfigAction(apiUrl = getApiUrl()) {
  return isLocalApi(apiUrl)
    ? 'Copy backend/local.env.example to backend/local.env, paste keys from Render, restart the backend.'
    : 'Fix Render environment variables and redeploy the backend, then try again.';
}
