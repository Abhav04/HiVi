import { getApiUrl } from '../utils/auth';

/**
 * Fetches cached trending Reddit posts from the HiVi backend.
 */
export async function fetchRedditTrending({ subreddit = 'all', page = 0, limit = 12 } = {}) {
  const params = new URLSearchParams({
    page: String(page),
    limit: String(limit),
  });
  if (subreddit && subreddit !== 'all') {
    params.set('subreddit', subreddit.replace(/^r\//, ''));
  }

  const res = await fetch(`${getApiUrl()}/api/reddit/trending?${params}`, {
    method: 'GET',
    cache: 'no-store',
  });

  if (!res.ok) {
    throw new Error(`Failed to load Reddit feed (${res.status})`);
  }

  return res.json();
}
