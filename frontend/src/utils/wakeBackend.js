import { getApiUrl } from './auth';

const HEALTH_PATH = '/health';

/**
 * Pings the backend until it responds or timeout. Wakes Render free-tier instances.
 */
const retryDelayMs = (attempt) => Math.min(1500 + attempt * 200, 4000);

export async function wakeBackend(apiUrl = getApiUrl(), maxWaitMs = 90000) {
  const deadline = Date.now() + maxWaitMs;

  for (let attempt = 1; Date.now() < deadline; attempt += 1) {
    try {
      const controller = new AbortController();
      const timeout = setTimeout(() => controller.abort(), 8000);
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
