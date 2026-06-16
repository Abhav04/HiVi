import { getApiUrl, deriveUsername } from './auth';
import { signInWithCredentials } from './credentialsLogin';

/**
 * Register via POST /auth/signup then sign in for a JWT.
 */
export async function signUpWithCredentials({ name, email, password, role }) {
  const apiUrl = getApiUrl();
  const username = deriveUsername(email) || email.split('@')[0];

  const res = await fetch(`${apiUrl}/auth/signup`, {
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
  });

  const body = await res.json().catch(() => ({}));

  if (!res.ok) {
    const message = body?.message || body?.error || 'Could not create your account.';
    throw new Error(message);
  }

  const session = await signInWithCredentials(username, password);

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
