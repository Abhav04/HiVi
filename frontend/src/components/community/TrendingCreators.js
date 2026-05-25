import React, { memo } from 'react';
import { Link } from 'react-router-dom';
import './TrendingCreators.css';

const TrendingCreators = memo(function TrendingCreators({ creators, onFollow }) {
  if (!creators?.length) return null;

  return (
    <section className="trending-creators">
      <p className="trending-creators-eyebrow">discovery</p>
      <h3 className="trending-creators-title">trending creators</h3>
      <div className="trending-creators-track">
        {creators.map((c) => (
          <Link
            key={c.id}
            to={`/community/creator/${c.username}`}
            className="trending-creator-chip"
          >
            {c.avatarUrl ? (
              <img src={c.avatarUrl} alt="" className="tc-avatar-img" />
            ) : (
              <span className="tc-avatar">{(c.displayName || c.username)?.[0]}</span>
            )}
            <div className="tc-info">
              <span className="tc-name">{c.displayName}</span>
              <span className="tc-niche">{c.niche || 'creator'}</span>
            </div>
            {c.availableForWork && <span className="tc-badge">open</span>}
          </Link>
        ))}
      </div>
    </section>
  );
});

export default TrendingCreators;
