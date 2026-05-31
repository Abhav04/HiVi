import React, { useCallback, useEffect, useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { fetchCommunityFeed } from '../../services/communityApi';
import { useAuth } from '../../context/AuthContext';
import CommunityPostCard from './CommunityPostCard';
import PostComposer from './PostComposer';
import FeedSidebar from './FeedSidebar';
import './CommunityFeed.css';

const CommunityFeed = () => {
  const [mode, setMode] = useState('trending');
  const [posts, setPosts] = useState([]);
  const [featured, setFeatured] = useState(null);
  const [topLiked, setTopLiked] = useState([]);
  const [trendingCreators, setTrendingCreators] = useState([]);
  const [, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [showComposer, setShowComposer] = useState(false);
  const [error, setError] = useState(null);
  const [authHint, setAuthHint] = useState(false);
  const { isAuthenticated: loggedIn } = useAuth();
  const loaderRef = useRef(null);

  const load = useCallback(async (feedMode, pageNum, append) => {
    if (append) setLoadingMore(true);
    else {
      setLoading(true);
      setError(null);
    }
    try {
      const data = await fetchCommunityFeed({ mode: feedMode, page: pageNum, size: 12 });
      setPosts((prev) => (append ? [...prev, ...data.posts] : data.posts));
      if (!append) {
        setFeatured(data.featuredPost);
        setTopLiked(data.topLikedPosts || []);
        setTrendingCreators(data.trendingCreators || []);
      }
      setHasMore(data.hasMore);
    } catch (err) {
      setAuthHint(false);
      const msg = err.message || 'Could not load the feed';
      setError(
        msg.includes('Failed to fetch') || msg.includes('NetworkError')
          ? 'Cannot reach the API. Start the backend (./run-local.sh) and ensure REACT_APP_API_URL=http://localhost:8080'
          : msg
      );
      if (!append) {
        setPosts([]);
        setFeatured(null);
        setTopLiked([]);
      }
    } finally {
      setLoading(false);
      setLoadingMore(false);
    }
  }, []);

  useEffect(() => {
    if (mode === 'following' && !loggedIn) {
      setMode('trending');
      return;
    }
    setPage(0);
    load(mode, 0, false);
  }, [mode, load, loggedIn]);

  useEffect(() => {
    if (!hasMore || loading || loadingMore) return undefined;
    const obs = new IntersectionObserver(([entry]) => {
      if (entry.isIntersecting && hasMore && !loadingMore) {
        setPage((p) => {
          const next = p + 1;
          load(mode, next, true);
          return next;
        });
      }
    }, { rootMargin: '200px' });
    if (loaderRef.current) obs.observe(loaderRef.current);
    return () => obs.disconnect();
  }, [hasMore, loading, loadingMore, mode, load]);

  const handlePosted = () => {
    setShowComposer(false);
    load(mode, 0, false);
  };

  return (
    <div className="community-feed-page">
      <header className="community-feed-header">
        <div>
          <p className="community-feed-eyebrow">Creator network</p>
          <h1 className="community-feed-title">Community Feed</h1>
          <p className="community-feed-sub">
            Portfolio drops, reels, breakdowns, and freelance availability from video editors.
          </p>
        </div>
        <div className="community-feed-header-actions">
          <button type="button" className="community-create-btn" onClick={() => setShowComposer(true)}>
            + Start a post
          </button>
        </div>
      </header>

      <div className="community-layout">
        <main className="community-main">
          <div className="community-feed-tabs">
            {['trending', 'following'].map((m) => (
              <button
                key={m}
                type="button"
                className={`community-feed-tab ${mode === m ? 'active' : ''}`}
                onClick={() => {
                  if (m === 'following' && !loggedIn) return;
                  setMode(m);
                }}
                disabled={m === 'following' && !loggedIn}
                title={m === 'following' && !loggedIn ? 'Sign in to see creators you follow' : undefined}
              >
                {m === 'trending' ? 'For you' : 'Following'}
              </button>
            ))}
          </div>

          {loggedIn && (
            <button
              type="button"
              className="community-start-post"
              onClick={() => setShowComposer(true)}
            >
              <span className="community-start-post__avatar">+</span>
              <span>Share an edit, reel, or portfolio piece…</span>
            </button>
          )}

          {showComposer && (
            <div className="community-composer-wrap">
              <PostComposer onPosted={handlePosted} onClose={() => setShowComposer(false)} />
            </div>
          )}

          {!loggedIn && !loading && (
            <div className="community-auth-banner">
              <p>Browse as a guest — sign in to post, like, follow, and save work.</p>
              <Link to="/login" className="community-auth-banner-cta">
                Sign in
              </Link>
            </div>
          )}

          {error && (
            <div className={`community-feed-error ${authHint ? 'community-feed-error--auth' : ''}`}>
              <p>{error}</p>
              <div className="community-feed-error-actions">
                {authHint && <Link to="/login" className="community-auth-banner-cta">Sign in</Link>}
                <button type="button" onClick={() => load(mode, 0, false)}>
                  Retry
                </button>
              </div>
            </div>
          )}

          <div className="community-feed-stream">
            {!loading && featured && (
              <section className="community-featured-block">
                <p className="community-section-label">Featured creator work</p>
                <CommunityPostCard post={featured} variant="featured" onUpdate={() => load(mode, 0, false)} />
              </section>
            )}

            {loading
              ? Array.from({ length: 4 }).map((_, i) => (
                  <div key={i} className="community-skeleton shimmer" style={{ animationDelay: `${i * 0.08}s` }} />
                ))
              : posts.map((post) => (
                  <CommunityPostCard
                    key={post.id}
                    post={post}
                    onUpdate={() => load(mode, 0, false)}
                  />
                ))}

            {!loading && !error && posts.length === 0 && !featured && (
              <div className="community-empty">
                <h3>Your feed is ready for its first story</h3>
                <p>Be the first to share a cinematic cut, tutorial, or portfolio piece.</p>
                {!loggedIn && <Link to="/login">Sign in to post</Link>}
              </div>
            )}

            <div ref={loaderRef} className="community-loader">
              {loadingMore && <span>Loading more posts…</span>}
            </div>
          </div>
        </main>

        <FeedSidebar trendingCreators={trendingCreators} topLiked={topLiked} />
      </div>
    </div>
  );
};

export default CommunityFeed;
