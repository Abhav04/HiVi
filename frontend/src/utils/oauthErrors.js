const ERROR_MAP = {
  google_secret_invalid: {
    type: 'config',
    title: 'Google client secret is wrong',
    message:
      'Google accepted sign-in but the backend could not finish: "The provided client secret is invalid." Your GOOGLE_CLIENT_SECRET in backend/local.env does not match the client ID.',
    action:
      'Google Cloud Console → Credentials → your OAuth client (same GOOGLE_CLIENT_ID as local.env) → copy Client secret → paste into GOOGLE_CLIENT_SECRET in backend/local.env → restart ./run-local.sh',
  },
  github_secret_invalid: {
    type: 'config',
    title: 'GitHub client secret is wrong',
    message:
      'GitHub rejected the client secret when exchanging the authorization code.',
    action:
      'GitHub → OAuth Apps → your app → Client secrets → copy into GITHUB_CLIENT_SECRET in backend/local.env and Render, then restart/redeploy.',
  },
  invalid_client: {
    type: 'config',
    title: 'Sign-in configuration issue',
    message: 'OAuth client ID or secret is invalid on the server.',
    action: 'Check GOOGLE_CLIENT_SECRET / GITHUB_CLIENT_SECRET in backend/local.env match your provider console.',
  },
  github_secret_missing: {
    type: 'config',
    title: 'GitHub secret missing on server',
    message:
      'GITHUB_CLIENT_SECRET is not set on Render. GitHub login cannot work until you add it.',
    action: 'GitHub → Settings → Developer settings → OAuth Apps → your app → copy Client secret → paste into Render as GITHUB_CLIENT_SECRET → redeploy.',
  },
  redirect_uri: {
    type: 'config',
    title: 'Google redirect URI mismatch',
    message:
      'Google blocked sign-in because the redirect URL is not registered for this OAuth client.',
    action:
      'In Google Cloud Console → APIs & Services → Credentials → your OAuth client → Authorized redirect URIs, add the exact URL shown below (local + production if you use both).',
  },
  redirect_uri_mismatch: {
    type: 'config',
    title: 'Google redirect URI mismatch',
    message:
      'Error 400: redirect_uri_mismatch — Google received a callback URL that is not registered for your OAuth client.',
    action:
      'Open Google Cloud Console → Credentials → the OAuth client that matches GOOGLE_CLIENT_ID on Render → Authorized redirect URIs → add the exact production URL below (and localhost if you develop locally).',
  },
  access_denied: {
    type: 'user',
    title: 'Sign-in cancelled',
    message: 'You declined permission or closed the sign-in window.',
    action: 'Click Google or GitHub again when ready.',
  },
  oauth_failed: {
    type: 'user',
    title: 'Sign-in failed',
    message: 'We could not complete sign-in with your provider.',
    action: 'Please try again in a moment.',
  },
  session_expired: {
    type: 'user',
    title: 'Session timed out',
    message:
      'The sign-in session expired while the server was waking up. This is common on free hosting.',
    action: 'Click GitHub or Google again — stay on this tab until sign-in finishes.',
  },
  jwt_error: {
    type: 'config',
    title: 'Server configuration error',
    message: 'Sign-in succeeded with your provider but the server could not issue a login token.',
    action: 'Contact the app owner to fix JWT_SECRET on Render.',
  },
  server_error: {
    type: 'user',
    title: 'Server error',
    message: 'Something went wrong on our server after provider sign-in.',
    action: 'Try again in a moment.',
  },
  server_offline: {
    type: 'user',
    title: 'Server offline',
    message: 'The server is temporarily taking too long to respond. Please try again in a few moments.',
    action: 'Try signing in again.',
  },
  invalid_grant: {
    type: 'user',
    title: 'Sign-in link expired',
    message:
      'The authorization code expired or did not match. This often happens if the server restarted during sign-in.',
    action: 'Click GitHub or Google again immediately and complete sign-in without delay.',
  },
};

export function parseOAuthError(raw) {
  if (!raw) return null;

  let decoded = raw;
  try {
    decoded = decodeURIComponent(raw);
  } catch {
    decoded = raw;
  }

  const lower = decoded.toLowerCase();

  if (lower.includes('google_secret_invalid')) {
    return ERROR_MAP.google_secret_invalid;
  }
  if (lower.includes('github_secret_invalid')) {
    return ERROR_MAP.github_secret_invalid;
  }
  if (lower.includes('invalid_client') || lower.includes('client secret')) {
    return ERROR_MAP.invalid_client;
  }
  if (lower.includes('redirect_uri_mismatch')) {
    return ERROR_MAP.redirect_uri_mismatch;
  }
  if (lower.includes('redirect_uri')) {
    return ERROR_MAP.redirect_uri;
  }
  if (lower.includes('access_denied') || lower.includes('denied')) {
    return ERROR_MAP.access_denied;
  }
  if (lower.includes('session_expired') || lower.includes('authorization_request')) {
    return ERROR_MAP.session_expired;
  }
  if (lower.includes('jwt_error')) {
    return ERROR_MAP.jwt_error;
  }
  if (lower.includes('server_error')) {
    return ERROR_MAP.server_error;
  }
  if (lower.includes('invalid_grant')) {
    return ERROR_MAP.invalid_grant;
  }
  if (lower.includes('github_secret_missing')) {
    return ERROR_MAP.github_secret_missing;
  }
  if (ERROR_MAP[lower]) {
    return ERROR_MAP[lower];
  }

  return {
    type: 'user',
    title: 'Sign-in failed',
    message: 'Something went wrong connecting to your account.',
    action: 'Try again or use email sign-in.',
  };
}
