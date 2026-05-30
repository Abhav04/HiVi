const USER_KEY = 'hivi_user';
const TOKEN_KEY = 'token';

const PRODUCTION_API = 'https://hivi-idam.onrender.com';

const isJwtShape = (token) =>
  typeof token === 'string' && token.split('.').length === 3 && token.length > 20;

/** Normalize JWT from OAuth query strings (+ may become space). */
export const normalizeToken = (raw) => {
  if (!raw || typeof raw !== 'string') return null;
  const trimmed = raw.trim().replace(/ /g, '+');
  return isJwtShape(trimmed) ? trimmed : null;
};

export const getToken = () => normalizeToken(localStorage.getItem(TOKEN_KEY));

export const decodeJwtPayload = (token) => {
  try {
    const part = token.split('.')[1];
    if (!part) return null;
    const json = atob(part.replace(/-/g, '+').replace(/_/g, '/'));
    return JSON.parse(json);
  } catch {
    return null;
  }
};

export const isTokenExpired = (token = getToken()) => {
  const payload = decodeJwtPayload(token);
  if (!payload?.exp) return false;
  return payload.exp * 1000 <= Date.now() + 5000;
};

export const setToken = (raw) => {
  const token = normalizeToken(raw);
  if (token) {
    localStorage.setItem(TOKEN_KEY, token);
    return token;
  }
  localStorage.removeItem(TOKEN_KEY);
  return null;
};

export const clearInvalidToken = () => {
  localStorage.removeItem(TOKEN_KEY);
};

/**
 * Drop orphaned profile data or expired tokens so UI stays consistent.
 */
export const reconcileAuthStorage = () => {
  const token = getToken();
  const user = getUser();
  if (token && isTokenExpired(token)) {
    localStorage.removeItem(TOKEN_KEY);
    return { user: null, authenticated: false };
  }
  if (!token && user) {
    localStorage.removeItem(USER_KEY);
    return { user: null, authenticated: false };
  }
  if (token && !user) {
    localStorage.removeItem(TOKEN_KEY);
    return { user: null, authenticated: false };
  }
  return { user: token ? user : null, authenticated: !!(token && user) };
};

/** Headers for public read endpoints — never sends a broken token */
export const getPublicHeaders = () => ({
  'Content-Type': 'application/json',
});

/** Headers for authenticated actions — only sends token if it looks like a JWT */
export const getAuthHeaders = (json = true) => {
  const headers = {};
  if (json) headers['Content-Type'] = 'application/json';
  const token = getToken();
  if (isJwtShape(token)) {
    headers.Authorization = `Bearer ${token}`;
  }
  return headers;
};

export const isLoggedIn = () => {
  const token = getToken();
  if (!token || !getUser() || isTokenExpired(token)) return false;
  return true;
};

export const mediaFullUrl = (path) => {
  if (!path) return null;
  if (path.startsWith('http')) return path;
  return `${getApiUrl()}${path.startsWith('/') ? path : `/${path}`}`;
};

export const getApiUrl = () => {
  const fromEnv = process.env.REACT_APP_API_URL?.replace(/\/$/, '');
  if (fromEnv) return fromEnv;
  if (process.env.NODE_ENV === 'production') return PRODUCTION_API;
  return 'http://localhost:8080';
};

/** Backend used for OAuth redirects (defaults to same as API). */
export const getOAuthApiUrl = () => {
  const override = process.env.REACT_APP_OAUTH_API_URL?.replace(/\/$/, '');
  if (override) return override;
  return getApiUrl();
};

export const getInitials = (name = '') => {
  const parts = name.trim().split(/\s+/).filter(Boolean);
  if (parts.length >= 2) return `${parts[0][0]}${parts[parts.length - 1][0]}`.toUpperCase();
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase();
  return '??';
};

export const nameFromEmail = (email) => {
  const local = email.split('@')[0] || 'user';
  return local
    .replace(/[._-]+/g, ' ')
    .split(' ')
    .map((w) => w.charAt(0).toUpperCase() + w.slice(1))
    .join(' ');
};

export const getUser = () => {
  try {
    const raw = localStorage.getItem(USER_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
};

export const saveUser = (user) => {
  const existing = getUser();
  const merged = {
    name: user.name,
    email: user.email,
    role: user.role || existing?.role || 'client',
    provider: user.provider || existing?.provider || 'local',
    projects: user.projects ?? existing?.projects ?? [],
    username: user.username || existing?.username || deriveUsername(user.email || existing?.email),
  };
  localStorage.setItem(USER_KEY, JSON.stringify(merged));
  if (typeof window !== 'undefined') {
    window.dispatchEvent(new Event('hivi-auth-change'));
  }
  return merged;
};

export const clearUser = () => {
  localStorage.removeItem(USER_KEY);
  localStorage.removeItem('token');
  if (typeof window !== 'undefined') {
    window.dispatchEvent(new Event('hivi-auth-change'));
  }
};

export function deriveUsername(email) {
  if (!email) return null;
  const local = email.split('@')[0];
  return local.replace(/[^a-zA-Z0-9_.-]/g, '').toLowerCase() || null;
}

export const getFirstName = (name = '') => name.trim().split(/\s+/)[0] || name;

export const getLastName = (name = '') => {
  const parts = name.trim().split(/\s+/).filter(Boolean);
  return parts.length > 1 ? parts.slice(1).join(' ') : '';
};
