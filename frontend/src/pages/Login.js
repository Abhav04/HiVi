import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './Auth.css';

const Login = () => {
  const navigate = useNavigate();
   const handleGoogleLogin = () => {
    window.location.href = "http://localhost:8080/oauth2/authorization/google";
  };


  const [form, setForm] = useState({ email: '', password: '' });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [showPass, setShowPass] = useState(false);

  const validate = () => {
    const e = {};
    if (!form.email) e.email = 'email is required';
    else if (!/\S+@\S+\.\S+/.test(form.email)) e.email = 'enter a valid email';
    if (!form.password) e.password = 'password is required';
    else if (form.password.length < 6) e.password = 'minimum 6 characters';
    return e;
  };

  const handleChange = (field) => (ev) => {
    setForm({ ...form, [field]: ev.target.value });
    if (errors[field]) setErrors({ ...errors, [field]: '' });
  };

  const handleSubmit = async () => {
    const e = validate();
    if (Object.keys(e).length) { setErrors(e); return; }
    setLoading(true);
    await new Promise(r => setTimeout(r, 1800));
    setLoading(false);
    navigate('/dashboard');
  };

  return (
    <div className="auth-page">
      {/* Left panel */}
      <div className="auth-left">
        <div className="auth-left-content">
          <Link to="/" className="auth-logo">
            <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
              <polygon points="14,2 26,8 26,20 14,26 2,20 2,8" stroke="#c9a84c" strokeWidth="1.5" fill="none"/>
              <circle cx="14" cy="14" r="2" fill="#c9a84c"/>
            </svg>
            <span>HIVI</span>
          </Link>

          <div className="auth-left-hero">
            <h2 className="auth-left-title">
              where the best<br/>
              <em>editors work</em>
            </h2>
            <p className="auth-left-sub">
              Sign in to access your exclusive dashboard — projects, earnings, and your curated editor profile.
            </p>
          </div>

          <div className="auth-testimonial">
            <div className="auth-t-quote">"</div>
            <p className="auth-t-text">HIVI changed how I work. Better projects, better pay, no middlemen.</p>
            <div className="auth-t-author">
              <div className="auth-t-av">AK</div>
              <div>
                <div className="auth-t-name">Aryan Kapoor</div>
                <div className="auth-t-role">Cinematic Editor, Mumbai</div>
              </div>
            </div>
          </div>

          <div className="auth-left-pillars">
            {[...Array(7)].map((_, i) => (
              <div key={i} className="auth-pillar" style={{ animationDelay: `${i * 0.1}s`, height: `${40 + i * 8}%` }} />
            ))}
          </div>
        </div>
      </div>

      {/* Right panel */}
      <div className="auth-right">
        <div className="auth-form-wrap animate-fadeUp">
          <div className="auth-form-header">
            <div className="auth-form-eyebrow">welcome back</div>
            <h1 className="auth-form-title">sign in</h1>
            <p className="auth-form-sub">
              New here?{' '}
              <Link to="/signup" className="auth-link">create an account →</Link>
            </p>
          </div>

          <div className="auth-form">
            {/* Email */}
            <div className={`field-group ${errors.email ? 'error' : form.email ? 'filled' : ''}`}>
              <label className="field-label">email address</label>
              <div className="field-wrap">
                <input
                  type="email"
                  className="field-input"
                  placeholder="you@example.com"
                  value={form.email}
                  onChange={handleChange('email')}
                  onKeyDown={(e) => e.key === 'Enter' && handleSubmit()}
                  autoComplete="email"
                />
                {form.email && !errors.email && (
                  <div className="field-check">
                    <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#27ae60" strokeWidth="3">
                      <polyline points="20 6 9 17 4 12"/>
                    </svg>
                  </div>
                )}
              </div>
              {errors.email && <span className="field-error">{errors.email}</span>}
            </div>

            {/* Password */}
            <div className={`field-group ${errors.password ? 'error' : form.password ? 'filled' : ''}`}>
              <div className="field-label-row">
                <label className="field-label">password</label>
<a href="/forgot-password" className="forgot-link">
  forgot password?
</a>              </div>
              <div className="field-wrap">
                <input
                  type={showPass ? 'text' : 'password'}
                  className="field-input"
                  placeholder="••••••••"
                  value={form.password}
                  onChange={handleChange('password')}
                  onKeyDown={(e) => e.key === 'Enter' && handleSubmit()}
                  autoComplete="current-password"
                />
                <button className="field-toggle" onClick={() => setShowPass(!showPass)} type="button">
                  {showPass ? (
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/>
                      <line x1="1" y1="1" x2="23" y2="23"/>
                    </svg>
                  ) : (
                    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                      <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                      <circle cx="12" cy="12" r="3"/>
                    </svg>
                  )}
                </button>
              </div>
              {errors.password && <span className="field-error">{errors.password}</span>}
            </div>

            <button
              className={`auth-submit ${loading ? 'loading' : ''}`}
              onClick={handleSubmit}
              disabled={loading}
            >
              {loading ? (
                <span className="submit-loading">
                  <span className="spinner" />
                  signing in...
                </span>
              ) : 'sign in →'}
            </button>

            <div className="auth-divider"><span>or continue with</span></div>

            <div className="oauth-row">
             <button className="oauth-btn" onClick={handleGoogleLogin}>
                <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z" fill="#4285F4"/>
                  <path d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z" fill="#34A853"/>
                  <path d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z" fill="#FBBC05"/>
                  <path d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z" fill="#EA4335"/>
                </svg>
                Google
              </button>
              <button className="oauth-btn">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M16.365 1.43c0 1.14-.493 2.27-1.177 3.08-.744.9-1.99 1.57-2.987 1.57-.12 0-.23-.02-.3-.03-.01-.06-.04-.22-.04-.39 0-1.15.572-2.27 1.206-2.98.804-.94 2.142-1.64 3.248-1.68.03.13.05.28.05.43zm4.565 15.71c-.03.07-.463 1.58-1.518 3.12-.945 1.34-1.94 2.71-3.43 2.71-1.517 0-1.9-.88-3.63-.88-1.698 0-2.302.91-3.67.91-1.377 0-2.332-1.26-3.428-2.8-1.287-1.82-2.323-4.63-2.323-7.28 0-3.55 2.488-5.43 4.936-5.43 1.518 0 2.802.9 3.722.9.896 0 2.297-1.02 4.01-1.02.65 0 2.497.06 3.785 1.43zm-2.73-15.73c-.744.9-2.274 1.8-3.276 1.8-.1 0-.2-.02-.28-.04-.01-.08-.03-.19-.03-.32 0-1.08.6-2.25 1.285-3.03.717-.83 2.012-1.53 3.023-1.57.01.1.03.23.03.37 0 1.06-.48 2.18-1.18 2.97l.43-.18z"/>
                </svg>
                Apple
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;