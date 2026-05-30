import React, { useEffect, useState, useCallback } from 'react';
import { useSearchParams } from 'react-router-dom';
import AuthLoadingScreen from '../components/AuthLoadingScreen';
import { buildOAuthBeginUrl, startOAuthFlow } from '../utils/oauthFlow';

const AuthConnecting = () => {
  const [searchParams] = useSearchParams();
  const provider = searchParams.get('provider') || 'google';
  const [progress, setProgress] = useState(10);
  const [statusText, setStatusText] = useState('Preparing secure sign-in...');

  const skipToOAuth = useCallback(() => {
    window.location.href = buildOAuthBeginUrl(provider);
  }, [provider]);

  useEffect(() => {
    let cancelled = false;

    startOAuthFlow(provider, {
      onProgress: (p) => {
        if (!cancelled && typeof p === 'number') setProgress(p);
      },
      onStatus: (text) => {
        if (!cancelled) setStatusText(text);
      },
    });

    return () => {
      cancelled = true;
    };
  }, [provider]);

  return (
    <AuthLoadingScreen
      provider={provider}
      progress={progress}
      statusText={statusText}
      onSkip={skipToOAuth}
      skipAfterMs={6000}
    />
  );
};

export default AuthConnecting;
