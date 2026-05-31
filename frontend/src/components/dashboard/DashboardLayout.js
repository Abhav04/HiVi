import React from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import Navbar from '../Navbar';
import { getInitials } from '../../utils/auth';
import { useAuth } from '../../context/AuthContext';
import '../../pages/Dashboard.css';
import './DashboardLayout.css';

const DASH_TABS = [
  { id: 'overview', label: 'overview', path: '/dashboard', icon: 'grid' },
  { id: 'projects', label: 'projects', path: '/dashboard?tab=projects', icon: 'folder' },
  { id: 'messages', label: 'messages', path: '/dashboard?tab=messages', icon: 'chat' },
  { id: 'earnings', label: 'earnings', path: '/dashboard?tab=earnings', icon: 'earnings' },
  { id: 'profile', label: 'profile', path: '/dashboard?tab=profile', icon: 'profile' },
];

const TabIcon = ({ type }) => {
  const props = { width: 14, height: 14, viewBox: '0 0 24 24', fill: 'none', stroke: 'currentColor', strokeWidth: 1.5 };
  switch (type) {
    case 'grid':
      return (
        <svg {...props}>
          <rect x="3" y="3" width="7" height="7" />
          <rect x="14" y="3" width="7" height="7" />
          <rect x="3" y="14" width="7" height="7" />
          <rect x="14" y="14" width="7" height="7" />
        </svg>
      );
    case 'folder':
      return (
        <svg {...props}>
          <path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z" />
          <polyline points="14 2 14 8 20 8" />
        </svg>
      );
    case 'chat':
      return (
        <svg {...props}>
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
        </svg>
      );
    case 'earnings':
      return (
        <svg {...props}>
          <line x1="12" y1="1" x2="12" y2="23" />
          <path d="M17 5H9.5a3.5 3.5 0 0 0 0 7h5a3.5 3.5 0 0 1 0 7H6" />
        </svg>
      );
    case 'profile':
      return (
        <svg {...props}>
          <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
          <circle cx="12" cy="7" r="4" />
        </svg>
      );
    case 'trends':
      return (
        <svg {...props}>
          <path d="M3 3v18h18" />
          <path d="M7 16l4-6 4 3 5-8" />
          <circle cx="18" cy="6" r="2" fill="currentColor" stroke="none" />
        </svg>
      );
    case 'community':
      return (
        <svg {...props}>
          <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
          <circle cx="9" cy="7" r="4" />
          <path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" />
        </svg>
      );
    case 'opportunities':
      return (
        <svg {...props}>
          <rect x="2" y="7" width="20" height="14" rx="2" />
          <path d="M16 7V5a2 2 0 0 0-2-2h-4a2 2 0 0 0-2 2v2" />
          <line x1="12" y1="12" x2="12" y2="16" />
          <line x1="10" y1="14" x2="14" y2="14" />
        </svg>
      );
    default:
      return null;
  }
};

const DashboardLayout = ({ children, activeTab, onTabChange }) => {
  const location = useLocation();
  const navigate = useNavigate();
  const { ready, isAuthenticated, user: authUser, logout: authLogout } = useAuth();
  const user = (isAuthenticated && authUser) || { name: 'Guest', email: '', role: 'client' };
  const initials = getInitials(user.name);
  const roleLabel = user.role === 'editor' ? 'editor account' : 'client account';
  const isRedditPage = location.pathname === '/reddit-trends';
  const isCommunityPage = location.pathname.startsWith('/community');
  const isOpportunitiesPage = location.pathname.startsWith('/opportunities');

  const handleSignOut = () => {
    authLogout();
    navigate('/');
  };

  const handleDashTab = (tab) => {
    if (onTabChange) {
      onTabChange(tab.id);
      return;
    }
    navigate(tab.path);
  };

  return (
    <div className="dashboard">
      <Navbar />
      <div className="dashboard-inner">
        <aside className="dash-sidebar">
          <div className="dash-user animate-fadeUp">
            {!ready ? (
              <div className="dash-user-skeleton shimmer" aria-hidden />
            ) : (
              <>
                <div className="dash-avatar">{initials}</div>
                <div>
                  <div className="dash-name">{user.name}</div>
                  <div className="dash-role">
                    <span className="dash-dot" />
                    {isAuthenticated ? roleLabel : 'sign in to continue'}
                  </div>
                </div>
              </>
            )}
          </div>

          <nav className="dash-nav">
            <Link
              to="/community"
              className={`dash-nav-featured dash-nav-featured--community ${isCommunityPage ? 'active' : ''}`}
            >
              <span className="dash-nav-icon">
                <TabIcon type="community" />
              </span>
              <span className="dash-nav-featured-text">
                <span className="dash-nav-featured-label">community feed</span>
                <span className="dash-nav-featured-badge">social</span>
              </span>
            </Link>

            <Link
              to="/opportunities"
              className={`dash-nav-featured dash-nav-featured--opportunities ${isOpportunitiesPage ? 'active' : ''}`}
            >
              <span className="dash-nav-icon">
                <TabIcon type="opportunities" />
              </span>
              <span className="dash-nav-featured-text">
                <span className="dash-nav-featured-label">opportunities</span>
                <span className="dash-nav-featured-badge">careers</span>
              </span>
            </Link>

            <Link
              to="/reddit-trends"
              className={`dash-nav-featured dash-nav-featured--reddit ${isRedditPage ? 'active' : ''}`}
            >
              <span className="dash-nav-icon">
                <TabIcon type="trends" />
              </span>
              <span className="dash-nav-featured-text">
                <span className="dash-nav-featured-label">trends from reddit</span>
                <span className="dash-nav-featured-badge">live</span>
              </span>
            </Link>

            <div className="dash-nav-divider" aria-hidden="true" />

            {DASH_TABS.map((tab) => (
              <button
                key={tab.id}
                type="button"
                className={`dash-nav-item ${!isRedditPage && !isCommunityPage && !isOpportunitiesPage && activeTab === tab.id ? 'active' : ''}`}
                onClick={() => handleDashTab(tab)}
              >
                <span className="dash-nav-icon">
                  <TabIcon type={tab.icon} />
                </span>
                {tab.label}
              </button>
            ))}
          </nav>

          <div className="dash-sidebar-footer">
            <Link to="/community" className="dash-browse-btn">
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="11" cy="11" r="8" />
                <path d="m21 21-4.35-4.35" />
              </svg>
              explore community
            </Link>
            <button type="button" className="dash-logout" onClick={handleSignOut}>
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9" />
              </svg>
              sign out
            </button>
          </div>
        </aside>

        <main
          className={`dash-main ${isRedditPage ? 'dash-main-reddit' : ''} ${isCommunityPage ? 'dash-main-community' : ''} ${isOpportunitiesPage ? 'dash-main-opportunities' : ''}`}
        >
          {children}
        </main>
      </div>
    </div>
  );
};

export default DashboardLayout;
