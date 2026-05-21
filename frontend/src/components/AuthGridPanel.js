import React, { useEffect, useRef } from 'react';
import './AuthGridPanel.css';

const COLS = 18;
const ROWS = 14;
const TRAIL_LENGTH = 14;
const LERP = 0.14;

const PURPLE = { r: 168, g: 85, b: 247 };
const PURPLE_DEEP = { r: 124, g: 58, b: 237 };
const PURPLE_BRIGHT = { r: 216, g: 180, b: 255 };

const AuthGridPanel = () => {
  const canvasRef = useRef(null);
  const frameRef = useRef(0);
  const pointerRef = useRef({ x: -1000, y: -1000, tx: -1000, ty: -1000 });
  const trailRef = useRef([]);
  const sizeRef = useRef({ w: 0, h: 0, cellW: 0, cellH: 0 });

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return undefined;

    const ctx = canvas.getContext('2d', { alpha: false });
    let running = true;

    const resize = () => {
      const parent = canvas.parentElement;
      if (!parent) return;
      const dpr = Math.min(window.devicePixelRatio || 1, 2);
      const w = parent.clientWidth;
      const h = parent.clientHeight;
      canvas.width = Math.floor(w * dpr);
      canvas.height = Math.floor(h * dpr);
      canvas.style.width = `${w}px`;
      canvas.style.height = `${h}px`;
      ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
      sizeRef.current = {
        w,
        h,
        cellW: w / COLS,
        cellH: h / ROWS,
      };
    };

    const onMove = (e) => {
      const rect = canvas.getBoundingClientRect();
      pointerRef.current.tx = e.clientX - rect.left;
      pointerRef.current.ty = e.clientY - rect.top;
    };

    const onLeave = () => {
      pointerRef.current.tx = -1000;
      pointerRef.current.ty = -1000;
    };

    const draw = () => {
      if (!running) return;
      const { w, h, cellW, cellH } = sizeRef.current;
      if (w <= 0 || h <= 0) {
        frameRef.current = requestAnimationFrame(draw);
        return;
      }

      const ptr = pointerRef.current;
      ptr.x += (ptr.tx - ptr.x) * LERP;
      ptr.y += (ptr.ty - ptr.y) * LERP;

      const trail = trailRef.current;
      trail.unshift({ x: ptr.x, y: ptr.y });
      if (trail.length > TRAIL_LENGTH) trail.length = TRAIL_LENGTH;

      ctx.fillStyle = '#080808';
      ctx.fillRect(0, 0, w, h);

      for (let row = 0; row < ROWS; row += 1) {
        for (let col = 0; col < COLS; col += 1) {
          const cx = col * cellW + cellW * 0.5;
          const cy = row * cellH + cellH * 0.5;
          let intensity = 0;

          trail.forEach((point, i) => {
            if (point.x < 0) return;
            const dx = cx - point.x;
            const dy = cy - point.y;
            const dist = Math.sqrt(dx * dx + dy * dy);
            const maxDist = Math.min(w, h) * 0.22;
            if (dist < maxDist) {
              const falloff = 1 - dist / maxDist;
              const trailWeight = 1 - i / TRAIL_LENGTH;
              intensity += falloff * falloff * trailWeight * 1.35;
            }
          });

          intensity = Math.min(intensity, 1);
          if (intensity < 0.02) continue;

          const pad = 2;
          const x = col * cellW + pad;
          const y = row * cellH + pad;
          const bw = cellW - pad * 2;
          const bh = cellH - pad * 2;

          const r = Math.round(PURPLE_DEEP.r + (PURPLE_BRIGHT.r - PURPLE_DEEP.r) * intensity);
          const g = Math.round(PURPLE_DEEP.g + (PURPLE_BRIGHT.g - PURPLE_DEEP.g) * intensity);
          const b = Math.round(PURPLE_DEEP.b + (PURPLE_BRIGHT.b - PURPLE_DEEP.b) * intensity);
          const alpha = 0.08 + intensity * 0.55;

          ctx.fillStyle = `rgba(${r},${g},${b},${alpha})`;
          ctx.fillRect(x, y, bw, bh);

          if (intensity > 0.35) {
            ctx.shadowColor = `rgba(${PURPLE.r},${PURPLE.g},${PURPLE.b},${intensity * 0.7})`;
            ctx.shadowBlur = 18 * intensity;
            ctx.strokeStyle = `rgba(${PURPLE_BRIGHT.r},${PURPLE_BRIGHT.g},${PURPLE_BRIGHT.b},${intensity * 0.5})`;
            ctx.lineWidth = 1;
            ctx.strokeRect(x + 0.5, y + 0.5, bw - 1, bh - 1);
            ctx.shadowBlur = 0;
          }
        }
      }

      if (ptr.x >= 0 && ptr.x <= w && ptr.y >= 0 && ptr.y <= h) {
        const glow = ctx.createRadialGradient(ptr.x, ptr.y, 0, ptr.x, ptr.y, Math.min(w, h) * 0.35);
        glow.addColorStop(0, 'rgba(168, 85, 247, 0.18)');
        glow.addColorStop(0.4, 'rgba(124, 58, 237, 0.08)');
        glow.addColorStop(1, 'rgba(8, 8, 8, 0)');
        ctx.fillStyle = glow;
        ctx.fillRect(0, 0, w, h);
      }

      ctx.strokeStyle = 'rgba(255,255,255,0.03)';
      ctx.lineWidth = 1;
      for (let c = 0; c <= COLS; c += 1) {
        const x = c * cellW + 0.5;
        ctx.beginPath();
        ctx.moveTo(x, 0);
        ctx.lineTo(x, h);
        ctx.stroke();
      }
      for (let r = 0; r <= ROWS; r += 1) {
        const y = r * cellH + 0.5;
        ctx.beginPath();
        ctx.moveTo(0, y);
        ctx.lineTo(w, y);
        ctx.stroke();
      }

      frameRef.current = requestAnimationFrame(draw);
    };

    resize();
    window.addEventListener('resize', resize);
    canvas.addEventListener('mousemove', onMove);
    canvas.addEventListener('mouseleave', onLeave);
    frameRef.current = requestAnimationFrame(draw);

    return () => {
      running = false;
      cancelAnimationFrame(frameRef.current);
      window.removeEventListener('resize', resize);
      canvas.removeEventListener('mousemove', onMove);
      canvas.removeEventListener('mouseleave', onLeave);
    };
  }, []);

  return <canvas ref={canvasRef} className="auth-grid-canvas" aria-hidden />;
};

export default AuthGridPanel;
