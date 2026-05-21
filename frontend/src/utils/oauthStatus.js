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

export function getOAuthBlockers(status, provider) {
  if (!status) return [];

  const blockers = [];
  if (provider === 'github') {
    if (!status.githubOAuthConfigured) {
      blockers.push('GitHub Client ID is not configured on the server (GITHUB_CLIENT_ID).');
    } else if (!status.githubClientSecretSet) {
      blockers.push(
        'GitHub Client Secret is missing on Render. Add GITHUB_CLIENT_SECRET in your Render web service environment variables, then redeploy.'
      );
    } else if (!status.readyForGithubLogin) {
      blockers.push('GitHub OAuth is not ready on the server.');
    }
  }
  if (provider === 'google') {
    if (!status.googleOAuthConfigured) {
      blockers.push('Google Client ID is not configured on the server (GOOGLE_CLIENT_ID).');
    } else if (!status.googleClientSecretSet) {
      blockers.push(
        'Google Client Secret is missing on Render. Add GOOGLE_CLIENT_SECRET in your Render web service environment variables, then redeploy.'
      );
    } else if (!status.readyForGoogleLogin) {
      blockers.push('Google OAuth is not ready on the server.');
    }
  }
  if (!status.jwtSigningKeyValid) {
    blockers.push('JWT signing key is invalid. Set JWT_SECRET on Render.');
  }
  return blockers;
}
