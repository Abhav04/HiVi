import React, { useEffect, useMemo, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import DashboardLayout from '../components/dashboard/DashboardLayout';
import { fetchCreatorProfile, toggleFollow } from '../services/communityApi';
import { getToken } from '../utils/auth';
import { formatCount } from '../utils/communityFormat';
import CreatorAvatar from '../components/community/CreatorAvatar';
import CommunityPostCard from '../components/community/CommunityPostCard';
import './CreatorProfilePage.css';

const TABS = [
  { id: 'all', label: 'All posts' },
  { id: 'portfolio', label: 'Portfolio' },
  { id: 'video', label: 'Videos' },
  { id: 'image', label: 'Visuals' },
];

const CreatorProfilePage = () => {
  const { username } = useParams();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [tab, setTab] = useState('all');

  useEffect(() => {
    setLoading(true);
    setTab('all');
    fetchCreatorProfile(username)
      .then(setProfile)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  }, [username]);

  const filteredPosts = useMemo(() => {
    const posts = profile?.recentPosts || [];
    if (tab === 'all') return posts;
    if (tab === 'portfolio') return posts.filter((p) => p.postType === 'PORTFOLIO');
    if (tab === 'video') return posts.filter((p) => p.postType === 'VIDEO');
    if (tab === 'image') return posts.filter((p) => p.postType === 'IMAGE');
    return posts;
  }, [profile, tab]);

  const handleFollow = async () => {
    if (!getToken() || !profile) return;
    try {
      const res = await toggleFollow(profile.userId);
      setProfile((p) => ({ ...p, following: res.following }));
    } catch {
      /* auth */
    }
  };

  const authorForAvatar = profile
    ? {
        username: profile.username,
        displayName: profile.displayName,
        avatarUrl: profile.avatarUrl,
        niche: profile.niche,
        availableForWork: profile.availableForWork,
      }
    : null;

  return (
    <DashboardLayout>
      <div className="creator-profile">
        {loading && <p className="creator-profile__loading">Loading profile…</p>}
        {error && <p className="creator-profile__error">{error}</p>}

        {profile && (
          <>
            <div
              className="creator-profile__banner"
              style={
                profile.bannerUrl
                  ? { backgroundImage: `url(${profile.bannerUrl})` }
                  : undefined
              }
            />
            <div className="creator-profile__overlay" />

            <div className="creator-profile__shell">
              <Link to="/community" className="creator-profile__back">
                ← Back to feed
              </Link>

              <header className="creator-profile__header">
                <CreatorAvatar author={authorForAvatar} size="lg" link={false} />
                <div className="creator-profile__intro">
                  <div className="creator-profile__title-row">
                    <h1>{profile.displayName}</h1>
                    {profile.availableForWork && (
                      <span className="creator-profile__available">Available for work</span>
                    )}
                  </div>
                  <p className="creator-profile__handle">@{profile.username}</p>
                  {profile.niche && <p className="creator-profile__headline">{profile.niche}</p>}
                </div>
                <div className="creator-profile__actions">
                  <button
                    type="button"
                    className={`creator-profile__follow ${profile.following ? 'creator-profile__follow--on' : ''}`}
                    onClick={handleFollow}
                  >
                    {profile.following ? 'Following' : '+ Follow'}
                  </button>
                  {profile.portfolioUrl && (
                    <a
                      href={profile.portfolioUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      className="creator-profile__secondary-btn"
                    >
                      Portfolio site
                    </a>
                  )}
                </div>
              </header>

              {profile.bio && <p className="creator-profile__bio">{profile.bio}</p>}

              {profile.tools && (
                <div className="creator-profile__tools">
                  <span className="creator-profile__tools-label">Tools</span>
                  <span>{profile.tools}</span>
                </div>
              )}

              <div className="creator-profile__stats">
                <div>
                  <strong>{formatCount(profile.totalPosts)}</strong>
                  <span>Posts</span>
                </div>
                <div>
                  <strong>{formatCount(profile.totalLikes)}</strong>
                  <span>Likes</span>
                </div>
                <div>
                  <strong>{formatCount(profile.followerCount)}</strong>
                  <span>Followers</span>
                </div>
                <div>
                  <strong>{formatCount(profile.followingCount)}</strong>
                  <span>Following</span>
                </div>
                <div>
                  <strong>{formatCount(profile.totalViews)}</strong>
                  <span>Views</span>
                </div>
              </div>

              {(profile.instagramUrl || profile.youtubeUrl || profile.websiteUrl) && (
                <div className="creator-profile__socials">
                  {profile.instagramUrl && (
                    <a href={profile.instagramUrl} target="_blank" rel="noopener noreferrer">
                      Instagram
                    </a>
                  )}
                  {profile.youtubeUrl && (
                    <a href={profile.youtubeUrl} target="_blank" rel="noopener noreferrer">
                      YouTube
                    </a>
                  )}
                  {profile.websiteUrl && (
                    <a href={profile.websiteUrl} target="_blank" rel="noopener noreferrer">
                      Website
                    </a>
                  )}
                </div>
              )}

              <nav className="creator-profile__tabs">
                {TABS.map((t) => (
                  <button
                    key={t.id}
                    type="button"
                    className={tab === t.id ? 'active' : ''}
                    onClick={() => setTab(t.id)}
                  >
                    {t.label}
                  </button>
                ))}
              </nav>

              <section className="creator-profile__posts">
                {filteredPosts.length === 0 ? (
                  <p className="creator-profile__empty">No posts in this category yet.</p>
                ) : (
                  filteredPosts.map((p) => (
                    <CommunityPostCard key={p.id} post={p} onUpdate={() => fetchCreatorProfile(username).then(setProfile)} />
                  ))
                )}
              </section>
            </div>
          </>
        )}
      </div>
    </DashboardLayout>
  );
};

export default CreatorProfilePage;
