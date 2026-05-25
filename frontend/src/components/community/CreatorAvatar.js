import React, { memo } from 'react';
import { Link } from 'react-router-dom';
import './CreatorAvatar.css';

const CreatorAvatar = memo(function CreatorAvatar({
  author,
  size = 'md',
  link = true,
  className = '',
}) {
  if (!author) return null;

  const initial = (author.displayName || author.username || '?')[0]?.toUpperCase();
  const inner = author.avatarUrl ? (
    <img src={author.avatarUrl} alt="" className="creator-avatar__img" />
  ) : (
    <span className="creator-avatar__initial">{initial}</span>
  );

  const el = (
    <span className={`creator-avatar creator-avatar--${size} ${className}`}>
      {inner}
    </span>
  );

  if (link && author.username) {
    return (
      <Link to={`/community/creator/${author.username}`} className="creator-avatar__link">
        {el}
      </Link>
    );
  }
  return el;
});

export default CreatorAvatar;
