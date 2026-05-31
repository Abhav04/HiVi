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
  const [submitting, setSubmitting] = useState(false);
  const [publishing, setPublishing] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  const toggleTag = (tag) => {
    setSelectedTags((prev) =>
      prev.includes(tag) ? prev.filter((t) => t !== tag) : [...prev, tag]
    );
  };

  const submitPost = async (asDraft) => {
    const trimmedTitle = title.trim();
    if (!trimmedTitle) {
      setError('Add a title for your post.');
      return;
    }
    setSubmitting(true);
    setPublishing(!asDraft);
    setError(null);
    setSuccess(false);
    try {
      const fd = new FormData();
      fd.append('title', trimmedTitle);
      fd.append('content', content.trim());
      fd.append('postType', postType);
      fd.append('tags', selectedTags.join(','));
      fd.append('portfolioLink', portfolioLink.trim());
      fd.append('draft', String(asDraft));
      if (media) fd.append('media', media);
      const post = await createCommunityPost(fd);
      setSuccess(true);
      onPosted?.(post);
      setTitle('');
      setContent('');
      setMedia(null);
      setSelectedTags([]);
      setPortfolioLink('');
      if (!asDraft) {
        setTimeout(() => onClose?.(), 400);
      }
    } catch (err) {
      setError(err.message || 'Could not publish. Sign in and try again.');
    } finally {
      setSubmitting(false);
      setPublishing(false);
    }
  };

  const handlePublish = (e) => {
    e.preventDefault();
    submitPost(false);
  };

  const handleSaveDraft = (e) => {
    e.preventDefault();
    submitPost(true);
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

      <form onSubmit={handlePublish} className="post-composer-form">
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

        {error && <p className="post-composer-error">{error}</p>}
        {success && <p className="post-composer-success">Published to the community feed.</p>}

        <div className="post-composer-actions">
          <button type="button" className="post-composer-draft-btn" disabled={submitting} onClick={handleSaveDraft}>
            {submitting && !publishing ? 'Saving…' : 'Save draft'}
          </button>
          <button type="submit" className="post-composer-submit" disabled={submitting}>
            {publishing ? 'Publishing…' : 'Publish'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default PostComposer;
