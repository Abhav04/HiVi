import { getApiUrl, getOAuthApiUrl } from './auth';
import { wakeBackend } from './wakeBackend';
import { fetchOAuthStatus, getOAuthBlockers } from './oauthStatus';

const HEALTH_TIMEOUT_MS = 2500;
const MAX_WAKE_MS = 12000;

/**
 * Quick health ping — returns true if backend responds within timeout.
 */
export async function quickHealthCheck(apiUrl = getApiUrl(), timeoutMs = HEALTH_TIMEOUT_MS) {
  try {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), timeoutMs);
    const res = await fetch(`${apiUrl}/health`, {
      method: 'GET',
      signal: controller.signal,
      cache: 'no-store',
    });
    clearTimeout(timer);
    return res.ok;
  } catch {
    return false;
  }
}

/**
 * Builds the backend OAuth entry URL (sets session return URL, then Spring redirects to Google/GitHub).
 */
export function buildOAuthBeginUrl(provider, frontendOrigin = window.location.origin) {
  const api = getOAuthApiUrl();
  const frontend = encodeURIComponent(frontendOrigin);
  return `${api}/oauth/begin?provider=${provider}&frontend=${frontend}`;
}

/**
 * Fast path: if backend is warm, redirect immediately. Otherwise wake up to MAX_WAKE_MS then redirect anyway.
 */
export async function startOAuthFlow(provider, { onProgress, onStatus } = {}) {
  const apiUrl = getApiUrl();
  const beginUrl = buildOAuthBeginUrl(provider);

  onStatus?.('Checking server...');
  onProgress?.(12);

  const blockers = await fetchOAuthStatus(apiUrl)
    .then((status) => getOAuthBlockers(status, provider, apiUrl))
    .catch(() => []);

  if (blockers.length > 0) {
    const code = provider === 'github' ? 'invalid_client' : 'invalid_client';
    const detail = encodeURIComponent(blockers[0]);
    window.location.href = `/login?error=${encodeURIComponent(code)}&detail=${detail}&provider=${provider}`;
    return { redirected: false, blockers };
  }

  const alreadyUp = await quickHealthCheck(apiUrl);
  if (alreadyUp) {
    onStatus?.('Opening sign in...');
    onProgress?.(100);
    window.location.href = beginUrl;
    return { redirected: true, warm: true };
  }

  onStatus?.('Starting server (first visit may take a moment)...');
  onProgress?.(25);

  const wake = wakeBackend(apiUrl, MAX_WAKE_MS);
  let tick = 25;
  const progressInterval = setInterval(() => {
    tick = Math.min(88, tick + 5);
    onProgress?.(tick);
  }, 500);

  const result = await wake;
  clearInterval(progressInterval);

  onProgress?.(100);
  onStatus?.(result.ready ? 'Opening sign in...' : 'Connecting...');

  await new Promise((r) => setTimeout(r, 200));
  window.location.href = beginUrl;
  return { redirected: true, warm: result.ready };
}
