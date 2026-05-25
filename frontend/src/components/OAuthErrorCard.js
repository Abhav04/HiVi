import React from 'react';
import { Link } from 'react-router-dom';
import './OAuthErrorCard.css';

const OAuthErrorCard = ({ error, onDismiss, apiUrl, oauthStatus }) => {
  if (!error) return null;

  const googleRedirect =
    oauthStatus?.googleRegistration?.redirectUri
    || oauthStatus?.googleRedirectUri
    || `${apiUrl}/login/oauth2/code/google`;
  const githubRedirect =
    oauthStatus?.githubRegistration?.redirectUri
    || oauthStatus?.githubRedirectUri
    || `${apiUrl}/login/oauth2/code/github`;
  const googleClientPrefix = oauthStatus?.googleClientIdPrefix;

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
          <div className="oauth-error-tech">
            <p>Add these <strong>exact</strong> URLs in your OAuth app settings:</p>
            <p>
              Google → Authorized redirect URIs: <code>{googleRedirect}</code>
            </p>
            <p>
              GitHub → Authorization callback URL: <code>{githubRedirect}</code>
            </p>
            {googleClientPrefix && (
              <p className="oauth-error-tech-note">
                Open the Google OAuth client whose ID starts with <code>{googleClientPrefix}</code> — must
                match <code>GOOGLE_CLIENT_ID</code> in backend/local.env.
              </p>
            )}
            {oauthStatus?.recommendedGoogleRedirectUris?.map((uri) => (
              <p key={uri} className="oauth-error-tech-note">
                Add redirect URI: <code>{uri}</code>
              </p>
            ))}
            {oauthStatus?.recommendedJavaScriptOrigins?.length > 0 && (
              <p className="oauth-error-tech-note">
                Authorized JavaScript origins (optional):{' '}
                {oauthStatus.recommendedJavaScriptOrigins.join(', ')}
              </p>
            )}
          </div>
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
