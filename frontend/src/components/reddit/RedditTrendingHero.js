import React, { memo, useCallback } from 'react';
import RedditPostImage from './RedditPostImage';
import './RedditTrendingHero.css';

const RedditTrendingHero = memo(function RedditTrendingHero({ post }) {
  const openReddit = useCallback(() => {
    if (!post) return;
    window.open(post.redditUrl || post.permalink, '_blank', 'noopener,noreferrer');
  }, [post]);

  if (!post) return null;

  return (
    <section className="reddit-hero" aria-label="Trending now">
      <div className="reddit-hero-label">
        <span className="reddit-hero-pulse" />
        trending now
      </div>

      <article className="reddit-hero-card" onClick={openReddit} role="button" tabIndex={0}
        onKeyDown={(e) => e.key === 'Enter' && openReddit()}>
        <div className="reddit-hero-media">
          <RedditPostImage
            thumbnailUrl={post.thumbnailUrl}
            imageUrls={post.imageUrls}
            title={post.title}
            subreddit={post.subreddit}
            mediaType={post.mediaType}
            priority
            className="reddit-hero-image"
          />
          <div className="reddit-hero-overlay" />
        </div>

        <div className="reddit-hero-content">
          <div className="reddit-hero-badges">
            <span className="reddit-hero-badge reddit-hero-badge--trending">top engagement</span>
            {post.hiring && post.hiringBadge && (
              <span className={`reddit-hero-badge reddit-hero-badge--hire reddit-hero-badge--${post.hiringBadge.toLowerCase()}`}>
                {post.hiringBadge}
              </span>
            )}
            <span className="reddit-hero-badge reddit-hero-badge--sub">{post.subreddit}</span>
          </div>

          <h2 className="reddit-hero-title">{post.title}</h2>

          <div className="reddit-hero-meta">
            <span>u/{post.author}</span>
            <span className="reddit-hero-dot">·</span>
            <span>{post.timeAgo}</span>
            <span className="reddit-hero-dot">·</span>
            <span className="reddit-hero-stat">↑ {post.upvotes?.toLocaleString()}</span>
            <span className="reddit-hero-stat">💬 {post.commentCount?.toLocaleString()}</span>
          </div>

          <button
            type="button"
            className="reddit-hero-cta"
            onClick={(e) => {
              e.stopPropagation();
              openReddit();
            }}
          >
            open on reddit →
          </button>
        </div>
      </article>
    </section>
  );
});

export default RedditTrendingHero;
