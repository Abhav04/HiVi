import { getApiUrl, getOAuthApiUrl } from './auth';
import { wakeBackend } from './wakeBackend';
import { fetchOAuthStatus, getOAuthBlockers } from './oauthStatus';

const HEALTH_TIMEOUT_MS = 2500;
const MAX_WAKE_MS = 45000;

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
 * Fast path: if backend is warm, redirect immediately. Otherwise wake up to MAX_WAKE_MS then redirect.
 * This function handles Render cold starts robustly by awaiting full health readiness before checking status configs
 * and redirecting, preventing browser hangs and session timeout desyncs.
 */
export async function startOAuthFlow(provider, { onProgress, onStatus } = {}) {
  const apiUrl = getApiUrl();
  const beginUrl = buildOAuthBeginUrl(provider);

  onStatus?.('Checking server...');
  onProgress?.(10);

  // Check if backend is already awake. We do a quick check first to avoid blocking on fetchOAuthStatus if asleep.
  const alreadyUp = await quickHealthCheck(apiUrl, 1500);
  
  if (!alreadyUp) {
    onStatus?.('Starting server (first visit may take a moment)...');
    onProgress?.(15);

    // Start polling health status
    const wake = wakeBackend(apiUrl, MAX_WAKE_MS);
    let tick = 15;
    const progressInterval = setInterval(() => {
      // Slowly advance progress towards 85% to indicate active startup
      tick = Math.min(85, tick + (tick < 50 ? 5 : 2));
      onProgress?.(tick);
    }, 850);

    const result = await wake;
    clearInterval(progressInterval);

    if (!result.ready) {
      onStatus?.('Server took too long to start. Please try again.');
      onProgress?.(0);
      // Redirect back to login screen with a descriptive error
      window.location.href = `/login?error=server_offline&provider=${provider}`;
      return { redirected: false, warm: false };
    }
  }

  // Once backend is confirmed active and stable, execute blocker configuration checks
  onStatus?.('Verifying configurations...');
  onProgress?.(90);
  
  const blockers = await fetchOAuthStatus(apiUrl)
    .then((status) => getOAuthBlockers(status, provider, apiUrl))
    .catch(() => []);

  if (blockers.length > 0) {
    const code = 'invalid_client';
    const detail = encodeURIComponent(blockers[0]);
    window.location.href = `/login?error=${encodeURIComponent(code)}&detail=${detail}&provider=${provider}`;
    return { redirected: false, blockers };
  }

  onStatus?.('Opening secure sign-in...');
  onProgress?.(100);
  await new Promise((r) => setTimeout(r, 200));
  window.location.href = beginUrl;
  return { redirected: true, warm: true };
}
