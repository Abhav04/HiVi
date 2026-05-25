import React, { useState } from 'react';
import './CompanyLogo.css';

const SOURCE_STYLES = {
  REDDIT: 'reddit',
  LINKEDIN: 'linkedin',
  INTERNSHALA: 'internshala',
  USER: 'user',
};

const CompanyLogo = ({ logoUrl, logoFallbackUrl, initials, company, source, size = 'md' }) => {
  const [stage, setStage] = useState(0);

  const label = company || 'Company';
  const styleKey = SOURCE_STYLES[source] || 'default';
  const displayInitials = initials || label.substring(0, 2).toUpperCase();

  const handleError = () => {
    setStage((s) => s + 1);
  };

  let src = null;
  if (stage === 0 && logoUrl) src = logoUrl;
  else if (stage <= 1 && logoFallbackUrl) src = logoFallbackUrl;

  return (
    <div className={`opp-logo opp-logo--${size} opp-logo--${styleKey}`} title={label}>
      {src ? (
        <img
          src={src}
          alt={`${label} logo`}
          className="opp-logo-img"
          onError={handleError}
          loading="lazy"
          referrerPolicy="no-referrer"
        />
      ) : (
        <span className="opp-logo-initials" aria-hidden="true">
          {displayInitials}
        </span>
      )}
    </div>
  );
};

export default CompanyLogo;
