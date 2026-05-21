import React from 'react';
import { Link } from 'react-router-dom';
import './OAuthErrorCard.css';

const OAuthErrorCard = ({ error, onDismiss, apiUrl }) => {
  if (!error) return null;

  return (
    <div className="oauth-error-card" role="alert">
      <div className="oauth-error-icon">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
          <circle cx="12" cy="12" r="10" />
          <line x1="12" y1="8" x2="12" y2="12" />
          <line x1="12" y1="16" x2="12.01" y2="16" />
        </svg>
      </div>
      <div className="oauth-error-body">
        <p className="oauth-error-title">{error.title}</p>
        <p className="oauth-error-message">{error.message}</p>
        {error.action && <p className="oauth-error-action">{error.action}</p>}
        {error.type === 'config' && apiUrl && (
          <p className="oauth-error-tech">
            Developer: set callback URLs in Google/GitHub to{' '}
            <code>{apiUrl}/login/oauth2/code/google</code> and{' '}
            <code>{apiUrl}/login/oauth2/code/github</code>
          </p>
        )}
      </div>
      {onDismiss && (
        <button type="button" className="oauth-error-dismiss" onClick={onDismiss} aria-label="Dismiss">
          ×
        </button>
      )}
      <Link to="/login" className="oauth-error-retry" onClick={onDismiss}>
        try again →
      </Link>
    </div>
  );
};

export default OAuthErrorCard;
