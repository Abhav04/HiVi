import React, { useEffect, useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getInitials } from '../utils/auth';
import './NavbarUserMenu.css';

const NavbarUserMenu = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [open, setOpen] = useState(false);
  const rootRef = useRef(null);

  const initials = getInitials(user?.name || user?.email);
  const profilePath = user?.username
    ? `/community/creator/${user.username}`
    : '/dashboard?tab=profile';

  useEffect(() => {
    const onDocClick = (e) => {
      if (rootRef.current && !rootRef.current.contains(e.target)) {
        setOpen(false);
      }
    };
    const onKey = (e) => {
      if (e.key === 'Escape') setOpen(false);
    };
    document.addEventListener('mousedown', onDocClick);
    document.addEventListener('keydown', onKey);
    return () => {
      document.removeEventListener('mousedown', onDocClick);
      document.removeEventListener('keydown', onKey);
    };
  }, []);

  const handleLogout = () => {
    setOpen(false);
    logout();
    navigate('/');
  };

  if (!user) return null;

  return (
    <div className="navbar-user navbar-user--minimal" ref={rootRef}>
      <button
        type="button"
        className={`navbar-user-avatar-btn ${open ? 'open' : ''}`}
        onClick={() => setOpen((v) => !v)}
        aria-expanded={open}
        aria-haspopup="menu"
        aria-label="Account menu"
        title={user.name || 'Account'}
      >
        <span className="navbar-user-avatar">{initials}</span>
      </button>

      {open && (
        <div className="navbar-user-menu" role="menu">
          <div className="navbar-user-menu-head">
            <span className="navbar-user-menu-avatar">{initials}</span>
            <div>
              <p className="navbar-user-menu-name">{user.name}</p>
              <p className="navbar-user-menu-email">{user.email}</p>
            </div>
          </div>
          <div className="navbar-user-menu-divider" />
          <Link to={profilePath} className="navbar-user-menu-item" role="menuitem" onClick={() => setOpen(false)}>
            Profile
          </Link>
          <Link
            to="/dashboard?tab=profile"
            className="navbar-user-menu-item"
            role="menuitem"
            onClick={() => setOpen(false)}
          >
            Settings
          </Link>
          <div className="navbar-user-menu-divider" />
          <button type="button" className="navbar-user-menu-item navbar-user-menu-item--danger" onClick={handleLogout}>
            Logout
          </button>
        </div>
      )}
    </div>
  );
};

export default NavbarUserMenu;
