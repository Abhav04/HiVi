import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import './Auth.css';

const Signup = () => {
  const navigate = useNavigate();
  const [role, setRole] = useState('');
  const [step, setStep] = useState(1); // 1: role select, 2: form
  const [form, setForm] = useState({ name: '', email: '', password: '', confirm: '' });
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);
  const [showPass, setShowPass] = useState(false);

  const validate = () => {
    const e = {};
    if (!form.name.trim()) e.name = 'full name is required';
    if (!form.email) e.email = 'email is required';
    else if (!/\S+@\S+\.\S+/.test(form.email)) e.email = 'enter a valid email';
    if (!form.password) e.password = 'password is required';
    else if (form.password.length < 8) e.password = 'minimum 8 characters';
    if (!form.confirm) e.confirm = 'please confirm your password';
    else if (form.confirm !== form.password) e.confirm = 'passwords do not match';
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
    await new Promise(r => setTimeout(r, 2000));
    setLoading(false);
    navigate('/dashboard');
  };

  const pwStrength = () => {
    const p = form.password;
    if (!p) return 0;
    let s = 0;
    if (p.length >= 8) s++;
    if (/[A-Z]/.test(p)) s++;
    if (/[0-9]/.test(p)) s++;
    if (/[^A-Za-z0-9]/.test(p)) s++;
    return s;
  };

  const strengthLabel = ['', 'weak', 'fair', 'good', 'strong'];
  const strengthColor = ['', '#c0392b', '#e67e22', '#f1c40f', '#27ae60'];
  const strength = pwStrength();

  return (
    <div className="auth-page">
      {/* Left */}
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
              join the most<br/>
              <em>exclusive network</em>
            </h2>
            <p className="auth-left-sub">
              Whether you're a world-class editor or a brand seeking one — HIVI is where exceptional meets exceptional.
            </p>
          </div>

          <div className="auth-perks">
            {[
              { icon: '◆', text: 'vetted editor profiles only' },
              { icon: '◆', text: 'escrow payment protection' },
              { icon: '◆', text: 'zero platform commission' },
              { icon: '◆', text: 'direct client connection' },
            ].map((p, i) => (
              <div key={i} className="perk-item" style={{ animationDelay: `${i * 0.1}s` }}>
                <span className="perk-icon">{p.icon}</span>
                <span className="perk-text">{p.text}</span>
              </div>
            ))}
          </div>

          <div className="auth-left-pillars">
            {[...Array(7)].map((_, i) => (
              <div key={i} className="auth-pillar" style={{ animationDelay: `${i * 0.1}s`, height: `${40 + i * 8}%` }} />
            ))}
          </div>
        </div>
      </div>

      {/* Right */}
      <div className="auth-right">
        <div className="auth-form-wrap animate-fadeUp">

          {step === 1 ? (
            /* Step 1: Role Selection */
            <div className="role-step">
              <div className="auth-form-eyebrow">getting started</div>
              <h1 className="auth-form-title">i am a...</h1>
              <p className="auth-form-sub">Choose your role to get the right experience</p>

              <div className="role-cards">
                <button
                  className={`role-card ${role === 'editor' ? 'active' : ''}`}
                  onClick={() => setRole('editor')}
                >
                  <div className="role-icon">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                      <path d="M15 10l4.553-2.069A1 1 0 0 1 21 8.87v6.26a1 1 0 0 1-1.447.899L15 14M3 8a2 2 0 0 1 2-2h8a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8z"/>
                    </svg>
                  </div>
                  <div className="role-label">video editor</div>
                  <div className="role-desc">I want to find projects and grow my editing career</div>
                  {role === 'editor' && <div className="role-check">✓</div>}
                </button>

                <button
                  className={`role-card ${role === 'client' ? 'active' : ''}`}
                  onClick={() => setRole('client')}
                >
                  <div className="role-icon">
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                      <rect x="2" y="3" width="20" height="14" rx="2"/><path d="M8 21h8m-4-4v4"/>
                    </svg>
                  </div>
                  <div className="role-label">client / brand</div>
                  <div className="role-desc">I need to hire exceptional video editors for my projects</div>
                  {role === 'client' && <div className="role-check">✓</div>}
                </button>
              </div>

              <button
                className={`auth-submit ${!role ? 'disabled' : ''}`}
                onClick={() => role && setStep(2)}
                disabled={!role}
              >
                continue →
              </button>

              <p className="auth-form-sub" style={{ marginTop: 16, textAlign: 'center' }}>
                Already have an account? <Link to="/login" className="auth-link">sign in →</Link>
              </p>
            </div>
          ) : (
            /* Step 2: Form */
            <>
              <div className="auth-form-header">
                <button className="back-btn" onClick={() => setStep(1)}>
                  <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M19 12H5M12 5l-7 7 7 7"/>
                  </svg>
                  back
                </button>
                <div className="auth-form-eyebrow">
                  {role === 'editor' ? 'join as editor' : 'join as client'}
                </div>
                <h1 className="auth-form-title">create account</h1>
                <p className="auth-form-sub">
                  Already have one?{' '}
                  <Link to="/login" className="auth-link">sign in →</Link>
                </p>
              </div>

              <div className="auth-form">
                {/* Name */}
                <div className={`field-group ${errors.name ? 'error' : form.name ? 'filled' : ''}`}>
                  <label className="field-label">full name</label>
                  <div className="field-wrap">
                    <input
                      type="text"
                      className="field-input"
                      placeholder="Your Full Name"
                      value={form.name}
                      onChange={handleChange('name')}
                      autoComplete="name"
                    />
                  </div>
                  {errors.name && <span className="field-error">{errors.name}</span>}
                </div>

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
                  <label className="field-label">password</label>
                  <div className="field-wrap">
                    <input
                      type={showPass ? 'text' : 'password'}
                      className="field-input"
                      placeholder="••••••••"
                      value={form.password}
                      onChange={handleChange('password')}
                      autoComplete="new-password"
                    />
                    <button className="field-toggle" onClick={() => setShowPass(!showPass)} type="button">
                      <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                        {showPass
                          ? <><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/></>
                          : <><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></>
                        }
                      </svg>
                    </button>
                  </div>
                  {form.password && (
                    <div className="pw-strength">
                      <div className="pw-bars">
                        {[1,2,3,4].map(n => (
                          <div
                            key={n}
                            className="pw-bar"
                            style={{ background: n <= strength ? strengthColor[strength] : 'var(--border2)' }}
                          />
                        ))}
                      </div>
                      <span className="pw-label" style={{ color: strengthColor[strength] }}>
                        {strengthLabel[strength]}
                      </span>
                    </div>
                  )}
                  {errors.password && <span className="field-error">{errors.password}</span>}
                </div>

                {/* Confirm */}
                <div className={`field-group ${errors.confirm ? 'error' : form.confirm && form.confirm === form.password ? 'filled' : ''}`}>
                  <label className="field-label">confirm password</label>
                  <div className="field-wrap">
                    <input
                      type="password"
                      className="field-input"
                      placeholder="••••••••"
                      value={form.confirm}
                      onChange={handleChange('confirm')}
                      onKeyDown={(e) => e.key === 'Enter' && handleSubmit()}
                      autoComplete="new-password"
                    />
                    {form.confirm && form.confirm === form.password && (
                      <div className="field-check">
                        <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="#27ae60" strokeWidth="3">
                          <polyline points="20 6 9 17 4 12"/>
                        </svg>
                      </div>
                    )}
                  </div>
                  {errors.confirm && <span className="field-error">{errors.confirm}</span>}
                </div>

                <p className="terms-note">
                  by creating an account, you agree to our{' '}
                  <a href="#" className="auth-link">terms of service</a> and{' '}
                  <a href="#" className="auth-link">privacy policy</a>
                </p>

                <button
                  className={`auth-submit ${loading ? 'loading' : ''}`}
                  onClick={handleSubmit}
                  disabled={loading}
                >
                  {loading ? (
                    <span className="submit-loading">
                      <span className="spinner" />
                      creating account...
                    </span>
                  ) : 'create account →'}
                </button>
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default Signup;