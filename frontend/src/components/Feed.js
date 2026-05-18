import React from 'react';
import PostCard from './PostCard';
import './Feed.css';

const mockEditors = [
  { id: 1, name: 'Aryan Kapoor', initials: 'AK', title: 'Cinematic Editor', rate: '$65', rating: 4.9, reviews: 142, skills: ['Color Grading', 'Documentary', 'Motion Graphics'], available: true, location: 'Mumbai, India', projects: 113, style: 'Cinematic' },
  { id: 2, name: 'Priya Sharma', initials: 'PS', title: 'Reels Specialist', rate: '$45', rating: 4.8, reviews: 87, skills: ['Reels', 'YouTube', 'Commercial'], available: true, location: 'Delhi, India', projects: 256, style: 'Trendy' },
  { id: 3, name: 'Marcus Webb', initials: 'MW', title: 'VFX & Motion Expert', rate: '$120', rating: 5.0, reviews: 63, skills: ['VFX', 'Motion Graphics', 'Color Grading'], available: false, location: 'Berlin, Germany', projects: 48, style: 'Futuristic' },
  { id: 4, name: 'Zara Nair', initials: 'ZN', title: 'Wedding Films Editor', rate: '$55', rating: 4.7, reviews: 201, skills: ['Wedding', 'Documentary', 'Color Grading'], available: true, location: 'Bangalore, India', projects: 89, style: 'Emotional' },
  { id: 5, name: 'Chen Wei', initials: 'CW', title: 'Commercial Director', rate: '$95', rating: 4.9, reviews: 44, skills: ['Commercial', 'Color Grading', 'Motion Graphics'], available: true, location: 'Singapore', projects: 31, style: 'Minimalist' },
  { id: 6, name: 'Sofia Alves', initials: 'SA', title: 'Content Creator Editor', rate: '$40', rating: 4.6, reviews: 318, skills: ['YouTube', 'Reels', 'Corporate'], available: false, location: 'São Paulo, Brazil', projects: 421, style: 'Vibrant' },
  { id: 7, name: 'Rahul Verma', initials: 'RV', title: 'Documentary Storyteller', rate: '$70', rating: 4.8, reviews: 95, skills: ['Documentary', 'Color Grading', 'Corporate'], available: true, location: 'Chennai, India', projects: 67, style: 'Raw' },
  { id: 8, name: 'Lena Fischer', initials: 'LF', title: 'Brand Film Editor', rate: '$85', rating: 4.9, reviews: 52, skills: ['Commercial', 'Motion Graphics', 'VFX'], available: true, location: 'Munich, Germany', projects: 44, style: 'Premium' },
];

const Feed = ({ searchQuery = '', filterBy = 'all', sortBy = 'top rated' }) => {
  const filtered = mockEditors
    .filter(e => {
      if (searchQuery) {
        const q = searchQuery.toLowerCase();
        return e.name.toLowerCase().includes(q) || e.title.toLowerCase().includes(q) || e.skills.some(s => s.toLowerCase().includes(q)) || e.style.toLowerCase().includes(q);
      }
      return true;
    })
    .filter(e => {
      if (filterBy === 'all') return true;
      return e.skills.some(s => s.toLowerCase() === filterBy.toLowerCase());
    })
    .sort((a, b) => {
      if (sortBy === 'top rated') return b.rating - a.rating;
      if (sortBy === 'lowest rate') return parseInt(a.rate.slice(1)) - parseInt(b.rate.slice(1));
      if (sortBy === 'highest rate') return parseInt(b.rate.slice(1)) - parseInt(a.rate.slice(1));
      if (sortBy === 'most projects') return b.projects - a.projects;
      return 0;
    });

  return (
    <div className="feed">
      {filtered.length === 0 ? (
        <div className="feed-empty">
          <div className="empty-icon">
            <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="var(--border2)" strokeWidth="1">
              <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
            </svg>
          </div>
          <p className="empty-text">no editors match your search</p>
          <p className="empty-sub">try adjusting your filters</p>
        </div>
      ) : (
        <div className="feed-grid">
          {filtered.map((editor, i) => (
            <div
              key={editor.id}
              className="feed-card-wrap animate-fadeUp"
              style={{ animationDelay: `${i * 0.07}s` }}
            >
              <PostCard editor={editor} />
            </div>
          ))}
        </div>
      )}

      {filtered.length > 0 && (
        <div className="feed-footer">
          <span className="feed-count">{filtered.length} editors found</span>
          <button className="load-more">load more <span>↓</span></button>
        </div>
      )}
    </div>
  );
};

export default Feed;