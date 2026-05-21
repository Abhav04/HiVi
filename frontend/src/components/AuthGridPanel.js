import React, { useCallback, useMemo, useState } from 'react';
import './AuthGridPanel.css';

const COLS = 14;
const ROWS = 12;

const AuthGridPanel = () => {
  const [hover, setHover] = useState({ col: -1, row: -1 });

  const cells = useMemo(() => {
    const list = [];
    for (let r = 0; r < ROWS; r += 1) {
      for (let c = 0; c < COLS; c += 1) {
        list.push({ r, c });
      }
    }
    return list;
  }, []);

  const onMove = useCallback((e) => {
    const rect = e.currentTarget.getBoundingClientRect();
    const col = Math.floor(((e.clientX - rect.left) / rect.width) * COLS);
    const row = Math.floor(((e.clientY - rect.top) / rect.height) * ROWS);
    setHover({
      col: Math.max(0, Math.min(COLS - 1, col)),
      row: Math.max(0, Math.min(ROWS - 1, row)),
    });
  }, []);

  const getCellClass = (r, c) => {
    if (hover.col < 0) return 'auth-grid-cell';
    const dist = Math.max(Math.abs(hover.col - c), Math.abs(hover.row - r));
    if (dist === 0) return 'auth-grid-cell active';
    if (dist === 1) return 'auth-grid-cell near';
    return 'auth-grid-cell';
  };

  return (
    <div
      className="auth-grid-panel"
      onMouseMove={onMove}
      onMouseLeave={() => setHover({ col: -1, row: -1 })}
    >
      {cells.map(({ r, c }) => (
        <div key={`${r}-${c}`} className={getCellClass(r, c)} />
      ))}
    </div>
  );
};

export default AuthGridPanel;
