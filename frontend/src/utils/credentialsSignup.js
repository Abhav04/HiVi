import { getApiUrl, deriveUsername } from './auth';
import { signInWithCredentials } from './credentialsLogin';

/**
 * Register via POST /auth/signup then sign in for a JWT.
 */
export async function signUpWithCredentials({ name, email, password, role }) {
  const apiUrl = getApiUrl();
  const username = deriveUsername(email) || email.split('@')[0];
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), 20000);

  let res;
  try {
    res = await fetch(`${apiUrl}/auth/signup`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username,
        email,
        password,
        displayName: name?.trim() || undefined,
        role: role || 'client',
      }),
      cache: 'no-store',
      signal: controller.signal,
    });
  } catch (err) {
    if (err?.name === 'AbortError') {
      throw new Error('Sign up timed out. Please try again in a few seconds.');
    }
    throw new Error('Unable to reach server. Check your connection and try again.');
  } finally {
    clearTimeout(timeoutId);
  }

  const body = await res.json().catch(() => ({}));

  if (!res.ok) {
    const message =
      body?.message
      || body?.detail
      || body?.error
      || 'Could not create your account.';
    throw new Error(message);
  }

  let session;
  try {
    session = await signInWithCredentials(username, password);
  } catch (err) {
    throw new Error(
      err?.message
        ? `Account created, but automatic sign-in failed: ${err.message}. Please sign in from the login page.`
        : 'Account created, but automatic sign-in failed. Please sign in from the login page.'
    );
  }

  return {
    ...session,
    user: {
      name: name?.trim() || email.split('@')[0],
      email,
      username,
      role: role || 'client',
      provider: 'local',
      projects: [],
    },
  };
};
