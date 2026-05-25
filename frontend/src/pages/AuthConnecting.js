import React, { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import AuthLoadingScreen from '../components/AuthLoadingScreen';
import { getApiUrl, getOAuthApiUrl } from '../utils/auth';
import { wakeBackend } from '../utils/wakeBackend';
import { fetchOAuthStatus, getOAuthBlockers } from '../utils/oauthStatus';

const AuthConnecting = () => {
  const [searchParams] = useSearchParams();
  const provider = searchParams.get('provider') || 'google';
  const [progress, setProgress] = useState(8);
  const [statusText, setStatusText] = useState('Waking up secure servers...');

  useEffect(() => {
    let cancelled = false;
    const apiUrl = getApiUrl();

    const run = async () => {
      setProgress(15);
      setStatusText('Waking up secure servers...');

      const progressTimer = setInterval(() => {
        setProgress((p) => {
          if (p >= 88) return p;
          return p + Math.random() * 4 + 1;
        });
      }, 800);

      const { ready } = await wakeBackend(apiUrl, 90000);

      if (cancelled) {
        clearInterval(progressTimer);
        return;
      }

      let oauthStatus = null;
      try {
        oauthStatus = await fetchOAuthStatus(apiUrl);
      } catch {
        // continue — backend may still work
      }

      const blockers = getOAuthBlockers(oauthStatus, provider, apiUrl);
      if (blockers.length > 0) {
        clearInterval(progressTimer);
        const code = provider === 'github' && oauthStatus && !oauthStatus.githubClientSecretSet
          ? 'github_secret_missing'
          : 'invalid_client';
        const detail = encodeURIComponent(blockers[0]);
        window.location.href = `/login?error=${encodeURIComponent(code)}&detail=${detail}`;
        return;
      }

      clearInterval(progressTimer);
      setProgress(ready ? 92 : 75);
      setStatusText(ready ? 'Server ready — opening sign in...' : 'Connecting anyway...');

      await new Promise((r) => setTimeout(r, 400));

      if (!cancelled) {
        setProgress(100);
        const frontend = encodeURIComponent(window.location.origin);
        window.location.href = `${getOAuthApiUrl()}/oauth/begin?provider=${provider}&frontend=${frontend}`;
      }
    };

    run();
    return () => {
      cancelled = true;
    };
  }, [provider]);

  return <AuthLoadingScreen provider={provider} progress={progress} statusText={statusText} />;
};

export default AuthConnecting;
