import React, { memo } from 'react';
import { Link } from 'react-router-dom';
import CreatorAvatar from './CreatorAvatar';
import { formatCount } from '../../utils/communityFormat';
import './FeedSidebar.css';

const FeedSidebar = memo(function FeedSidebar({ trendingCreators = [], topLiked = [] }) {
  return (
    <aside className="feed-sidebar">
      {trendingCreators.length > 0 && (
        <section className="feed-sidebar-card feed-sidebar-card--glow">
          <h3 className="feed-sidebar-title">Trending creators</h3>
          <p className="feed-sidebar-sub">Editors gaining momentum this week</p>
          <ul className="feed-sidebar-list">
            {trendingCreators.slice(0, 6).map((c, i) => (
              <li key={c.id}>
                <Link to={`/community/creator/${c.username}`} className="feed-sidebar-creator">
                  <span className="feed-sidebar-rank">{i + 1}</span>
                  <CreatorAvatar author={c} size="sm" link={false} />
                  <div className="feed-sidebar-creator-info">
                    <span className="feed-sidebar-creator-name">{c.displayName}</span>
                    <span className="feed-sidebar-creator-meta">{c.niche || 'Video editor'}</span>
                  </div>
                  {c.availableForWork && <span className="feed-sidebar-open">Open</span>}
                </Link>
              </li>
            ))}
          </ul>
        </section>
      )}

      {topLiked.length > 0 && (
        <section className="feed-sidebar-card">
          <h3 className="feed-sidebar-title">Most liked edits</h3>
          <ul className="feed-sidebar-top">
            {topLiked.slice(0, 4).map((p) => (
              <li key={p.id}>
                <Link to={`/community/creator/${p.author?.username}`} className="feed-sidebar-top-item">
                  <span className="feed-sidebar-top-title">{p.title}</span>
                  <span className="feed-sidebar-top-meta">
                    {p.author?.displayName} · ♥ {formatCount(p.likeCount)}
                  </span>
                </Link>
              </li>
            ))}
          </ul>
        </section>
      )}

      <section className="feed-sidebar-card feed-sidebar-card--tip">
        <h3 className="feed-sidebar-title">Grow on HiVi</h3>
        <p className="feed-sidebar-tip">
          Share breakdowns, reels, and portfolio drops. Clients discover editors through real work—not
          résumés alone.
        </p>
        <Link to="/reddit-trends" className="feed-sidebar-link">
          Explore Reddit trends →
        </Link>
      </section>
    </aside>
  );
});

export default FeedSidebar;
