import { getApiUrl, getAuthHeaders, getPublicHeaders, clearInvalidToken, isLoggedIn } from '../utils/auth';

function friendlyError(status, body, fallback, { publicFeed = false } = {}) {
  const raw = body?.message || body?.error || fallback;
  if (status === 401) {
    if (publicFeed) {
      return 'We could not load the feed right now. Please try again in a moment.';
    }
    if (raw?.includes('session expired') || raw?.includes('Session expired')) {
      return 'Your sign-in has expired. Please sign in again to continue.';
    }
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

async function handleResponse(res, { allowRetry = false, publicFeed = false } = {}) {
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

  const message = friendlyError(res.status, body, `Request failed (${res.status})`, { publicFeed });
  const err = new Error(message);
  err.status = res.status;
  throw err;
}

async function fetchPublic(path, fetchOptions = {}) {
  const res = await fetch(`${getApiUrl()}${path}`, {
    ...fetchOptions,
    headers: { ...getPublicHeaders(), ...fetchOptions.headers },
    cache: 'no-store',
  });
  return handleResponse(res, { publicFeed: true });
}

export async function ensureCommunityDemo() {
  try {
    await fetch(`${getApiUrl()}/api/public/community/ensure-demo`, { method: 'POST' });
  } catch {
    /* non-blocking */
  }
}

export async function fetchCommunityFeed({ mode = 'trending', page = 0, size = 15 } = {}) {
  const params = new URLSearchParams({ mode, page: String(page), size: String(size) });
  await ensureCommunityDemo();
  return fetchPublic(`/api/public/community/feed?${params}`);
}

export async function createCommunityPost(formData) {
  const headers = getAuthHeaders(false);
  delete headers['Content-Type'];
  const res = await fetch(`${getApiUrl()}/api/community/posts`, {
    method: 'POST',
    headers,
    body: formData,
  });
  const body = await res.json().catch(() => ({}));
  if (!res.ok) {
    if (res.status === 401) {
      clearInvalidToken();
    }
    const message = friendlyError(res.status, body, 'Could not publish your post.');
    const err = new Error(message);
    err.status = res.status;
    throw err;
  }
  return body;
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
  return fetchPublic(`/api/public/community/posts/${postId}/comments`);
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
  if (isLoggedIn()) {
    const res = await fetch(`${getApiUrl()}/api/community/profiles/${username}`, {
      headers: getAuthHeaders(),
    });
    return handleResponse(res, { allowRetry: true });
  }
  return fetchPublic(`/api/public/community/profiles/${username}`);
}

export async function recordPostView(postId) {
  try {
    await fetch(`${getApiUrl()}/api/community/posts/${postId}/view`, { method: 'POST' });
  } catch {
    /* non-blocking */
  }
}
