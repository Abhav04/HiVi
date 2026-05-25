import React, { memo, useCallback, useMemo, useState } from 'react';

const SUBREDDIT_GRADIENTS = [
  ['#1a1208', '#3d2814', '#ff8c42'],
  ['#0d0a14', '#2a1a3d', '#a855f7'],
  ['#081018', '#142838', '#38bdf8'],
  ['#140a0a', '#2d1818', '#f97316'],
];

function hashSubreddit(name = '') {
  let h = 0;
  const s = name.toLowerCase();
  for (let i = 0; i < s.length; i += 1) {
    h = (h * 31 + s.charCodeAt(i)) % 10000;
  }
  return h;
}

function buildUrlList(thumbnailUrl, imageUrls) {
  const seen = new Set();
  const list = [];
  const add = (url) => {
    if (!url || seen.has(url)) return;
    seen.add(url);
    list.push(url);
  };
  add(thumbnailUrl);
  if (Array.isArray(imageUrls)) {
    imageUrls.forEach(add);
  }
  return list;
}

const RedditPostImage = memo(function RedditPostImage({
  thumbnailUrl,
  imageUrls,
  title,
  subreddit,
  mediaType,
  priority = false,
  className = '',
}) {
  const urls = useMemo(() => buildUrlList(thumbnailUrl, imageUrls), [thumbnailUrl, imageUrls]);
  const [urlIndex, setUrlIndex] = useState(0);
  const [loaded, setLoaded] = useState(false);
  const [exhausted, setExhausted] = useState(false);

  const activeUrl = urls[urlIndex] ?? null;
  const showImage = Boolean(activeUrl) && !exhausted;

  const gradient = useMemo(() => {
    const idx = hashSubreddit(subreddit) % SUBREDDIT_GRADIENTS.length;
    const [a, b, accent] = SUBREDDIT_GRADIENTS[idx];
    return `linear-gradient(145deg, ${a} 0%, ${b} 55%, ${accent}22 100%)`;
  }, [subreddit]);

  const handleError = useCallback(() => {
    setLoaded(false);
    setUrlIndex((prev) => {
      if (prev + 1 < urls.length) return prev + 1;
      setExhausted(true);
      return prev;
    });
  }, [urls.length]);

  React.useEffect(() => {
    setUrlIndex(0);
    setLoaded(false);
    setExhausted(false);
  }, [thumbnailUrl, imageUrls]);

  const handleLoad = useCallback(() => setLoaded(true), []);

  const isVideo = mediaType === 'video';

  return (
    <div className={`reddit-card-media-inner ${className}`.trim()}>
      <div
        className="reddit-card-thumb-placeholder reddit-card-thumb-fallback"
        style={{ background: gradient }}
        aria-hidden={showImage && loaded}
      >
        <div className="reddit-card-fallback-icon">
          {isVideo ? (
            <svg width="36" height="36" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1">
              <polygon points="23 7 16 12 23 17 23 7" />
              <rect x="1" y="5" width="15" height="14" rx="2" />
            </svg>
          ) : (
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1">
              <rect x="3" y="3" width="18" height="18" rx="2" />
              <circle cx="8.5" cy="8.5" r="1.5" fill="currentColor" />
              <path d="M21 15l-5-5L5 21" />
            </svg>
          )}
        </div>
        <span className="reddit-card-fallback-sub">{subreddit}</span>
      </div>

      {showImage && (
        <img
          key={activeUrl}
          src={activeUrl}
          alt={title ? `${title.slice(0, 80)} preview` : 'Post preview'}
          className={`reddit-card-thumb ${loaded ? 'is-loaded' : ''}`}
          loading={priority ? 'eager' : 'lazy'}
          decoding="async"
          fetchPriority={priority ? 'high' : 'auto'}
          sizes="(max-width: 768px) 100vw, (max-width: 1400px) 50vw, 420px"
          referrerPolicy="no-referrer"
          onLoad={handleLoad}
          onError={handleError}
        />
      )}

      {isVideo && loaded && (
        <span className="reddit-card-media-type-badge" aria-label="Video post">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor">
            <polygon points="8 5 19 12 8 19 8 5" />
          </svg>
        </span>
      )}
    </div>
  );
});

export default RedditPostImage;
