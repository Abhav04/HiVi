import React, { useState } from 'react';
import './PostBox.css';

const filters = ['all', 'color grading', 'motion graphics', 'documentary', 'commercial', 'youtube', 'reels', 'vfx', 'wedding'];
const sorts = ['top rated', 'newest', 'lowest rate', 'highest rate', 'most projects'];

const PostBox = ({ onSearch, onFilter, onSort }) => {
  const [query, setQuery] = useState('');
  const [activeFilter, setActiveFilter] = useState('all');
  const [activeSort, setActiveSort] = useState('top rated');
  const [sortOpen, setSortOpen] = useState(false);

  const handleFilter = (f) => {
    setActiveFilter(f);
    if (onFilter) onFilter(f);
  };

  const handleSort = (s) => {
    setActiveSort(s);
    setSortOpen(false);
    if (onSort) onSort(s);
  };

  const handleSearch = (e) => {
    setQuery(e.target.value);
    if (onSearch) onSearch(e.target.value);
  };

  return (
    <div className="postbox">
      {/* Search bar */}
      <div className="search-row">
        <div className="search-wrap">
          <svg className="search-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
          </svg>
          <input
            type="text"
            className="search-input"
            placeholder="search by name, skill, or style..."
            value={query}
            onChange={handleSearch}
          />
          {query && (
            <button className="search-clear" onClick={() => { setQuery(''); if (onSearch) onSearch(''); }}>
              <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
              </svg>
            </button>
          )}
        </div>

        {/* Sort dropdown */}
        <div className="sort-wrap">
          <button className="sort-btn" onClick={() => setSortOpen(!sortOpen)}>
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <line x1="3" y1="6" x2="21" y2="6"/><line x1="3" y1="12" x2="15" y2="12"/><line x1="3" y1="18" x2="9" y2="18"/>
            </svg>
            {activeSort}
            <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" style={{ transform: sortOpen ? 'rotate(180deg)' : 'none', transition: '0.2s' }}>
              <polyline points="6 9 12 15 18 9"/>
            </svg>
          </button>
          {sortOpen && (
            <div className="sort-dropdown">
              {sorts.map(s => (
                <button key={s} className={`sort-option ${s === activeSort ? 'active' : ''}`} onClick={() => handleSort(s)}>
                  {s}
                  {s === activeSort && <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="#c9a84c" strokeWidth="3"><polyline points="20 6 9 17 4 12"/></svg>}
                </button>
              ))}
            </div>
          )}
        </div>
      </div>

      {/* Filter pills */}
      <div className="filter-row">
        <div className="filters-label">filter:</div>
        <div className="filters-scroll">
          {filters.map(f => (
            <button
              key={f}
              className={`filter-pill ${f === activeFilter ? 'active' : ''}`}
              onClick={() => handleFilter(f)}
            >
              {f}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
};

export default PostBox;