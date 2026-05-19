import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { saveUser } from '../utils/auth';

function OAuthSuccess() {
  const navigate = useNavigate();

  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    const token = params.get('token');
    const name = params.get('name');
    const email = params.get('email');
    const role = params.get('role');

    if (token) {
      localStorage.setItem('token', token);
    }

    if (name && email) {
      saveUser({
        name,
        email,
        role: role || 'client',
        provider: email.endsWith('@github.local') ? 'github' : 'google',
        projects: [],
      });
      navigate('/dashboard');
    } else if (token) {
      navigate('/dashboard');
    } else {
      navigate('/login');
    }
  }, [navigate]);

  return <div>Logging in...</div>;
}

export default OAuthSuccess;
