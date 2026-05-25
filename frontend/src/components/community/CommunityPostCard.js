import React, { memo, useCallback, useState } from 'react';
import { Link } from 'react-router-dom';
import { mediaFullUrl, getToken } from '../../utils/auth';
import { formatTimeAgo, getPostTypeMeta, creatorRole, formatCount } from '../../utils/communityFormat';
import { toggleLike, toggleBookmark, fetchComments, addComment, recordPostView } from '../../services/communityApi';
import CreatorAvatar from './CreatorAvatar';
import './CommunityPostCard.css';

const CommunityPostCard = memo(function CommunityPostCard({ post, variant = 'feed', onUpdate }) {
  const [liked, setLiked] = useState(post.likedByMe);
  const [likeCount, setLikeCount] = useState(post.likeCount);
  const [bookmarked, setBookmarked] = useState(post.bookmarkedByMe);
  const [showComments, setShowComments] = useState(false);
  const [comments, setComments] = useState([]);
  const [commentText, setCommentText] = useState('');
  const [likeAnim, setLikeAnim] = useState(false);

  const mediaUrl = mediaFullUrl(post.thumbnailUrl || post.mediaUrl);
  const isVideo = post.postType === 'VIDEO';
  const typeMeta = getPostTypeMeta(post.postType);
  const timeAgo = formatTimeAgo(post.createdAt);
  const role = creatorRole(post.author);

  React.useEffect(() => {
    recordPostView(post.id).catch(() => {});
  }, [post.id]);

  const handleLike = useCallback(async (e) => {
    e.stopPropagation();
    if (!getToken()) return;
    try {
      const res = await toggleLike(post.id);
      setLiked(res.liked);
      setLikeCount((c) => (res.liked ? c + 1 : Math.max(0, c - 1)));
      setLikeAnim(true);
      setTimeout(() => setLikeAnim(false), 600);
      onUpdate?.();
    } catch {
      /* auth */
    }
  }, [post.id, onUpdate]);

  const handleBookmark = useCallback(async (e) => {
    e.stopPropagation();
    if (!getToken()) return;
    try {
      const res = await toggleBookmark(post.id);
      setBookmarked(res.bookmarked);
    } catch {
      /* ignore */
    }
  }, [post.id]);

  const handleShare = useCallback(async (e) => {
    e.stopPropagation();
    const url = `${window.location.origin}/community/creator/${post.author?.username}`;
    try {
      if (navigator.share) {
        await navigator.share({ title: post.title, url });
      } else {
        await navigator.clipboard.writeText(url);
      }
    } catch {
      /* cancelled */
    }
  }, [post.author?.username, post.title]);

  const loadComments = useCallback(async () => {
    const data = await fetchComments(post.id);
    setComments(data);
  }, [post.id]);

  const toggleCommentsPanel = useCallback(async (e) => {
    e.stopPropagation();
    if (!showComments) await loadComments();
    setShowComments((v) => !v);
  }, [showComments, loadComments]);

  const submitComment = useCallback(async (e) => {
    e.preventDefault();
    if (!commentText.trim() || !getToken()) return;
    await addComment(post.id, commentText.trim());
    setCommentText('');
    await loadComments();
  }, [post.id, commentText, loadComments]);

  const isFeatured = variant === 'featured';
  const isCompact = variant === 'compact';

  return (
    <article
      className={`feed-post feed-post--${variant} feed-post--${typeMeta.accent} ${post.postType === 'PORTFOLIO' ? 'feed-post--portfolio' : ''}`}
    >
      <header className="feed-post__header">
        <CreatorAvatar author={post.author} size={isCompact ? 'sm' : 'md'} />
        <div className="feed-post__meta">
          <div className="feed-post__meta-top">
            <Link
              to={`/community/creator/${post.author?.username}`}
              className="feed-post__name"
              onClick={(e) => e.stopPropagation()}
            >
              {post.author?.displayName}
            </Link>
            {post.author?.availableForWork && (
              <span className="feed-post__badge feed-post__badge--available">Available</span>
            )}
            <span className={`feed-post__badge feed-post__badge--type feed-post__badge--${typeMeta.badge}`}>
              {typeMeta.label}
            </span>
          </div>
          <p className="feed-post__role">
            {role}
            {timeAgo && (
              <>
                <span className="feed-post__dot">·</span>
                <span className="feed-post__time">{timeAgo}</span>
              </>
            )}
          </p>
        </div>
      </header>

      <div className="feed-post__body">
        {post.title && <h3 className="feed-post__title">{post.title}</h3>}
        {post.content && <p className="feed-post__caption">{post.content}</p>}
        {post.tags?.length > 0 && (
          <div className="feed-post__tags">
            {post.tags.map((t) => (
              <span key={t} className="feed-post__tag">
                #{t}
              </span>
            ))}
          </div>
        )}
      </div>

      {(mediaUrl || isFeatured) && (
        <div className={`feed-post__media ${isFeatured ? 'feed-post__media--hero' : ''}`}>
          {mediaUrl ? (
            isVideo ? (
              <video
                src={mediaFullUrl(post.mediaUrl)}
                className="feed-post__video"
                controls
                preload="metadata"
                poster={mediaUrl}
              />
            ) : (
              <img src={mediaUrl} alt="" className="feed-post__image" loading="lazy" />
            )
          ) : (
            <div className="feed-post__placeholder">Featured work</div>
          )}
        </div>
      )}

      <footer className="feed-post__footer">
        <div className="feed-post__stats">
          <span>{formatCount(likeCount)} likes</span>
          <span className="feed-post__dot">·</span>
          <span>{formatCount(post.commentCount)} comments</span>
          {post.viewCount > 0 && (
            <>
              <span className="feed-post__dot">·</span>
              <span>{formatCount(post.viewCount)} views</span>
            </>
          )}
        </div>
        <div className="feed-post__actions">
          <button
            type="button"
            className={`feed-post__action ${liked ? 'feed-post__action--liked' : ''} ${likeAnim ? 'feed-post__action--pop' : ''}`}
            onClick={handleLike}
            aria-label="Like"
          >
            <span className="feed-post__action-icon">♥</span>
            <span>Like</span>
          </button>
          <button type="button" className="feed-post__action" onClick={toggleCommentsPanel} aria-label="Comment">
            <span className="feed-post__action-icon">💬</span>
            <span>Comment</span>
          </button>
          <button type="button" className="feed-post__action" onClick={handleShare} aria-label="Share">
            <span className="feed-post__action-icon">↗</span>
            <span>Share</span>
          </button>
          <button
            type="button"
            className={`feed-post__action ${bookmarked ? 'feed-post__action--saved' : ''}`}
            onClick={handleBookmark}
            aria-label="Save"
          >
            <span className="feed-post__action-icon">{bookmarked ? '★' : '☆'}</span>
            <span>Save</span>
          </button>
          {post.portfolioLink && (
            <a
              href={post.portfolioLink}
              target="_blank"
              rel="noopener noreferrer"
              className="feed-post__action feed-post__action--link"
              onClick={(e) => e.stopPropagation()}
            >
              Portfolio →
            </a>
          )}
        </div>

        {showComments && (
          <div className="feed-post__comments">
            {comments.map((c) => (
              <div key={c.id} className="feed-post__comment">
                <strong>@{c.author?.username}</strong> {c.content}
                {c.replies?.map((r) => (
                  <div key={r.id} className="feed-post__comment feed-post__comment--reply">
                    <strong>@{r.author?.username}</strong> {r.content}
                  </div>
                ))}
              </div>
            ))}
            <form className="feed-post__comment-form" onSubmit={submitComment}>
              <input
                value={commentText}
                onChange={(e) => setCommentText(e.target.value)}
                placeholder={getToken() ? 'Add a comment…' : 'Sign in to comment'}
                disabled={!getToken()}
              />
              <button type="submit" disabled={!getToken()}>
                Post
              </button>
            </form>
          </div>
        )}
      </footer>
    </article>
  );
});

export default CommunityPostCard;
