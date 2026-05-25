import { getApiUrl, getAuthHeaders, getPublicHeaders } from '../utils/auth';

async function handleResponse(res, fallback) {
  if (res.ok) return res.json();
  const body = await res.json().catch(() => ({}));
  throw new Error(body.message || body.error || fallback);
}

export async function fetchOpportunities({ page = 0, size = 20, category, source } = {}) {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  if (category && category !== 'ALL') params.set('category', category);
  if (source && source !== 'ALL') params.set('source', source);
  const res = await fetch(`${getApiUrl()}/api/opportunities?${params}`, {
    headers: getPublicHeaders(),
  });
  return handleResponse(res, 'Failed to load opportunities');
}

export async function fetchTrendingOpportunities() {
  const res = await fetch(`${getApiUrl()}/api/opportunities/trending`, {
    headers: getPublicHeaders(),
  });
  return handleResponse(res, 'Failed to load trending');
}

export async function fetchOpportunitiesByCategory(type, page = 0, size = 20) {
  const params = new URLSearchParams({ page: String(page), size: String(size) });
  const res = await fetch(`${getApiUrl()}/api/opportunities/category/${type}?${params}`, {
    headers: getPublicHeaders(),
  });
  return handleResponse(res, 'Failed to load category');
}

export async function postOpportunity(payload) {
  const res = await fetch(`${getApiUrl()}/api/opportunities`, {
    method: 'POST',
    headers: { ...getAuthHeaders(), 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
  return handleResponse(res, 'Failed to post opportunity');
}
