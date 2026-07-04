import React, { useState, useEffect, useRef } from 'react';
import { createCommunityPost, updateCommunityPost } from '../../services/communityApi';
import './PostComposer.css';

const STYLES = ['cinematic', 'anime', 'gaming', 'reels', 'motion graphics', 'documentaries'];

const PostComposer = ({ onPosted, onClose, editingPost }) => {
  const [title, setTitle] = useState(editingPost ? editingPost.title : '');
  const [content, setContent] = useState(editingPost ? editingPost.content || '' : '');
  const [postType, setPostType] = useState(editingPost ? editingPost.postType : 'TEXT');
  const [portfolioLink, setPortfolioLink] = useState(editingPost ? editingPost.portfolioLink || '' : '');
  const [selectedTags, setSelectedTags] = useState(editingPost ? editingPost.tags || [] : []);
  
  const [mediaPreviews, setMediaPreviews] = useState(
    editingPost && editingPost.mediaItems
      ? editingPost.mediaItems.map((item) => ({
          id: item.id,
          url: item.mediaUrl,
          name: item.mediaUrl.substring(item.mediaUrl.lastIndexOf('/') + 1),
          isExisting: true,
          isVideo: item.mediaKind === 'VIDEO',
        }))
      : []
  );

  const [dragActive, setDragActive] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [publishing, setPublishing] = useState(false);
  const [uploadProgress, setUploadProgress] = useState(0);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  const fileInputRef = useRef(null);

  // Clean up object URLs when the previews change or component unmounts
  useEffect(() => {
    return () => {
      mediaPreviews.forEach((item) => {
        if (!item.isExisting && item.url && item.url.startsWith('blob:')) {
          URL.revokeObjectURL(item.url);
        }
      });
    };
  }, [mediaPreviews]);

  const toggleTag = (tag) => {
    setSelectedTags((prev) =>
      prev.includes(tag) ? prev.filter((t) => t !== tag) : [...prev, tag]
    );
  };

  const validateFiles = (files) => {
    const valid = [];
    let errMessage = null;

    files.forEach((file) => {
      if (!file.type.startsWith('image/') && !file.type.startsWith('video/')) {
        errMessage = `Unsupported file type: ${file.name}. Only images and videos are allowed.`;
        return;
      }

      const isVideo = file.type.startsWith('video/');
      const limit = isVideo ? 60 * 1024 * 1024 : 15 * 1024 * 1024; // 60MB for videos, 15MB for images
      if (file.size > limit) {
        errMessage = `File too large: ${file.name}. Limit is ${isVideo ? '60MB' : '15MB'}.`;
        return;
      }

      valid.push(file);
    });

    if (errMessage) {
      setError(errMessage);
    }
    return valid;
  };

  const handleFiles = (files) => {
    if (!files || files.length === 0) return;
    setError(null);

    const validFiles = validateFiles(Array.from(files));
    if (validFiles.length === 0) return;

    // Limit total files in a post to 10 (existing + new)
    const currentTotal = mediaPreviews.length;
    const allowedCount = 10 - currentTotal;
    if (allowedCount <= 0) {
      setError('You can attach a maximum of 10 media files.');
      return;
    }

    const filesToAdd = validFiles.slice(0, allowedCount);

    const newPreviews = filesToAdd.map((file) => {
      const isVideo = file.type.startsWith('video/');
      return {
        url: URL.createObjectURL(file),
        isVideo,
        name: file.name,
        isExisting: false,
        file,
      };
    });
    
    const mergedPreviews = [...mediaPreviews, ...newPreviews];
    setMediaPreviews(mergedPreviews);

    // Auto-select type based on files
    const hasVideo = mergedPreviews.some((f) => f.isVideo);
    const hasImage = mergedPreviews.some((f) => !f.isVideo);
    if (hasVideo) {
      setPostType('VIDEO');
    } else if (hasImage) {
      setPostType('IMAGE');
    }
  };

  const handleDrag = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.type === 'dragenter' || e.type === 'dragover') {
      setDragActive(true);
    } else if (e.type === 'dragleave') {
      setDragActive(false);
    }
  };

  const handleDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      handleFiles(e.dataTransfer.files);
    }
  };

  const removeFile = (index) => {
    const fileToRemove = mediaPreviews[index];
    if (fileToRemove && fileToRemove.url && !fileToRemove.isExisting) {
      URL.revokeObjectURL(fileToRemove.url);
    }

    const nextPreviews = mediaPreviews.filter((_, i) => i !== index);
    setMediaPreviews(nextPreviews);

    if (nextPreviews.length === 0) {
      setPostType('TEXT');
    } else {
      const hasVideo = nextPreviews.some((f) => f.isVideo);
      const hasImage = nextPreviews.some((f) => !f.isVideo);
      if (hasVideo) setPostType('VIDEO');
      else if (hasImage) setPostType('IMAGE');
    }
  };

  const moveFile = (index, direction) => {
    const swapIndex = direction === 'left' ? index - 1 : index + 1;
    if (swapIndex < 0 || swapIndex >= mediaPreviews.length) return;

    const updatedPreviews = [...mediaPreviews];

    // Swap previews
    const tempPreview = updatedPreviews[index];
    updatedPreviews[index] = updatedPreviews[swapIndex];
    updatedPreviews[swapIndex] = tempPreview;

    setMediaPreviews(updatedPreviews);
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
    setUploadProgress(10);

    const progressTimer = setInterval(() => {
      setUploadProgress((prev) => {
        if (prev >= 90) return prev;
        return prev + (prev < 40 ? 15 : 5);
      });
    }, 200);

    try {
      const fd = new FormData();
      fd.append('title', trimmedTitle);
      fd.append('content', content.trim());
      fd.append('postType', postType);
      fd.append('tags', selectedTags.join(','));
      fd.append('portfolioLink', portfolioLink.trim());
      fd.append('draft', String(asDraft));

      if (editingPost) {
        const keptIds = mediaPreviews
          .filter((p) => p.isExisting)
          .map((p) => p.id)
          .join(',');
        fd.append('keepMediaIds', keptIds);
      }

      // Upload files in the exact order of the preview list
      const newFilesOrdered = mediaPreviews
        .filter((p) => !p.isExisting)
        .map((p) => p.file);

      newFilesOrdered.forEach((file) => {
        fd.append('mediaFiles', file);
      });

      let post;
      if (editingPost) {
        post = await updateCommunityPost(editingPost.id, fd);
      } else {
        post = await createCommunityPost(fd);
      }

      clearInterval(progressTimer);
      setUploadProgress(100);
      setSuccess(true);
      onPosted?.(post);

      // Reset
      setTitle('');
      setContent('');
      mediaPreviews.forEach((item) => {
        if (!item.isExisting) URL.revokeObjectURL(item.url);
      });
      setMediaPreviews([]);
      setSelectedTags([]);
      setPortfolioLink('');

      if (!asDraft) {
        setTimeout(() => onClose?.(), 400);
      }
    } catch (err) {
      clearInterval(progressTimer);
      setUploadProgress(0);
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

  const triggerFileBrowser = () => {
    fileInputRef.current?.click();
  };

  const displayUrl = (preview) => {
    if (preview.isExisting) {
      // It is an absolute path or relative path from server
      if (preview.url.startsWith('http') || preview.url.startsWith('/')) {
        // Resolve path to backend API URL if it's relative
        if (preview.url.startsWith('/')) {
          const apiBase = process.env.REACT_APP_API_URL || 'http://localhost:8080';
          return `${apiBase}${preview.url}`;
        }
        return preview.url;
      }
    }
    return preview.url;
  };

  return (
    <div className="post-composer">
      <div className="post-composer-header">
        <h3>{editingPost ? 'edit post' : 'create post'}</h3>
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

        {/* Drag & Drop Upload Zone */}
        <div
          className={`post-composer-dropzone ${dragActive ? 'drag-active' : ''}`}
          onDragEnter={handleDrag}
          onDragOver={handleDrag}
          onDragLeave={handleDrag}
          onDrop={handleDrop}
          onClick={triggerFileBrowser}
        >
          <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12" />
          </svg>
          <p>Drag & drop your videos or images here, or click to browse</p>
          <span>Supports up to 10 files (Max: 15MB image / 60MB video)</span>
          <input
            type="file"
            ref={fileInputRef}
            style={{ display: 'none' }}
            multiple
            accept="image/*,video/*"
            onChange={(e) => handleFiles(e.target.files)}
          />
        </div>

        {/* Render Previews in Grid with Reordering */}
        {mediaPreviews.length > 0 && (
          <div className="post-composer-previews">
            {mediaPreviews.map((preview, idx) => (
              <div key={idx} className="post-composer-preview-card">
                {preview.isVideo ? (
                  <video src={displayUrl(preview)} className="post-composer-preview-media" muted />
                ) : (
                  <img src={displayUrl(preview)} alt="" className="post-composer-preview-media" />
                )}
                <button
                  type="button"
                  className="post-composer-preview-remove"
                  onClick={(e) => {
                    e.stopPropagation();
                    removeFile(idx);
                  }}
                  title="Remove file"
                >
                  ×
                </button>
                <div className="post-composer-preview-controls" onClick={(e) => e.stopPropagation()}>
                  <button
                    type="button"
                    className="post-composer-preview-btn"
                    onClick={() => moveFile(idx, 'left')}
                    disabled={idx === 0}
                    title="Move left"
                  >
                    ←
                  </button>
                  <button
                    type="button"
                    className="post-composer-preview-btn"
                    onClick={() => moveFile(idx, 'right')}
                    disabled={idx === mediaPreviews.length - 1}
                    title="Move right"
                  >
                    →
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}

        {submitting && (
          <div className="post-composer-progress-container">
            <div className="post-composer-progress-bar" style={{ width: `${uploadProgress}%` }} />
          </div>
        )}

        {error && <p className="post-composer-error">{error}</p>}
        {success && <p className="post-composer-success">{editingPost ? 'Post updated successfully.' : 'Published to the community feed.'}</p>}

        <div className="post-composer-actions">
          {!editingPost && (
            <button type="button" className="post-composer-draft-btn" disabled={submitting} onClick={handleSaveDraft}>
              {submitting && !publishing ? 'Saving…' : 'Save draft'}
            </button>
          )}
          <button 
            type="submit" 
            className="post-composer-submit" 
            disabled={submitting}
            style={editingPost ? { flex: 1 } : undefined}
          >
            {publishing ? 'Publishing…' : editingPost ? 'Save changes' : 'Publish'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default PostComposer;
