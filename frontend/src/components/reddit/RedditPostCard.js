import React, { memo, useCallback } from 'react';
import RedditPostImage from './RedditPostImage';
import './RedditPostCard.css';

const badgeClass = (badge) => (badge ? badge.toLowerCase() : 'hiring');

const RedditPostCard = memo(function RedditPostCard({ post, variant = 'default', priority = false }) {
  const openReddit = useCallback(() => {
    window.open(post.redditUrl || post.permalink, '_blank', 'noopener,noreferrer');
  }, [post.redditUrl, post.permalink]);

  const handleKeyDown = useCallback(
    (e) => {
      if (e.key === 'Enter') openReddit();
    },
    [openReddit]
  );

  return (
    <article
      className={`reddit-card reddit-card--${variant} ${post.hiring ? 'reddit-card--hiring' : ''}`}
      onClick={openReddit}
      role="button"
      tabIndex={0}
      onKeyDown={handleKeyDown}
    >
      <div className="reddit-card-media">
        <RedditPostImage
          thumbnailUrl={post.thumbnailUrl}
          imageUrls={post.imageUrls}
          title={post.title}
          subreddit={post.subreddit}
          mediaType={post.mediaType}
          priority={priority}
        />
        <span className="reddit-card-subreddit">{post.subreddit}</span>
        {post.hiring && post.hiringBadge && (
          <span className={`reddit-card-hiring-badge reddit-card-hiring-badge--${badgeClass(post.hiringBadge)}`}>
            {post.hiringBadge}
          </span>
        )}
        <div className="reddit-card-media-glow" aria-hidden="true" />
      </div>

      <div className="reddit-card-body">
        <h3 className="reddit-card-title">{post.title}</h3>
        <div className="reddit-card-meta">
          <span>u/{post.author}</span>
          <span className="reddit-card-dot">·</span>
          <span>{post.timeAgo}</span>
        </div>
        <div className="reddit-card-stats">
          <span className="reddit-stat">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M12 19V5M5 12l7-7 7 7" />
            </svg>
            {post.upvotes?.toLocaleString()}
          </span>
          <span className="reddit-stat">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
            </svg>
            {post.commentCount?.toLocaleString()}
          </span>
        </div>
        <button
          type="button"
          className="reddit-card-cta"
          onClick={(e) => {
            e.stopPropagation();
            openReddit();
          }}
        >
          open on reddit →
        </button>
      </div>
    </article>
  );
});

export default RedditPostCard;
