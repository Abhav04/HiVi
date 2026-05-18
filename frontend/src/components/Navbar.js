import React, { useState, useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import './Navbar.css';

const Navbar = ({ transparent = false }) => {
  const [scrolled, setScrolled] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const location = useLocation();

  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 40);
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  return (
    <nav className={`navbar ${scrolled ? 'scrolled' : ''} ${transparent ? 'transparent' : ''}`}>
      <div className="navbar-inner">
        {/* Logo */}
        <Link to="/" className="navbar-logo">
          <div className="logo-icon">
            <svg width="28" height="28" viewBox="0 0 28 28" fill="none">
              <polygon points="14,2 26,8 26,20 14,26 2,20 2,8" stroke="#c9a84c" strokeWidth="1.5" fill="none"/>
              <polygon points="14,7 21,11 21,17 14,21 7,17 7,11" stroke="#c9a84c" strokeWidth="1" fill="none" opacity="0.5"/>
              <circle cx="14" cy="14" r="2" fill="#c9a84c"/>
            </svg>
          </div>
          <span className="logo-text">HIVI</span>
        </Link>

        {/* Nav Links - Desktop */}
        <div className="navbar-links">
          <Link to="/feed" className={`nav-link ${location.pathname === '/feed' ? 'active' : ''}`}>
            <span>discover</span>
          </Link>
          <a href="#how" className="nav-link"><span>how it works</span></a>
          <a href="#talent" className="nav-link"><span>for talent</span></a>
          <a href="#hire" className="nav-link"><span>hire</span></a>
        </div>

        {/* CTA */}
        <div className="navbar-cta">
          <Link to="/login" className="btn-ghost">sign in</Link>
          <Link to="/signup" className="btn-gold">get started</Link>
        </div>

        {/* Hamburger */}
        <button
          className={`hamburger ${menuOpen ? 'open' : ''}`}
          onClick={() => setMenuOpen(!menuOpen)}
          aria-label="menu"
        >
          <span /><span /><span />
        </button>
      </div>

      {/* Mobile Menu */}
      <div className={`mobile-menu ${menuOpen ? 'open' : ''}`}>
        <Link to="/feed" className="mobile-link" onClick={() => setMenuOpen(false)}>discover</Link>
        <a href="#how" className="mobile-link" onClick={() => setMenuOpen(false)}>how it works</a>
        <a href="#talent" className="mobile-link" onClick={() => setMenuOpen(false)}>for talent</a>
        <a href="#hire" className="mobile-link" onClick={() => setMenuOpen(false)}>hire</a>
        <div className="mobile-cta">
          <Link to="/login" className="btn-ghost" onClick={() => setMenuOpen(false)}>sign in</Link>
          <Link to="/signup" className="btn-gold" onClick={() => setMenuOpen(false)}>get started</Link>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;