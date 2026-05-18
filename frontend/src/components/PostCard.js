import React, { useState } from 'react';
import './PostCard.css';

const skillColors = {
  'Color Grading': '#c9a84c',
  'Motion Graphics': '#7b5ea7',
  'Documentary': '#2e7d6b',
  'Commercial': '#c0392b',
  'YouTube': '#e67e22',
  'Reels': '#d4a5c9',
  'VFX': '#3498db',
  'Wedding': '#e8c96d',
  'Corporate': '#95a5a6',
};

const PostCard = ({ editor }) => {
  const [saved, setSaved] = useState(false);
  const [hovered, setHovered] = useState(false);

  const {
    name = 'Alex Chen',
    title = 'Senior Video Editor',
    avatar = null,
    initials = 'AC',
    rate = '$85',
    rating = 4.9,
    reviews = 124,
    skills = ['Color Grading', 'Motion Graphics', 'Documentary'],
    available = true,
    location = 'Mumbai, India',
    projects = 89,
    style = 'Cinematic',
    reel = '#',
  } = editor || {};

  return (
    <div
      className={`postcard ${hovered ? 'hovered' : ''}`}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
    >
      {/* Availability badge */}
      <div className={`availability-badge ${available ? 'available' : 'busy'}`}>
        <span className="dot" />
        {available ? 'available now' : 'booked'}
      </div>

      {/* Save button */}
      <button
        className={`save-btn ${saved ? 'saved' : ''}`}
        onClick={(e) => { e.stopPropagation(); setSaved(!saved); }}
        aria-label="save"
      >
        <svg width="14" height="14" viewBox="0 0 24 24" fill={saved ? '#c9a84c' : 'none'} stroke={saved ? '#c9a84c' : '#6b6b6b'} strokeWidth="2">
          <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
        </svg>
      </button>

      {/* Avatar */}
      <div className="card-avatar">
        {avatar ? (
          <img src={avatar} alt={name} />
        ) : (
          <div className="avatar-initials">{initials}</div>
        )}
        <div className="style-tag">{style}</div>
      </div>

      {/* Info */}
      <div className="card-info">
        <div className="card-header">
          <div>
            <h3 className="card-name">{name}</h3>
            <p className="card-title">{title}</p>
          </div>
          <div className="card-rate">
            <span className="rate-value">{rate}</span>
            <span className="rate-unit">/hr</span>
          </div>
        </div>

        <div className="card-meta">
          <span className="meta-item">
            <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
              <circle cx="12" cy="10" r="3"/>
            </svg>
            {location}
          </span>
          <span className="meta-divider">·</span>
          <span className="meta-item">
            <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2"/>
            </svg>
            {rating} ({reviews})
          </span>
          <span className="meta-divider">·</span>
          <span className="meta-item">{projects} projects</span>
        </div>

        {/* Skills */}
        <div className="card-skills">
          {skills.slice(0, 3).map((skill) => (
            <span
              key={skill}
              className="skill-tag"
              style={{ '--skill-color': skillColors[skill] || '#c9a84c' }}
            >
              {skill}
            </span>
          ))}
          {skills.length > 3 && <span className="skill-more">+{skills.length - 3}</span>}
        </div>

        {/* CTA */}
        <div className="card-actions">
          <a href={reel} className="btn-reel">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor">
              <polygon points="5 3 19 12 5 21 5 3"/>
            </svg>
            view reel
          </a>
          <button className="btn-hire">hire →</button>
        </div>
      </div>
    </div>
  );
};

export default PostCard;