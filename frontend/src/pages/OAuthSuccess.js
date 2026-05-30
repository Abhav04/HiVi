import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import AuthLoadingScreen from '../components/AuthLoadingScreen';
import { deriveUsername, normalizeToken } from '../utils/auth';
import { useAuth } from '../context/AuthContext';

function OAuthSuccess() {
  const navigate = useNavigate();
  const { login } = useAuth();
  const [progress, setProgress] = useState(20);

  useEffect(() => {
    const timer = setInterval(() => {
      setProgress((p) => Math.min(p + 12, 90));
    }, 200);

    const params = new URLSearchParams(window.location.search);
    const token = normalizeToken(params.get('token'));
    const name = params.get('name');
    const email = params.get('email');
    const role = params.get('role');

    const finish = async () => {
      await new Promise((r) => setTimeout(r, 300));

      if (name && email && token) {
        login(
          {
            name,
            email,
            username: deriveUsername(email),
            role: role || 'client',
            provider: email.endsWith('@github.local') ? 'github' : 'google',
            projects: [],
          },
          token
        );
        setProgress(100);
        navigate('/dashboard', { replace: true });
      } else if (token) {
        login(null, token);
        setProgress(100);
        navigate('/dashboard', { replace: true });
      } else {
        navigate('/login?error=oauth_failed', { replace: true });
      }
    };

    finish();
    return () => clearInterval(timer);
  }, [navigate, login]);

  return (
    <AuthLoadingScreen
      provider="google"
      progress={progress}
      statusText="Setting up your dashboard..."
    />
  );
}

export default OAuthSuccess;
