import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import NavbarUserMenu from './NavbarUserMenu';
import { getInitials } from '../utils/auth';
import './Navbar.css';

const GUEST_LINKS = [
  { to: '/feed', label: 'discover', hash: false },
  { href: '#how', label: 'how it works', hash: true },
  { href: '#talent', label: 'for talent', hash: true },
  { href: '#hire', label: 'hire', hash: true },
];

const AUTH_LINKS = [
  { to: '/community', label: 'community' },
  { to: '/opportunities', label: 'opportunities' },
  { to: '/dashboard', label: 'dashboard' },
  { to: '/feed', label: 'discover' },
];

const Navbar = ({ transparent = false }) => {
  const [scrolled, setScrolled] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();
  const { ready, isAuthenticated, user, logout } = useAuth();

  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 40);
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  useEffect(() => {
    setMenuOpen(false);
  }, [location.pathname, isAuthenticated]);

  const isActive = (path) => location.pathname === path || location.pathname.startsWith(`${path}/`);

  const handleMobileLogout = () => {
    setMenuOpen(false);
    logout();
    navigate('/');
  };

  const showAuthChrome = ready && isAuthenticated;

  return (
    <nav className={`navbar ${scrolled ? 'scrolled' : ''} ${transparent ? 'transparent' : ''}`}>
      <div className="navbar-inner">
        <Link to={showAuthChrome ? '/dashboard' : '/'} className="navbar-logo">
          <div className="logo-icon">
            <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
              <polygon points="14,2 26,8 26,20 14,26 2,20 2,8" stroke="#c9a84c" strokeWidth="1.5" fill="none" />
              <polygon points="14,7 21,11 21,17 14,21 7,17 7,11" stroke="#c9a84c" strokeWidth="1" fill="none" opacity="0.5" />
              <circle cx="14" cy="14" r="2" fill="#c9a84c" />
            </svg>
          </div>
          <span className="logo-text">HIVI</span>
        </Link>

        <div className="navbar-links">
          {!ready ? (
            <span className="navbar-links-skeleton" aria-hidden />
          ) : showAuthChrome ? (
            AUTH_LINKS.map((link) => (
              <Link
                key={link.to}
                to={link.to}
                className={`nav-link ${isActive(link.to) ? 'active' : ''}`}
              >
                <span>{link.label}</span>
              </Link>
            ))
          ) : (
            GUEST_LINKS.map((link) =>
              link.hash ? (
                <a key={link.href} href={link.href} className="nav-link">
                  <span>{link.label}</span>
                </a>
              ) : (
                <Link
                  key={link.to}
                  to={link.to}
                  className={`nav-link ${isActive(link.to) ? 'active' : ''}`}
                >
                  <span>{link.label}</span>
                </Link>
              )
            )
          )}
        </div>

        {!ready ? (
          <div className="navbar-cta navbar-cta--skeleton" aria-hidden />
        ) : showAuthChrome ? (
          <NavbarUserMenu />
        ) : (
          <div className="navbar-cta">
            <Link to="/login" className="btn-ghost">sign in</Link>
            <Link to="/signup" className="btn-gold">get started</Link>
          </div>
        )}

        <button
          type="button"
          className={`hamburger ${menuOpen ? 'open' : ''}`}
          onClick={() => setMenuOpen(!menuOpen)}
          aria-label="menu"
        >
          <span /><span /><span />
        </button>
      </div>

      <div className={`mobile-menu ${menuOpen ? 'open' : ''}`}>
        {showAuthChrome ? (
          <>
            <div className="mobile-user-head">
              <span className="mobile-user-avatar">{getInitials(user?.name)}</span>
              <div>
                <p className="mobile-user-name">{user?.name}</p>
                <p className="mobile-user-email">{user?.email}</p>
              </div>
            </div>
            {AUTH_LINKS.map((link) => (
              <Link
                key={link.to}
                to={link.to}
                className={`mobile-link ${isActive(link.to) ? 'active' : ''}`}
                onClick={() => setMenuOpen(false)}
              >
                {link.label}
              </Link>
            ))}
            <button type="button" className="mobile-link mobile-link--logout" onClick={handleMobileLogout}>
              sign out
            </button>
          </>
        ) : (
          <>
            {GUEST_LINKS.map((link) =>
              link.hash ? (
                <a key={link.href} href={link.href} className="mobile-link" onClick={() => setMenuOpen(false)}>
                  {link.label}
                </a>
              ) : (
                <Link key={link.to} to={link.to} className="mobile-link" onClick={() => setMenuOpen(false)}>
                  {link.label}
                </Link>
              )
            )}
            <div className="mobile-cta">
              <Link to="/login" className="btn-ghost" onClick={() => setMenuOpen(false)}>sign in</Link>
              <Link to="/signup" className="btn-gold" onClick={() => setMenuOpen(false)}>get started</Link>
            </div>
          </>
        )}
      </div>
    </nav>
  );
};

export default Navbar;
