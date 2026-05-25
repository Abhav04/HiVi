import React from 'react';
import DashboardLayout from '../components/dashboard/DashboardLayout';
import RedditTrendingFeed from '../components/reddit/RedditTrendingFeed';
import './RedditTrendsPage.css';

const RedditTrendsPage = () => (
  <DashboardLayout>
    <div className="reddit-trends-page">
      <div className="reddit-trends-hero" aria-hidden="true">
        <div className="reddit-trends-hero-orb reddit-trends-hero-orb--orange" />
        <div className="reddit-trends-hero-orb reddit-trends-hero-orb--purple" />
      </div>
      <RedditTrendingFeed variant="page" />
    </div>
  </DashboardLayout>
);

export default RedditTrendsPage;
