import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import AuthGridPanel from './AuthGridPanel';
import './AuthLoadingScreen.css';

const MESSAGES = [
  'Connecting creators worldwide...',
  'Curating your creative workspace...',
  'Matching you with top editors...',
  'Preparing your dashboard...',
  'Almost there...',
];

const HIVI_LOGO = (
  <svg width="28" height="28" viewBox="0 0 28 28" fill="none" aria-hidden>
    <polygon points="14,2 26,8 26,20 14,26 2,20 2,8" stroke="#c9a84c" strokeWidth="1.5" fill="none" />
    <circle cx="14" cy="14" r="2" fill="#c9a84c" />
  </svg>
);

const AuthLoadingScreen = ({
  provider = 'google',
  progress = 0,
  statusText,
  onSkip,
  skipAfterMs = 6000,
}) => {
  const [msgIndex, setMsgIndex] = useState(0);
  const [showSkip, setShowSkip] = useState(false);
  const providerLabel = provider === 'github' ? 'GitHub' : 'Google';

  useEffect(() => {
    const id = setInterval(() => {
      setMsgIndex((i) => (i + 1) % MESSAGES.length);
    }, 2800);
    return () => clearInterval(id);
  }, []);

  useEffect(() => {
    if (!onSkip) return undefined;
    const t = setTimeout(() => setShowSkip(true), skipAfterMs);
    return () => clearTimeout(t);
  }, [onSkip, skipAfterMs]);

  const displayProgress = Math.min(100, Math.max(0, progress));

  return (
    <div className="auth-loading-page">
      <div className="auth-loading-left">
        <div className="auth-loading-left-inner">
          <Link to="/" className="auth-logo">
            {HIVI_LOGO}
            <span>HIVI</span>
          </Link>

          <div className="auth-loading-hero">
            <p className="auth-loading-eyebrow">secure sign-in</p>
            <h1 className="auth-loading-title">
              welcome to the<br />
              <em>creator network</em>
            </h1>
            <p className="auth-loading-tagline">
              The premium platform where visionary brands meet world-class editors.
            </p>
          </div>

          <div className="auth-loading-status">
            <p className="auth-loading-provider">
              Continuing with <span>{providerLabel}</span>
            </p>
            <p className="auth-loading-message" key={msgIndex}>
              {statusText || MESSAGES[msgIndex]}
            </p>
            <div className="auth-loading-progress-track">
              <div
                className="auth-loading-progress-bar"
                style={{ width: `${displayProgress}%` }}
              />
            </div>
            <p className="auth-loading-hint">
              {displayProgress < 30
                ? 'Waking up servers — usually under 15 seconds'
                : displayProgress < 85
                  ? 'Establishing secure connection...'
                  : 'Redirecting to sign in...'}
            </p>
            {showSkip && onSkip && (
              <button type="button" className="auth-loading-skip" onClick={onSkip}>
                Continue to sign in now →
              </button>
            )}
          </div>

          <p className="auth-loading-footer">
            Trusted by creators who demand cinematic quality.
          </p>
        </div>
      </div>

      <div className="auth-loading-right">
        <AuthGridPanel />
        <div className="auth-loading-right-label">
          <span className="auth-loading-pulse" />
          application loading
        </div>
      </div>
    </div>
  );
};

export default AuthLoadingScreen;
