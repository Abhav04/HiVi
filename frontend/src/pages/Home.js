import React, { useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import Navbar from '../components/Navbar';
import './Home.css';

const stats = [
  { value: '4,200+', label: 'verified editors' },
  { value: '98%', label: 'satisfaction rate' },
  { value: '72hrs', label: 'avg. turnaround' },
  { value: '₹0', label: 'platform fee' },
];

const marqueeItems = [
  'COLOR GRADING', 'MOTION GRAPHICS', 'DOCUMENTARY', 'REELS', 'VFX', 'COMMERCIAL', 'WEDDING', 'YOUTUBE', 'CORPORATE',
  'COLOR GRADING', 'MOTION GRAPHICS', 'DOCUMENTARY', 'REELS', 'VFX', 'COMMERCIAL', 'WEDDING', 'YOUTUBE', 'CORPORATE',
];

const howItWorks = [
  { num: '01', title: 'post your project', desc: 'Describe your vision — genre, style, deadline, and budget. Takes less than 3 minutes.' },
  { num: '02', title: 'match with editors', desc: 'Our algorithm surfaces only the top-tier verified editors who match your exact requirements.' },
  { num: '03', title: 'review & hire', desc: 'Watch their reels, review portfolios, and hire with a single click. Payment held in escrow.' },
  { num: '04', title: 'receive perfection', desc: 'Collaborate in real-time, request revisions, and release payment only when satisfied.' },
];

const testimonials = [
  { name: 'Karan Mehta', role: 'YouTube Creator — 2.1M subs', quote: 'Found my go-to editor within a day. The quality is unlike anything I\'ve seen on other platforms.', avatar: 'KM' },
  { name: 'Ananya Singh', role: 'Wedding Photographer', quote: 'My clients cry watching the films. That\'s the edit quality HIVI editors bring to every project.', avatar: 'AS' },
  { name: 'Rohan Iyer', role: 'Brand Marketing Head', quote: 'We\'ve used HIVI for 40+ brand films. Consistent, fast, and always cinematic.', avatar: 'RI' },
];

const Home = () => {
  const heroRef = useRef(null);

  useEffect(() => {
    const handleMouse = (e) => {
      if (!heroRef.current) return;
      const { clientX, clientY } = e;
      const { innerWidth, innerHeight } = window;
      const x = (clientX / innerWidth - 0.5) * 20;
      const y = (clientY / innerHeight - 0.5) * 20;
      heroRef.current.style.setProperty('--mx', `${x}px`);
      heroRef.current.style.setProperty('--my', `${y}px`);
    };
    window.addEventListener('mousemove', handleMouse);
    return () => window.removeEventListener('mousemove', handleMouse);
  }, []);

  return (
    <div className="home">
      <Navbar transparent />

      {/* Hero */}
      <section className="hero" ref={heroRef}>
        <div className="hero-bg">
          <div className="hero-pillars">
            {[...Array(9)].map((_, i) => (
              <div key={i} className="pillar" style={{ animationDelay: `${i * 0.12}s` }} />
            ))}
          </div>
          <div className="hero-glow" />
        </div>

        <div className="hero-content">
          <div className="hero-badge animate-fadeUp">
            <span className="badge-dot" />
            crafted for storytellers
          </div>

          <h1 className="hero-headline animate-fadeUp delay-1">
            where <em>exceptional</em><br/>
            editors meet bold<br/>
            <span className="headline-accent">visions</span>
          </h1>

          <p className="hero-sub animate-fadeUp delay-2">
            HIVI is India's most exclusive video editing marketplace —
            a curated network of elite editors handpicked for quality.
          </p>

          <div className="hero-cta animate-fadeUp delay-3">
            <Link to="/signup" className="cta-primary">
              start hiring
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <path d="M5 12h14M12 5l7 7-7 7"/>
              </svg>
            </Link>
            <Link to="/feed" className="cta-secondary">
              browse editors
            </Link>
          </div>

          <div className="hero-trust animate-fadeUp delay-4">
            <div className="trust-avatars">
              {['AK', 'PS', 'MW', 'ZN', 'CW'].map((i, idx) => (
                <div key={i} className="trust-avatar" style={{ zIndex: 5 - idx, transform: `translateX(${idx * -10}px)` }}>{i}</div>
              ))}
            </div>
            <span className="trust-text">join 4,200+ verified editors</span>
          </div>
        </div>

        {/* Stats strip */}
        <div className="hero-stats animate-fadeUp delay-5">
          {stats.map((s, i) => (
            <div key={i} className="stat-item">
              <span className="stat-value">{s.value}</span>
              <span className="stat-label">{s.label}</span>
            </div>
          ))}
        </div>
      </section>

      {/* Marquee */}
      <div className="marquee-wrap" id="how">
        <div className="marquee-track">
          {marqueeItems.map((item, i) => (
            <span key={i} className="marquee-item">
              {item} <span className="marquee-dot">◆</span>
            </span>
          ))}
        </div>
      </div>

      {/* How It Works */}
      <section className="how-section" id="talent">
        <div className="section-inner">
          <div className="section-label">the process</div>
          <h2 className="section-title">
            four steps to<br/><em>flawless footage</em>
          </h2>
          <div className="how-grid">
            {howItWorks.map((step, i) => (
              <div
                key={i}
                className="how-card animate-fadeUp"
                style={{ animationDelay: `${i * 0.1}s` }}
              >
                <span className="how-num">{step.num}</span>
                <div className="how-line" />
                <h3 className="how-title">{step.title}</h3>
                <p className="how-desc">{step.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA Banner */}
      <section className="cta-banner" id="hire">
        <div className="banner-inner">
          <div className="banner-left">
            <p className="banner-eyebrow">for editors</p>
            <h2 className="banner-title">
              your craft deserves<br/>
              <em>better opportunities</em>
            </h2>
            <p className="banner-desc">
              Join India's most prestigious editor network. Work with top brands, creators, and filmmakers — on your terms.
            </p>
            <Link to="/signup" className="cta-primary" style={{ marginTop: 8 }}>
              apply as editor
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                <path d="M5 12h14M12 5l7 7-7 7"/>
              </svg>
            </Link>
          </div>
          <div className="banner-right">
            <div className="banner-card">
              <div className="bc-header">
                <div className="bc-dot available" />
                <span>member since 2024</span>
              </div>
              <div className="bc-name">Aryan Kapoor</div>
              <div className="bc-title">Cinematic Editor</div>
              <div className="bc-stats">
                <div><span className="bc-val">₹6.5L</span><span className="bc-lbl">earned</span></div>
                <div><span className="bc-val">113</span><span className="bc-lbl">projects</span></div>
                <div><span className="bc-val">4.9★</span><span className="bc-lbl">rating</span></div>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Testimonials */}
      <section className="testimonials-section">
        <div className="section-inner">
          <div className="section-label">social proof</div>
          <h2 className="section-title">
            trusted by those who<br/><em>demand the best</em>
          </h2>
          <div className="testimonials-grid">
            {testimonials.map((t, i) => (
              <div key={i} className="testimonial-card animate-fadeUp" style={{ animationDelay: `${i * 0.12}s` }}>
                <div className="t-quote">"</div>
                <p className="t-text">{t.quote}</p>
                <div className="t-author">
                  <div className="t-avatar">{t.avatar}</div>
                  <div>
                    <div className="t-name">{t.name}</div>
                    <div className="t-role">{t.role}</div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="footer">
        <div className="footer-inner">
          <div className="footer-brand">
            <svg width="22" height="22" viewBox="0 0 28 28" fill="none">
              <polygon points="14,2 26,8 26,20 14,26 2,20 2,8" stroke="#c9a84c" strokeWidth="1.5" fill="none"/>
              <circle cx="14" cy="14" r="2" fill="#c9a84c"/>
            </svg>
            <span className="footer-logo">HIVI</span>
          </div>
          <p className="footer-tagline">crafted for the exceptional.</p>
          <div className="footer-links">
<Link to="/privacy">privacy</Link>
<Link to="/terms">terms</Link>
<Link to="/contact">contact</Link>
<Link to="/blog">blog</Link>          </div>
          <p className="footer-copy">© 2025 HIVI. all rights reserved.</p>
        </div>
      </footer>
    </div>
  );
};

export default Home;