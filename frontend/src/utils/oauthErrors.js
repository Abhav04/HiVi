const ERROR_MAP = {
  invalid_client: {
    type: 'config',
    title: 'Sign-in configuration issue',
    message:
      'Google or GitHub credentials on the server need to be updated. If you are the app owner, check GOOGLE_CLIENT_SECRET and GITHUB_CLIENT_SECRET in Render.',
    action: 'Try again later or use email sign-in.',
  },
  redirect_uri: {
    type: 'config',
    title: 'Redirect mismatch',
    message: 'The OAuth app redirect URL does not match the server. This is a developer configuration issue.',
    action: 'Use email sign-in for now.',
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

  if (lower.includes('invalid_client') || lower.includes('client secret')) {
    return ERROR_MAP.invalid_client;
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
