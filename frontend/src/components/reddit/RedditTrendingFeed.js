import React, { useCallback, useEffect, useRef, useState } from 'react';
import { fetchRedditTrending } from '../../services/redditApi';
import RedditPostCard from './RedditPostCard';
import './RedditTrendingFeed.css';

const DEFAULT_SUBREDDITS = [
  'all',
  'videoediting',
  'editors',
  'AfterEffects',
  'premiere',
  'videography',
  'Filmmakers',
];

const SkeletonCard = () => (
  <div className="reddit-skeleton">
    <div className="reddit-skeleton-media shimmer" />
    <div className="reddit-skeleton-body">
      <div className="reddit-skeleton-line shimmer" style={{ width: '90%' }} />
      <div className="reddit-skeleton-line shimmer" style={{ width: '60%' }} />
      <div className="reddit-skeleton-line shimmer" style={{ width: '40%' }} />
    </div>
  </div>
);

const RedditTrendingFeed = () => {
  const [activeSub, setActiveSub] = useState('all');
  const [posts, setPosts] = useState([]);
  const [subreddits, setSubreddits] = useState(DEFAULT_SUBREDDITS);
  const [, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState(null);
  const [cachedAt, setCachedAt] = useState(null);
  const loaderRef = useRef(null);

  const loadPage = useCallback(async (subreddit, pageNum, append) => {
    if (append) {
      setLoadingMore(true);
    } else {
      setLoading(true);
      setError(null);
    }

    try {
      const data = await fetchRedditTrending({
        subreddit,
        page: pageNum,
        limit: 12,
      });

      setPosts((prev) => (append ? [...prev, ...data.posts] : data.posts));
      setHasMore(data.hasMore);
      setCachedAt(data.cachedAt);
      if (data.subreddits?.length) {
        setSubreddits(['all', ...data.subreddits.map((s) => s.replace(/^r\//, ''))]);
      }
    } catch (err) {
      setError(err.message || 'Could not load Reddit feed');
      if (!append) setPosts([]);
    } finally {
      setLoading(false);
      setLoadingMore(false);
    }
  }, []);

  useEffect(() => {
    setPage(0);
    setHasMore(true);
    loadPage(activeSub, 0, false);
  }, [activeSub, loadPage]);

  useEffect(() => {
    if (!hasMore || loading || loadingMore || posts.length === 0) return undefined;

    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && hasMore && !loadingMore && !loading) {
          setPage((prev) => {
            const nextPage = prev + 1;
            loadPage(activeSub, nextPage, true);
            return nextPage;
          });
        }
      },
      { rootMargin: '200px' }
    );

    const node = loaderRef.current;
    if (node) observer.observe(node);
    return () => observer.disconnect();
  }, [hasMore, loading, loadingMore, posts.length, activeSub, loadPage]);

  const handleTab = (sub) => {
    if (sub === activeSub) return;
    setActiveSub(sub);
  };

  return (
    <section className="reddit-trending">
      <div className="reddit-trending-header">
        <div>
          <p className="reddit-trending-eyebrow">creator ecosystem</p>
          <h2 className="reddit-trending-title">
            trending from <em>reddit</em>
          </h2>
          <p className="reddit-trending-sub">
            Hot posts from editing communities — refreshed every few minutes.
          </p>
        </div>
        {cachedAt && !loading && (
          <span className="reddit-trending-cache">
            updated {new Date(cachedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
          </span>
        )}
      </div>

      <div className="reddit-tabs" role="tablist">
        {subreddits.map((sub) => (
          <button
            key={sub}
            type="button"
            role="tab"
            aria-selected={activeSub === sub}
            className={`reddit-tab ${activeSub === sub ? 'active' : ''}`}
            onClick={() => handleTab(sub)}
          >
            {sub === 'all' ? 'all' : `r/${sub}`}
          </button>
        ))}
      </div>

      {error && (
        <div className="reddit-error">
          <p>{error}</p>
          <button type="button" onClick={() => loadPage(activeSub, 0, false)}>
            retry →
          </button>
        </div>
      )}

      <div className="reddit-grid">
        {loading
          ? Array.from({ length: 6 }).map((_, i) => <SkeletonCard key={`sk-${i}`} />)
          : posts.map((post) => <RedditPostCard key={`${post.id}-${post.subreddit}`} post={post} />)}
      </div>

      {!loading && !error && posts.length === 0 && (
        <p className="reddit-empty">No posts found for this community right now.</p>
      )}

      <div ref={loaderRef} className="reddit-loader">
        {loadingMore && <span className="reddit-loader-text">loading more…</span>}
        {!hasMore && posts.length > 0 && (
          <span className="reddit-loader-text muted">you&apos;re all caught up</span>
        )}
      </div>
    </section>
  );
};

export default RedditTrendingFeed;
