import React, { useState } from 'react';
import Navbar from '../components/Navbar';
import PostBox from '../components/PostBox';
import Feed from '../components/Feed';
import './HomeFeed.css';

const HomeFeed = () => {
  const [searchQuery, setSearchQuery] = useState('');
  const [filterBy, setFilterBy] = useState('all');
  const [sortBy, setSortBy] = useState('top rated');

  return (
    <div className="homefeed">
      <Navbar />

      <div className="homefeed-inner">
        {/* Page Header */}
        <div className="feed-page-header animate-fadeUp">
          <div className="feed-eyebrow">discover talent</div>
          <h1 className="feed-page-title">
            find your <em>perfect editor</em>
          </h1>
          <p className="feed-page-sub">
            Handpicked, verified professionals — every one of them exceptional.
          </p>
        </div>

        {/* Search & Filter */}
        <div className="animate-fadeUp delay-2">
          <PostBox
            onSearch={setSearchQuery}
            onFilter={setFilterBy}
            onSort={setSortBy}
          />
        </div>

        {/* Feed */}
        <div className="animate-fadeUp delay-3">
          <Feed
            searchQuery={searchQuery}
            filterBy={filterBy}
            sortBy={sortBy}
          />
        </div>
      </div>
    </div>
  );
};

export default HomeFeed;