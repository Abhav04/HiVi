const POST_TYPE_META = {
  VIDEO: { label: 'Cinematic reel', badge: 'reel', accent: 'purple' },
  PORTFOLIO: { label: 'Portfolio showcase', badge: 'portfolio', accent: 'orange' },
  IMAGE: { label: 'Creative work', badge: 'visual', accent: 'purple' },
  TEXT: { label: 'Creator update', badge: 'post', accent: 'neutral' },
};

export function getPostTypeMeta(postType) {
  return POST_TYPE_META[postType] || POST_TYPE_META.TEXT;
}

export function formatTimeAgo(isoDate) {
  if (!isoDate) return '';
  const then = new Date(isoDate).getTime();
  const now = Date.now();
  const sec = Math.floor((now - then) / 1000);
  if (sec < 60) return 'just now';
  const min = Math.floor(sec / 60);
  if (min < 60) return `${min}m`;
  const hr = Math.floor(min / 60);
  if (hr < 24) return `${hr}h`;
  const day = Math.floor(hr / 24);
  if (day < 7) return `${day}d`;
  const wk = Math.floor(day / 7);
  if (wk < 5) return `${wk}w`;
  return new Date(isoDate).toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
}

export function creatorRole(author) {
  if (!author) return 'Video creator';
  if (author.niche) return author.niche;
  return 'Video editor';
}

export function formatCount(n) {
  const num = Number(n) || 0;
  if (num >= 1_000_000) return `${(num / 1_000_000).toFixed(1)}M`;
  if (num >= 1000) return `${(num / 1000).toFixed(1)}k`;
  return String(num);
}
