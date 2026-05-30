import { getApiUrl } from './auth';

const HEALTH_PATH = '/health';

const retryDelayMs = (attempt) => Math.min(400 + attempt * 150, 2000);

/**
 * Pings the backend until it responds or timeout. Wakes Render free-tier instances.
 * Default max wait is 12s — long enough for cold start, short enough to feel responsive.
 */
export async function wakeBackend(apiUrl = getApiUrl(), maxWaitMs = 12000) {
  const deadline = Date.now() + maxWaitMs;

  for (let attempt = 1; Date.now() < deadline; attempt += 1) {
    try {
      const controller = new AbortController();
      const timeout = setTimeout(() => controller.abort(), 5000);
      const res = await fetch(`${apiUrl}${HEALTH_PATH}`, {
        method: 'GET',
        signal: controller.signal,
        cache: 'no-store',
      });
      clearTimeout(timeout);
      if (res.ok) return { ready: true, attempts: attempt };
    } catch {
      // Render cold start — keep retrying
    }
    await new Promise((resolve) => {
      setTimeout(resolve, retryDelayMs(attempt));
    });
  }

  return { ready: false, attempts: 0 };
}
