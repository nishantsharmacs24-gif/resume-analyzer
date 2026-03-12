// src/components/ScoreCard.jsx
// Displays resume analysis results with animated score

export default function ScoreCard({ result }) {
  if (!result) return null;

  const score = result.matchScore || 0;

  const getScoreColor = (s) => {
    if (s >= 80) return '#10b981'; // green
    if (s >= 60) return '#f59e0b'; // yellow
    if (s >= 40) return '#f97316'; // orange
    return '#ef4444';              // red
  };

  const getScoreLabel = (s) => {
    if (s >= 80) return 'Excellent Match 🟢';
    if (s >= 60) return 'Good Match 🟡';
    if (s >= 40) return 'Partial Match 🟠';
    return 'Low Match 🔴';
  };

  const color = getScoreColor(score);
  const circumference = 2 * Math.PI * 54; // circle radius = 54
  const strokeDashoffset = circumference - (score / 100) * circumference;

  return (
    <div style={styles.container}>

      {/* ── Circular Score Gauge ── */}
      <div style={styles.gaugeSection}>
        <svg width="140" height="140" viewBox="0 0 120 120">
          {/* Background circle */}
          <circle cx="60" cy="60" r="54"
            fill="none" stroke="rgba(255,255,255,0.05)" strokeWidth="10"/>
          {/* Score arc */}
          <circle cx="60" cy="60" r="54"
            fill="none" stroke={color} strokeWidth="10"
            strokeDasharray={circumference}
            strokeDashoffset={strokeDashoffset}
            strokeLinecap="round"
            transform="rotate(-90 60 60)"
            style={{ transition: 'stroke-dashoffset 1.5s ease', filter: `drop-shadow(0 0 8px ${color})` }}
          />
          <text x="60" y="55" textAnchor="middle"
            fill={color} fontSize="22" fontWeight="900"
            fontFamily="'Orbitron', monospace">
            {Math.round(score)}
          </text>
          <text x="60" y="72" textAnchor="middle"
            fill="#94a3b8" fontSize="11"
            fontFamily="'Rajdhani', sans-serif">
            MATCH %
          </text>
        </svg>
        <div style={{ ...styles.label, color }}>{getScoreLabel(score)}</div>
      </div>

      {/* ── Keywords Matched ── */}
      <div style={styles.section}>
        <h4 style={styles.sectionTitle}>✅ Keywords Matched</h4>
        <div style={styles.tagContainer}>
          {(result.keywordsMatched || '').split(',').filter(Boolean).map((kw, i) => (
            <span key={i} style={{ ...styles.tag, background: 'rgba(16,185,129,0.15)',
              border: '1px solid rgba(16,185,129,0.3)', color: '#10b981' }}>
              {kw.trim()}
            </span>
          ))}
        </div>
      </div>

      {/* ── Missing Keywords ── */}
      <div style={styles.section}>
        <h4 style={styles.sectionTitle}>❌ Missing Keywords</h4>
        <div style={styles.tagContainer}>
          {(result.keywordsMissing || '').split(',').filter(Boolean).map((kw, i) => (
            <span key={i} style={{ ...styles.tag, background: 'rgba(239,68,68,0.1)',
              border: '1px solid rgba(239,68,68,0.3)', color: '#ef4444' }}>
              {kw.trim()}
            </span>
          ))}
        </div>
      </div>

      {/* ── AI Feedback ── */}
      {result.feedback && (
        <div style={styles.section}>
          <h4 style={styles.sectionTitle}>🤖 AI Feedback</h4>
          <div style={styles.textBox}>
            {result.feedback.split('\n').map((line, i) => (
              <p key={i} style={{ marginBottom: '8px', color: '#cbd5e1' }}>{line}</p>
            ))}
          </div>
        </div>
      )}

      {/* ── Suggestions ── */}
      {result.suggestions && (
        <div style={styles.section}>
          <h4 style={styles.sectionTitle}>💡 Improvement Suggestions</h4>
          <div style={styles.textBox}>
            {result.suggestions.split('\n').map((line, i) => (
              <p key={i} style={{ marginBottom: '8px', color: '#cbd5e1' }}>{line}</p>
            ))}
          </div>
        </div>
      )}

      {/* ── Summary ── */}
      {result.summary && (
        <div style={styles.section}>
          <h4 style={styles.sectionTitle}>📝 Candidate Summary</h4>
          <p style={{ color: '#94a3b8', lineHeight: 1.8 }}>{result.summary}</p>
        </div>
      )}
    </div>
  );
}

const styles = {
  container: { display: 'flex', flexDirection: 'column', gap: '28px' },
  gaugeSection: { display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '12px' },
  label: { fontFamily: "'Orbitron', monospace", fontSize: '0.85rem', fontWeight: '700', letterSpacing: '1px' },
  section: {},
  sectionTitle: {
    fontFamily: "'Orbitron', monospace",
    fontSize: '0.8rem', fontWeight: '700',
    color: '#00f5ff', letterSpacing: '1px',
    marginBottom: '14px', textTransform: 'uppercase',
  },
  tagContainer: { display: 'flex', flexWrap: 'wrap', gap: '8px' },
  tag: {
    padding: '4px 12px', borderRadius: '20px',
    fontSize: '0.82rem',
    fontFamily: "'Rajdhani', sans-serif", fontWeight: '600',
  },
  textBox: {
    background: 'rgba(255,255,255,0.03)',
    border: '1px solid rgba(255,255,255,0.08)',
    borderRadius: '10px', padding: '16px',
  },
};
