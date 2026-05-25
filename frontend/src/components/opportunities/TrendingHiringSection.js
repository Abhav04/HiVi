import React from 'react';
import CompanyLogo from './CompanyLogo';
import OpportunityCard from './OpportunityCard';
import './TrendingHiringSection.css';

const TrendingHiringSection = ({ trending = [], latest = [], loading = false }) => {
  const hasTrending = trending.length > 0;
  const hasLatest = latest.length > 0;

  if (loading) {
    return (
      <section className="opp-trending-section opp-trending-section--loading" aria-busy="true">
        <div className="opp-trending-block">
          <div className="opp-trending-head">
            <h2>Trending hiring</h2>
            <p>Loading editor roles…</p>
          </div>
          <div className="opp-trending-scroll">
            {[1, 2, 3].map((n) => (
              <div key={n} className="opp-trending-skeleton" />
            ))}
          </div>
        </div>
      </section>
    );
  }

  if (!hasTrending && !hasLatest) {
    return null;
  }

  return (
    <section className="opp-trending-section">
      {hasTrending && (
        <div className="opp-trending-block">
          <div className="opp-trending-head">
            <h2>Trending hiring</h2>
            <p>Most engaged editor roles right now</p>
          </div>
          <div className="opp-trending-scroll">
            {trending.slice(0, 4).map((o) => (
              <div key={o.id || o.applyUrl} className="opp-trending-card-wrap">
                <OpportunityCard opportunity={o} />
              </div>
            ))}
          </div>
        </div>
      )}

      {hasLatest && (
        <div className="opp-trending-block opp-trending-block--latest">
          <div className="opp-trending-head">
            <h2>Latest openings</h2>
            <p>Fresh posts from Reddit & partners</p>
          </div>
          <ul className="opp-latest-list">
            {latest.slice(0, 6).map((o) => (
              <li key={o.id || o.applyUrl}>
                <a href={o.applyUrl} target="_blank" rel="noopener noreferrer" className="opp-latest-item">
                  <CompanyLogo
                    size="sm"
                    logoUrl={o.logoUrl}
                    logoFallbackUrl={o.logoFallbackUrl}
                    initials={o.companyInitials}
                    company={o.company}
                    source={o.source}
                  />
                  <span className="opp-latest-text">
                    <span className="opp-latest-title">{o.title}</span>
                    <span className="opp-latest-meta">
                      {o.company} · {o.timeAgo}
                    </span>
                  </span>
                </a>
              </li>
            ))}
          </ul>
        </div>
      )}
    </section>
  );
};

export default TrendingHiringSection;
