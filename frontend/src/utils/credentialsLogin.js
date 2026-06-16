import { getApiUrl, saveUser, nameFromEmail, deriveUsername, setToken } from './auth';

/** Map login form email to backend username (demo users use username, not email). */
export function resolveLoginUsername(emailOrUsername) {
  const value = (emailOrUsername || '').trim();
  if (!value) return value;
  if (value.endsWith('@demo.hivi.local')) {
    return value.split('@')[0];
  }
  if (value.includes('@')) {
    return deriveUsername(value) || value.split('@')[0];
  }
  return value;
}

/**
 * POST /auth/signin — returns JWT for demo users and local accounts.
 */
export async function signInWithCredentials(emailOrUsername, password) {
  const apiUrl = getApiUrl();
  const username = resolveLoginUsername(emailOrUsername);

  const res = await fetch(`${apiUrl}/auth/signin`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ username, password }),
    cache: 'no-store',
  });

  const body = await res.json().catch(() => ({}));

  if (!res.ok) {
    const message =
      body?.message || body?.error || 'Invalid email/username or password.';
    throw new Error(message);
  }

  const token = body.jwtToken || body.token;
  if (!token) {
    throw new Error('Sign-in succeeded but no token was returned.');
  }

  setToken(token);

  const displayEmail = emailOrUsername.includes('@') ? emailOrUsername : username;
  const roleRaw = Array.isArray(body.roles) && body.roles[0] ? body.roles[0] : 'client';
  const role = roleRaw.replace(/^ROLE_/i, '').toLowerCase();

  saveUser({
    name: nameFromEmail(displayEmail),
    email: displayEmail,
    username: body.username || deriveUsername(displayEmail),
    role,
    provider: 'local',
    projects: [],
  });

  return { token, username: body.username || username, role };
}
