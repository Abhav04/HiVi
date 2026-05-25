import React, { useCallback, useEffect, useRef, useState, memo } from 'react';
import { fetchRedditTrending } from '../../services/redditApi';
import RedditPostCard from './RedditPostCard';
import RedditTrendingHero from './RedditTrendingHero';
import RedditHiringSection from './RedditHiringSection';
import './RedditTrendingFeed.css';

const PAGE_SUBREDDITS = [
  'all',
  'VideoEditors',
  'VideoEditor_forhire',
  'FreelanceIndia',
  'forhire',
  'videoediting',
  'AfterEffects',
  'premiere',
  'Filmmakers',
  'videography',
];

const SkeletonCard = memo(function SkeletonCard({ variant }) {
  return (
    <div className={`reddit-skeleton reddit-skeleton--${variant}`}>
      <div className="reddit-skeleton-media shimmer" />
      <div className="reddit-skeleton-body">
        <div className="reddit-skeleton-line shimmer" style={{ width: '92%' }} />
        <div className="reddit-skeleton-line shimmer" style={{ width: '70%' }} />
        <div className="reddit-skeleton-line shimmer" style={{ width: '45%' }} />
      </div>
    </div>
  );
});

const RedditTrendingFeed = ({ variant = 'default' }) => {
  const isPage = variant === 'page';
  const [activeSub, setActiveSub] = useState('all');
  const [posts, setPosts] = useState([]);
  const [featuredPost, setFeaturedPost] = useState(null);
  const [hiringPosts, setHiringPosts] = useState([]);
  const [subreddits, setSubreddits] = useState(PAGE_SUBREDDITS);
  const [, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState(null);
  const [cachedAt, setCachedAt] = useState(null);
  const loaderRef = useRef(null);
  const scrollRef = useRef(null);
  const pageSize = isPage ? 15 : 12;

  const loadPage = useCallback(
    async (subreddit, pageNum, append) => {
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
          limit: pageSize,
        });

        setPosts((prev) => (append ? [...prev, ...data.posts] : data.posts));
        if (!append) {
          setFeaturedPost(data.featuredPost ?? null);
          setHiringPosts(data.hiringPosts ?? []);
        }
        setHasMore(data.hasMore);
        setCachedAt(data.cachedAt);
        if (data.subreddits?.length) {
          setSubreddits(['all', ...data.subreddits.map((s) => s.replace(/^r\//i, ''))]);
        }
      } catch (err) {
        setError(err.message || 'Could not load inspiration feed');
        if (!append) {
          setPosts([]);
          setFeaturedPost(null);
          setHiringPosts([]);
        }
      } finally {
        setLoading(false);
        setLoadingMore(false);
      }
    },
    [pageSize]
  );

  useEffect(() => {
    setPage(0);
    setHasMore(true);
    loadPage(activeSub, 0, false);
    if (scrollRef.current) {
      scrollRef.current.scrollTop = 0;
    }
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
      { root: isPage ? scrollRef.current : null, rootMargin: '240px' }
    );

    const node = loaderRef.current;
    if (node) observer.observe(node);
    return () => observer.disconnect();
  }, [hasMore, loading, loadingMore, posts.length, activeSub, loadPage, isPage]);

  const handleTab = (sub) => {
    if (sub === activeSub) return;
    setActiveSub(sub);
  };

  const formatSubLabel = (sub) => (sub === 'all' ? 'all communities' : `r/${sub}`);

  const showHero = isPage && featuredPost && !loading;
  const showHiring = isPage && hiringPosts.length > 0 && !loading && activeSub === 'all';

  return (
    <section className={`reddit-trending reddit-trending--${variant}`}>
      <div className="reddit-trending-header">
        <div>
          <p className="reddit-trending-eyebrow">
            {isPage ? 'curated creator feed' : 'creator ecosystem'}
          </p>
          <h2 className="reddit-trending-title">
            {isPage ? (
              <>
                trends from <em>reddit</em>
              </>
            ) : (
              <>
                trending from <em>reddit</em>
              </>
            )}
          </h2>
          <p className="reddit-trending-sub">
            {isPage
              ? 'Ranked by engagement and recency — inspiration and paid opportunities for editors.'
              : 'Hot posts from editing communities — refreshed every few minutes.'}
          </p>
        </div>
        {cachedAt && !loading && (
          <span className="reddit-trending-cache">
            refreshed{' '}
            {new Date(cachedAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
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
            {formatSubLabel(sub)}
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

      <div className="reddit-feed-scroll" ref={scrollRef}>
        {loading && isPage && (
          <div className="reddit-hero-skeleton shimmer" aria-hidden="true" />
        )}

        {showHero && <RedditTrendingHero post={featuredPost} />}

        {showHiring && <RedditHiringSection posts={hiringPosts} />}

        {isPage && !loading && (
          <h3 className="reddit-feed-section-title">editor inspiration</h3>
        )}

        <div className={`reddit-grid reddit-grid--${variant}`}>
          {loading
            ? Array.from({ length: isPage ? 9 : 6 }).map((_, i) => (
                <SkeletonCard key={`sk-${i}`} variant={variant} />
              ))
            : posts.map((post, index) => (
                <RedditPostCard
                  key={`${post.id}-${post.subreddit}`}
                  post={post}
                  variant={variant}
                  priority={index < 4}
                />
              ))}
        </div>

        {!loading && !error && posts.length === 0 && !featuredPost && (
          <p className="reddit-empty">No posts in this community right now. Try another filter.</p>
        )}

        <div ref={loaderRef} className="reddit-loader">
          {loadingMore && <span className="reddit-loader-text">loading more inspiration…</span>}
          {!hasMore && posts.length > 0 && (
            <span className="reddit-loader-text muted">you&apos;re all caught up</span>
          )}
        </div>
      </div>
    </section>
  );
};

export default RedditTrendingFeed;
