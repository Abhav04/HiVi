import React, { useState } from 'react';
import { createCommunityPost } from '../../services/communityApi';
import './PostComposer.css';

const STYLES = ['cinematic', 'anime', 'gaming', 'reels', 'motion graphics', 'documentaries'];

const PostComposer = ({ onPosted, onClose }) => {
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [postType, setPostType] = useState('TEXT');
  const [portfolioLink, setPortfolioLink] = useState('');
  const [selectedTags, setSelectedTags] = useState([]);
  const [media, setMedia] = useState(null);
  const [draft, setDraft] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  const toggleTag = (tag) => {
    setSelectedTags((prev) =>
      prev.includes(tag) ? prev.filter((t) => t !== tag) : [...prev, tag]
    );
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError(null);
    try {
      const fd = new FormData();
      fd.append('title', title);
      fd.append('content', content);
      fd.append('postType', postType);
      fd.append('tags', selectedTags.join(','));
      fd.append('portfolioLink', portfolioLink);
      fd.append('draft', String(draft));
      if (media) fd.append('media', media);
      const post = await createCommunityPost(fd);
      onPosted?.(post);
      setTitle('');
      setContent('');
      setMedia(null);
      setSelectedTags([]);
      onClose?.();
    } catch (err) {
      setError(err.message || 'Could not create post. Sign in and try again.');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="post-composer">
      <div className="post-composer-header">
        <h3>create post</h3>
        {onClose && (
          <button type="button" className="post-composer-close" onClick={onClose} aria-label="Close">
            ×
          </button>
        )}
      </div>

      <form onSubmit={handleSubmit} className="post-composer-form">
        <div className="post-composer-types">
          {['TEXT', 'IMAGE', 'VIDEO', 'PORTFOLIO'].map((t) => (
            <button
              key={t}
              type="button"
              className={`post-type-btn ${postType === t ? 'active' : ''}`}
              onClick={() => setPostType(t)}
            >
              {t.toLowerCase()}
            </button>
          ))}
        </div>

        <input
          className="post-composer-input"
          placeholder="Title — what did you create?"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          required
        />

        <textarea
          className="post-composer-textarea"
          placeholder="Tell the community about your edit, process, or gig..."
          value={content}
          onChange={(e) => setContent(e.target.value)}
          rows={4}
        />

        <input
          className="post-composer-input"
          placeholder="Portfolio / reel link (optional)"
          value={portfolioLink}
          onChange={(e) => setPortfolioLink(e.target.value)}
        />

        <div className="post-composer-tags">
          {STYLES.map((tag) => (
            <button
              key={tag}
              type="button"
              className={`tag-chip ${selectedTags.includes(tag) ? 'active' : ''}`}
              onClick={() => toggleTag(tag)}
            >
              {tag}
            </button>
          ))}
        </div>

        <label className="post-composer-upload">
          <span>upload video or image</span>
          <input
            type="file"
            accept="image/*,video/*"
            onChange={(e) => setMedia(e.target.files?.[0] || null)}
          />
          {media && <em>{media.name}</em>}
        </label>

        <label className="post-composer-draft">
          <input type="checkbox" checked={draft} onChange={(e) => setDraft(e.target.checked)} />
          save as draft
        </label>

        {error && <p className="post-composer-error">{error}</p>}

        <button type="submit" className="post-composer-submit" disabled={submitting}>
          {submitting ? 'publishing…' : draft ? 'save draft' : 'publish to community →'}
        </button>
      </form>
    </div>
  );
};

export default PostComposer;
