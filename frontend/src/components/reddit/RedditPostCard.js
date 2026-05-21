import React from 'react';
import './RedditPostCard.css';

const RedditPostCard = ({ post }) => {
  const openReddit = () => {
    window.open(post.redditUrl || post.permalink, '_blank', 'noopener,noreferrer');
  };

  return (
    <article className="reddit-card" onClick={openReddit} role="button" tabIndex={0}
      onKeyDown={(e) => e.key === 'Enter' && openReddit()}>
      <div className="reddit-card-media">
        {post.thumbnailUrl ? (
          <img
            src={post.thumbnailUrl}
            alt=""
            className="reddit-card-thumb"
            loading="lazy"
            onError={(e) => { e.currentTarget.style.display = 'none'; }}
          />
        ) : (
          <div className="reddit-card-thumb-placeholder">
            <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.2">
              <polygon points="23 7 16 12 23 17 23 7" />
              <rect x="1" y="5" width="15" height="14" rx="2" />
            </svg>
          </div>
        )}
        <span className="reddit-card-subreddit">{post.subreddit}</span>
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
};

export default RedditPostCard;
