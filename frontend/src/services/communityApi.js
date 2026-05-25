import { getApiUrl, getAuthHeaders, getPublicHeaders, clearInvalidToken, isLoggedIn } from '../utils/auth';

function friendlyError(status, body, fallback) {
  const raw = body?.message || body?.error || fallback;
  if (status === 401) {
    if (raw?.includes('Full authentication') || raw?.includes('Unauthorized')) {
      return 'Sign in to unlock the full community experience.';
    }
    return raw || 'Please sign in to continue.';
  }
  if (status === 403) {
    return 'You do not have permission for this action.';
  }
  return raw || fallback;
}

async function handleResponse(res, { allowRetry = false } = {}) {
  if (res.ok) {
    return res.json();
  }

  const body = await res.json().catch(() => ({}));

  if (res.status === 401 && allowRetry) {
    clearInvalidToken();
    const err = new Error(friendlyError(res.status, body, 'Authentication required'));
    err.status = 401;
    err.shouldRetryPublic = true;
    throw err;
  }

  const message = friendlyError(res.status, body, `Request failed (${res.status})`);
  const err = new Error(message);
  err.status = res.status;
  throw err;
}

export async function fetchCommunityFeed({ mode = 'trending', page = 0, size = 15 } = {}) {
  const params = new URLSearchParams({ mode, page: String(page), size: String(size) });
  const url = `${getApiUrl()}/api/community/feed?${params}`;

  // Feed is public — never send a stale JWT (avoids false "session expired" errors)
  const res = await fetch(url, { headers: getPublicHeaders(), cache: 'no-store' });
  return handleResponse(res);
}

export async function createCommunityPost(formData) {
  const headers = getAuthHeaders(false);
  delete headers['Content-Type'];
  const res = await fetch(`${getApiUrl()}/api/community/posts`, {
    method: 'POST',
    headers,
    body: formData,
  });
  return handleResponse(res);
}

export async function toggleLike(postId) {
  const res = await fetch(`${getApiUrl()}/api/community/posts/${postId}/like`, {
    method: 'POST',
    headers: getAuthHeaders(),
  });
  return handleResponse(res);
}

export async function toggleBookmark(postId) {
  const res = await fetch(`${getApiUrl()}/api/community/posts/${postId}/bookmark`, {
    method: 'POST',
    headers: getAuthHeaders(),
  });
  return handleResponse(res);
}

export async function fetchComments(postId) {
  const res = await fetch(`${getApiUrl()}/api/community/posts/${postId}/comments`, {
    headers: getPublicHeaders(),
  });
  return handleResponse(res);
}

export async function addComment(postId, content, parentId = null) {
  const res = await fetch(`${getApiUrl()}/api/community/posts/${postId}/comments`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: JSON.stringify({ content, parentId }),
  });
  return handleResponse(res);
}

export async function toggleFollow(userId) {
  const res = await fetch(`${getApiUrl()}/api/community/users/${userId}/follow`, {
    method: 'POST',
    headers: getAuthHeaders(),
  });
  return handleResponse(res);
}

export async function fetchCreatorProfile(username) {
  const headers = isLoggedIn() ? getAuthHeaders() : getPublicHeaders();
  const res = await fetch(`${getApiUrl()}/api/community/profiles/${username}`, { headers });
  return handleResponse(res, { allowRetry: true });
}

export async function recordPostView(postId) {
  try {
    await fetch(`${getApiUrl()}/api/community/posts/${postId}/view`, { method: 'POST' });
  } catch {
    /* non-blocking */
  }
}
