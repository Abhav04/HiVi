import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import NavbarUserMenu from './NavbarUserMenu';
import { getInitials } from '../utils/auth';
import './Navbar.css';

const GUEST_LINKS = [
  { href: '#how', label: 'how it works', hash: true },
  { href: '#talent', label: 'for talent', hash: true },
  { href: '#hire', label: 'hire', hash: true },
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

  const handleMobileLogout = () => {
    setMenuOpen(false);
    logout();
    navigate('/');
  };

  const showAuthChrome = ready && isAuthenticated;

  return (
    <nav className={`navbar ${scrolled ? 'scrolled' : ''} ${transparent ? 'transparent' : ''} ${showAuthChrome ? 'navbar--app' : ''}`}>
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

        {!showAuthChrome && (
          <div className="navbar-links">
            {!ready ? (
              <span className="navbar-links-skeleton" aria-hidden />
            ) : (
              GUEST_LINKS.map((link) => (
                <a key={link.href} href={link.href} className="nav-link">
                  <span>{link.label}</span>
                </a>
              ))
            )}
          </div>
        )}

        {showAuthChrome && <div className="navbar-spacer" aria-hidden />}

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
            <Link to="/dashboard?tab=profile" className="mobile-link" onClick={() => setMenuOpen(false)}>
              Profile
            </Link>
            <Link to="/dashboard?tab=profile" className="mobile-link" onClick={() => setMenuOpen(false)}>
              Settings
            </Link>
            <button type="button" className="mobile-link mobile-link--logout" onClick={handleMobileLogout}>
              Logout
            </button>
          </>
        ) : (
          <>
            {GUEST_LINKS.map((link) => (
              <a key={link.href} href={link.href} className="mobile-link" onClick={() => setMenuOpen(false)}>
                {link.label}
              </a>
            ))}
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
