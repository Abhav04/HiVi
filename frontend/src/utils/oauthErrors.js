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
  if (lower.includes('redirect_uri') || lower.includes('redirect')) {
    return ERROR_MAP.redirect_uri;
  }
  if (lower.includes('access_denied') || lower.includes('denied')) {
    return ERROR_MAP.access_denied;
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
