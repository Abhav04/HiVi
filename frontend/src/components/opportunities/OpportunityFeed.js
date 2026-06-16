import React, { useCallback, useEffect, useState } from 'react';
import { fetchOpportunities, postOpportunity } from '../../services/opportunitiesApi';
import { isLoggedIn } from '../../utils/auth';
import OpportunityCard from './OpportunityCard';
import TrendingHiringSection from './TrendingHiringSection';
import './OpportunityFeed.css';

export const CATEGORIES = [
  { id: 'ALL', label: 'All roles' },
  { id: 'REEL_EDITING', label: 'Reel editing' },
  { id: 'GAMING_EDITS', label: 'Gaming edits' },
  { id: 'ANIME_EDITING', label: 'Anime editing' },
  { id: 'YOUTUBE_LONGFORM', label: 'YouTube longform' },
  { id: 'MOTION_GRAPHICS', label: 'Motion graphics' },
  { id: 'SHORTS_EDITING', label: 'Shorts editing' },
  { id: 'FREELANCE', label: 'Freelance' },
  { id: 'INTERNSHIP', label: 'Internship' },
  { id: 'REMOTE_WORK', label: 'Remote work' },
];

const FAVICON_ORIGIN = 'https://www.google.com';

const SOURCES = [
  { id: 'ALL', label: 'All sources' },
  { id: 'REDDIT', label: 'Reddit' },
  { id: 'INTERNSHALA', label: 'Internshala' },
  { id: 'LINKEDIN', label: 'LinkedIn' },
  { id: 'USER', label: 'Community' },
];

const OpportunityFeed = () => {
  const [category, setCategory] = useState('ALL');
  const [source, setSource] = useState('ALL');
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showPost, setShowPost] = useState(false);
  const [postForm, setPostForm] = useState({
    title: '',
    company: '',
    description: '',
    applyUrl: '',
    payLabel: '',
    category: 'FREELANCE',
  });

  useEffect(() => {
    const link = document.createElement('link');
    link.rel = 'preconnect';
    link.href = FAVICON_ORIGIN;
    link.crossOrigin = 'anonymous';
    document.head.appendChild(link);
    return () => {
      document.head.removeChild(link);
    };
  }, []);

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const res = await fetchOpportunities({ page, size: 20, category, source });
      setData((prev) => (page === 0 ? res : { ...res, opportunities: [...(prev?.opportunities || []), ...res.opportunities] }));
    } catch (e) {
      setError(
        e.message?.includes('fetch')
          ? 'Cannot reach the API. Start the backend (./run-local.sh) and set REACT_APP_API_URL=http://localhost:8080'
          : e.message
      );
    } finally {
      setLoading(false);
    }
  }, [page, category, source]);

  useEffect(() => {
    load();
  }, [load]);

  useEffect(() => {
    setPage(0);
    setData(null);
  }, [category, source]);

  const handlePost = async (e) => {
    e.preventDefault();
    if (!isLoggedIn()) {
      setError('Sign in to post an opportunity for the community.');
      return;
    }
    try {
      await postOpportunity(postForm);
      setShowPost(false);
      setPostForm({ title: '', company: '', description: '', applyUrl: '', payLabel: '', category: 'FREELANCE' });
      setPage(0);
      load();
    } catch (err) {
      setError(err.message);
    }
  };

  const opportunities = data?.opportunities || [];

  return (
    <div className="opp-feed-page">
      <header className="opp-feed-header">
        <div>
          <p className="opp-feed-eyebrow">Creator career hub</p>
          <h1 className="opp-feed-title">
            Opportunities for <em>video editors</em>
          </h1>
          <p className="opp-feed-sub">
            Curated internships, freelance gigs, and hiring posts — summarized on HiVi, applied on the original platform.
          </p>
        </div>
        <button type="button" className="opp-post-btn" onClick={() => setShowPost(!showPost)}>
          {showPost ? 'Close' : 'Post opportunity'}
        </button>
      </header>

      {showPost && (
        <form className="opp-post-form" onSubmit={handlePost}>
          <input
            placeholder="Job title *"
            value={postForm.title}
            onChange={(e) => setPostForm({ ...postForm, title: e.target.value })}
            required
          />
          <input
            placeholder="Company / client"
            value={postForm.company}
            onChange={(e) => setPostForm({ ...postForm, company: e.target.value })}
          />
          <textarea
            placeholder="Short description"
            value={postForm.description}
            onChange={(e) => setPostForm({ ...postForm, description: e.target.value })}
            rows={3}
          />
          <input
            placeholder="Apply URL (https://) *"
            value={postForm.applyUrl}
            onChange={(e) => setPostForm({ ...postForm, applyUrl: e.target.value })}
            required
          />
          <input
            placeholder="Pay / stipend (optional)"
            value={postForm.payLabel}
            onChange={(e) => setPostForm({ ...postForm, payLabel: e.target.value })}
          />
          <button type="submit" className="opp-post-submit">
            Publish to HiVi
          </button>
        </form>
      )}

      <TrendingHiringSection
        trending={data?.trending}
        latest={data?.latest}
        loading={loading && !data}
      />

      <div className="opp-filters">
        <div className="opp-filter-group">
          {CATEGORIES.map((c) => (
            <button
              key={c.id}
              type="button"
              className={`opp-filter-chip ${category === c.id ? 'active' : ''}`}
              onClick={() => setCategory(c.id)}
            >
              {c.label}
            </button>
          ))}
        </div>
        <div className="opp-source-tabs">
          {SOURCES.map((s) => (
            <button
              key={s.id}
              type="button"
              className={`opp-source-tab ${source === s.id ? 'active' : ''}`}
              onClick={() => setSource(s.id)}
            >
              {s.label}
            </button>
          ))}
        </div>
      </div>

      {error && <p className="opp-feed-error">{error}</p>}

      <div className="opp-feed-grid">
        {opportunities.map((o, index) => (
          <OpportunityCard
            key={`${o.source}-${o.id || o.applyUrl}`}
            opportunity={o}
            logoPriority={index < 8}
          />
        ))}
      </div>

      {loading && opportunities.length === 0 && (
        <p className="opp-feed-loading">Loading opportunities…</p>
      )}

      {!loading && data?.hasMore && (
        <button type="button" className="opp-load-more" onClick={() => setPage((p) => p + 1)}>
          Load more
        </button>
      )}
    </div>
  );
};

export default OpportunityFeed;
