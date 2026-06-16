import React, { useMemo, useState } from 'react';
import './CompanyLogo.css';

const SOURCE_STYLES = {
  REDDIT: 'reddit',
  LINKEDIN: 'linkedin',
  INTERNSHALA: 'internshala',
  USER: 'user',
};

/** Prefer fast Google favicon CDN over slow Clearbit URLs from older API data */
function normalizeLogoSrc(logoUrl, logoFallbackUrl) {
  const candidates = [logoUrl, logoFallbackUrl].filter(Boolean);
  for (const url of candidates) {
    if (url.includes('logo.clearbit.com')) {
      const domain = url.replace(/^https?:\/\/logo\.clearbit\.com\//i, '').split('/')[0];
      if (domain) {
        return `https://www.google.com/s2/favicons?domain=${encodeURIComponent(domain)}&sz=128`;
      }
    }
    if (url.includes('google.com/s2/favicons')) {
      return url;
    }
  }
  return candidates[0] || null;
}

const CompanyLogo = ({
  logoUrl,
  logoFallbackUrl,
  initials,
  company,
  source,
  size = 'md',
  priority = false,
}) => {
  const [loaded, setLoaded] = useState(false);
  const [failed, setFailed] = useState(false);

  const label = company || 'Company';
  const styleKey = SOURCE_STYLES[source] || 'default';
  const displayInitials = initials || label.substring(0, 2).toUpperCase();

  const src = useMemo(
    () => normalizeLogoSrc(logoUrl, logoFallbackUrl),
    [logoUrl, logoFallbackUrl]
  );

  const showImage = src && !failed;

  return (
    <div className={`opp-logo opp-logo--${size} opp-logo--${styleKey}`} title={label}>
      <span className="opp-logo-initials" aria-hidden="true">
        {displayInitials}
      </span>
      {showImage && (
        <img
          src={src}
          alt=""
          className={`opp-logo-img ${loaded ? 'opp-logo-img--loaded' : ''}`}
          onLoad={() => setLoaded(true)}
          onError={() => setFailed(true)}
          loading={priority ? 'eager' : 'lazy'}
          decoding="async"
          fetchPriority={priority ? 'high' : 'auto'}
          referrerPolicy="no-referrer"
        />
      )}
    </div>
  );
};

export default CompanyLogo;
