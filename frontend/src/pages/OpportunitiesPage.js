import React from 'react';
import DashboardLayout from '../components/dashboard/DashboardLayout';
import OpportunityFeed from '../components/opportunities/OpportunityFeed';
import './OpportunitiesPage.css';

const OpportunitiesPage = () => (
  <DashboardLayout>
    <div className="opportunities-page-shell">
      <OpportunityFeed />
    </div>
  </DashboardLayout>
);

export default OpportunitiesPage;
