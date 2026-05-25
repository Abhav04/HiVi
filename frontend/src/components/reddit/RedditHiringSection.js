import React, { memo, useCallback } from 'react';
import RedditPostImage from './RedditPostImage';
import './RedditHiringSection.css';

const badgeClass = (badge) => {
  if (!badge) return 'hiring';
  return badge.toLowerCase();
};

const HiringCard = memo(function HiringCard({ post }) {
  const openReddit = useCallback(() => {
    window.open(post.redditUrl || post.permalink, '_blank', 'noopener,noreferrer');
  }, [post.redditUrl, post.permalink]);

  return (
    <article
      className="reddit-hiring-card"
      onClick={openReddit}
      role="button"
      tabIndex={0}
      onKeyDown={(e) => e.key === 'Enter' && openReddit()}
    >
      <div className="reddit-hiring-card-media">
        <RedditPostImage
          thumbnailUrl={post.thumbnailUrl}
          imageUrls={post.imageUrls}
          title={post.title}
          subreddit={post.subreddit}
          mediaType={post.mediaType}
        />
        {post.hiringBadge && (
          <span className={`reddit-hiring-badge reddit-hiring-badge--${badgeClass(post.hiringBadge)}`}>
            {post.hiringBadge}
          </span>
        )}
      </div>
      <div className="reddit-hiring-card-body">
        <span className="reddit-hiring-sub">{post.subreddit}</span>
        <h3 className="reddit-hiring-title">{post.title}</h3>
        <div className="reddit-hiring-meta">
          <span>↑ {post.upvotes?.toLocaleString()}</span>
          <span>💬 {post.commentCount?.toLocaleString()}</span>
          <span>{post.timeAgo}</span>
        </div>
      </div>
    </article>
  );
});

const RedditHiringSection = memo(function RedditHiringSection({ posts }) {
  if (!posts?.length) return null;

  return (
    <section className="reddit-hiring" aria-label="Hiring opportunities">
      <div className="reddit-hiring-header">
        <div>
          <p className="reddit-hiring-eyebrow">career opportunities</p>
          <h2 className="reddit-hiring-title">hiring opportunities</h2>
          <p className="reddit-hiring-sub">
            Paid gigs and freelance work for video editors — sourced from hiring communities.
          </p>
        </div>
      </div>

      <div className="reddit-hiring-track">
        {posts.map((post) => (
          <HiringCard key={`hire-${post.id}-${post.subreddit}`} post={post} />
        ))}
      </div>
    </section>
  );
});

export default RedditHiringSection;
