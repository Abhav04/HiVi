import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import DashboardLayout from '../components/dashboard/DashboardLayout';
import { getUser, getFirstName, getLastName } from '../utils/auth';
import './Dashboard.css';

const statusColors = {
  completed: { color: '#27ae60', bg: 'rgba(39,174,96,0.1)' },
  'in progress': { color: '#c9a84c', bg: 'rgba(201,168,76,0.1)' },
  review: { color: '#3498db', bg: 'rgba(52,152,219,0.1)' },
  pending: { color: '#6b6b6b', bg: 'rgba(107,107,107,0.1)' },
};

const Dashboard = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('overview');
  const user = getUser() || { name: 'Guest', email: '', role: 'client', projects: [] };

  const firstName = getFirstName(user.name);
  const lastName = getLastName(user.name);
  const projects = user.projects || [];
  const isNewUser = projects.length === 0;

  const stats = isNewUser
    ? [
        { label: 'active projects', value: '0', sub: 'post your first project', pos: null },
        { label: 'total spent', value: '₹0', sub: 'no projects yet', pos: null },
        { label: 'avg. rating given', value: '—', sub: 'complete a project first', pos: null },
        { label: 'editors worked with', value: '0', sub: 'find an editor to start', pos: null },
      ]
    : [
        { label: 'active projects', value: '3', sub: '+1 this week', pos: true },
        { label: 'total spent', value: '₹96,500', sub: 'across 12 projects', pos: null },
        { label: 'avg. rating given', value: '4.8★', sub: 'above platform avg.', pos: true },
        { label: 'editors worked with', value: '7', sub: '3 recurring', pos: null },
      ];

  return (
    <DashboardLayout activeTab={activeTab} onTabChange={(tab) => setActiveTab(tab.id)}>
      {activeTab === 'overview' && (
        <>
          <div className="dash-greeting animate-fadeUp">
            <div>
              <p className="dash-eyebrow">good morning</p>
              <h1 className="dash-headline">
                {firstName}
                {lastName ? (
                  <>
                    {' '}
                    <em>{lastName}</em>
                  </>
                ) : null}
              </h1>
            </div>
            <Link to="/community" className="cta-new-project">
              + new project
            </Link>
          </div>

          <div className="dash-stats animate-fadeUp delay-1">
            {stats.map((s, i) => (
              <div key={i} className="dash-stat">
                <span className="ds-label">{s.label}</span>
                <span className="ds-value">{s.value}</span>
                <span className={`ds-sub ${s.pos === true ? 'positive' : ''}`}>{s.sub}</span>
              </div>
            ))}
          </div>

          <div className="dash-section animate-fadeUp delay-2">
            <div className="dash-section-header">
              <h2 className="dash-section-title">recent projects</h2>
              {!isNewUser && (
                <button className="dash-section-action" type="button" onClick={() => setActiveTab('projects')}>
                  view all →
                </button>
              )}
            </div>

            {isNewUser ? (
              <div className="projects-empty">
                <div className="projects-empty-icon">
                  <svg width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5">
                    <path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z" />
                    <polyline points="14 2 14 8 20 8" />
                  </svg>
                </div>
                <p className="projects-empty-title">no projects yet</p>
                <p className="projects-empty-sub">post your first project to connect with top editors</p>
                <Link to="/community" className="projects-empty-cta">
                  browse editors →
                </Link>
              </div>
            ) : (
              <div className="projects-table">
                <div className="pt-head">
                  <span>project</span>
                  <span>editor</span>
                  <span>status</span>
                  <span>due</span>
                  <span>amount</span>
                </div>
                {projects.map((p) => (
                  <div key={p.id} className="pt-row">
                    <div className="pt-title">
                      <span>{p.title}</span>
                      <div className="pt-progress-wrap">
                        <div className="pt-progress-bar" style={{ width: `${p.progress}%` }} />
                      </div>
                    </div>
                    <div className="pt-editor">{p.editor}</div>
                    <div>
                      <span
                        className="pt-status"
                        style={{
                          color: statusColors[p.status]?.color,
                          background: statusColors[p.status]?.bg,
                        }}
                      >
                        {p.status}
                      </span>
                    </div>
                    <div className="pt-due">{p.due}</div>
                    <div className="pt-amount">{p.amount}</div>
                  </div>
                ))}
              </div>
            )}
          </div>

          <div className="dash-section animate-fadeUp delay-3">
            <h2 className="dash-section-title" style={{ marginBottom: 16 }}>
              quick actions
            </h2>
            <div className="quick-actions">
              {[
                { icon: '🎬', label: 'community feed', sub: 'see creator work & post', action: () => navigate('/community') },
                {
                  icon: '🔥',
                  label: 'trends from reddit',
                  sub: 'curated editor inspiration',
                  action: () => navigate('/reddit-trends'),
                },
                {
                  icon: '📁',
                  label: 'view all projects',
                  sub: isNewUser ? 'no projects yet' : `${projects.length} total projects`,
                  action: () => setActiveTab('projects'),
                },
                {
                  icon: '💬',
                  label: 'check messages',
                  sub: isNewUser ? 'no messages yet' : 'view your inbox',
                  action: () => setActiveTab('messages'),
                },
              ].map((a, i) => (
                <div key={i} className="quick-action-card" onClick={a.action} role="button" tabIndex={0}>
                  <span className="qa-icon">{a.icon}</span>
                  <div>
                    <div className="qa-label">{a.label}</div>
                    <div className="qa-sub">{a.sub}</div>
                  </div>
                  <svg className="qa-arrow" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M5 12h14M12 5l7 7-7 7" />
                  </svg>
                </div>
              ))}
            </div>
          </div>
        </>
      )}

      {activeTab !== 'overview' && (
        <div className="dash-coming-soon animate-fadeUp">
          <div className="cs-icon">
            <svg width="32" height="32" viewBox="0 0 28 28" fill="none">
              <polygon points="14,2 26,8 26,20 14,26 2,20 2,8" stroke="var(--border2)" strokeWidth="1.5" fill="none" />
              <circle cx="14" cy="14" r="2" fill="var(--border2)" />
            </svg>
          </div>
          <h3 className="cs-title">{activeTab}</h3>
          <p className="cs-sub">This section is coming soon. Use Community, Opportunities, or Reddit trends in the meantime.</p>
        </div>
      )}
    </DashboardLayout>
  );
};

export default Dashboard;
