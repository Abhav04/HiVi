import React from 'react';
import DashboardLayout from '../components/dashboard/DashboardLayout';
import CommunityFeed from '../components/community/CommunityFeed';
import './CommunityFeedPage.css';

const CommunityFeedPage = () => (
  <DashboardLayout>
    <div className="community-page-shell">
      <CommunityFeed />
    </div>
  </DashboardLayout>
);

export default CommunityFeedPage;
