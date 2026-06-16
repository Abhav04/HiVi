import React from 'react';
import CompanyLogo from './CompanyLogo';
import './OpportunityCard.css';

const SOURCE_LABELS = {
  REDDIT: 'Reddit',
  INTERNSHALA: 'Internshala',
  LINKEDIN: 'LinkedIn',
  USER: 'HiVi Community',
};

const badgeClass = (badge) => {
  const key = badge.toLowerCase().replace(/\s+/g, '-');
  return `opp-badge opp-badge--${key}`;
};

const formatWorkMode = (workMode) => {
  if (!workMode || workMode === 'UNKNOWN') return null;
  return workMode.replace('_', ' ').toLowerCase();
};

const OpportunityCard = ({ opportunity, logoPriority = false }) => {
  const {
    title,
    company,
    description,
    applyUrl,
    payLabel,
    workMode,
    tags = [],
    badges = [],
    source,
    timeAgo,
    trendingScore,
    logoUrl,
    logoFallbackUrl,
    companyInitials,
  } = opportunity;

  const locationLabel = formatWorkMode(workMode);

  return (
    <article className="opp-card">
      <div className="opp-card-glow" aria-hidden="true" />
      <header className="opp-card-header">
        <CompanyLogo
          logoUrl={logoUrl}
          logoFallbackUrl={logoFallbackUrl}
          initials={companyInitials}
          company={company}
          source={source}
          priority={logoPriority}
        />
        <div className="opp-card-header-text">
          <div className="opp-card-title-row">
            <h3 className="opp-card-title">{title}</h3>
            <span className="opp-card-source">{SOURCE_LABELS[source] || source}</span>
          </div>
          <p className="opp-card-company-line">
            <span className="opp-card-company">{company || 'Client'}</span>
            {locationLabel && (
              <>
                <span className="opp-card-dot">·</span>
                <span className="opp-card-location">{locationLabel}</span>
              </>
            )}
          </p>
        </div>
      </header>

      <div className="opp-card-badges">
        {badges.map((b) => (
          <span key={b} className={badgeClass(b)}>
            {b}
          </span>
        ))}
      </div>

      {payLabel && <p className="opp-card-pay">{payLabel}</p>}

      <p className="opp-card-desc">{description}</p>

      {tags.length > 0 && (
        <div className="opp-card-tags">
          {tags.map((tag) => (
            <span key={tag} className="opp-tag">
              {tag}
            </span>
          ))}
        </div>
      )}

      <footer className="opp-card-footer">
        <span className="opp-card-meta">
          {timeAgo || 'Recently'}
          {trendingScore > 40 && <span className="opp-card-hot"> · trending</span>}
        </span>
        <a
          href={applyUrl}
          target="_blank"
          rel="noopener noreferrer"
          className="opp-apply-btn"
        >
          Apply now
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6" />
            <polyline points="15 3 21 3 21 9" />
            <line x1="10" y1="14" x2="21" y2="3" />
          </svg>
        </a>
      </footer>
    </article>
  );
};

export default OpportunityCard;
