// src/pages/InterviewerPortal.jsx
import { useState } from 'react';
import Navbar from '../components/Navbar';
import { bulkUploadResumes, updateCandidateDecision, getAuthData } from '../services/api';

export default function InterviewerPortal() {
  const { userId, userName } = getAuthData();
  const [files, setFiles] = useState([]);
  const [jd, setJd] = useState('');
  const [candidates, setCandidates] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [view, setView] = useState('upload'); // upload | results
  const [minScore, setMinScore] = useState(0);
  const [selected, setSelected] = useState(null);

  const handleFileChange = (e) => {
    setFiles(Array.from(e.target.files));
  };

  const handleUpload = async () => {
    if (files.length === 0) return setError('Please select at least one resume!');
    if (!jd.trim()) return setError('Please enter the job description!');
    setLoading(true); setError('');

    try {
      const res = await bulkUploadResumes(files, jd, userId);
      setCandidates(res.data.rankedCandidates || []);
      setView('results');
    } catch (err) {
      setError('Upload failed. Make sure backend and ML service are running!');
    } finally {
      setLoading(false);
    }
  };

  const handleDecision = async (analysisId, decision, idx) => {
    try {
      await updateCandidateDecision(analysisId, decision);
      const updated = [...candidates];
      updated[idx] = { ...updated[idx], decision };
      setCandidates(updated);
    } catch (err) {
      alert('Failed to update decision');
    }
  };

  const filtered = candidates.filter(c => (c.matchScore || 0) >= minScore);

  const getScoreColor = (s) => {
    if (s >= 80) return '#10b981';
    if (s >= 60) return '#f59e0b';
    if (s >= 40) return '#f97316';
    return '#ef4444';
  };

  return (
    <div style={styles.page}>
      <Navbar />
      <div style={styles.orb} />

      <div style={styles.container}>

        {/* Header */}
        <div style={styles.header}>
          <div style={styles.badge}>🏢 INTERVIEWER PORTAL</div>
          <h1 style={styles.title}>
            Welcome, <span style={styles.neon}>{userName || 'Recruiter'}</span>
          </h1>
          <p style={styles.subtitle}>Upload multiple resumes and rank candidates automatically with AI</p>
        </div>

        {error && <div style={styles.errorBox}>⚠️ {error}</div>}

        {/* ════════════════════════════════
            UPLOAD VIEW
        ════════════════════════════════ */}
        {view === 'upload' && (
          <div style={styles.card}>
            <h2 style={styles.cardTitle}>📤 Bulk Resume Upload</h2>

            {/* File Upload */}
            <div style={styles.field}>
              <label style={styles.label}>SELECT RESUMES (PDF / DOCX)</label>
              <div style={styles.fileInput}>
                <input type="file" multiple accept=".pdf,.docx"
                  onChange={handleFileChange}
                  style={{ display: 'none' }} id="bulk-files" />
                <label htmlFor="bulk-files" style={styles.fileBtn}>
                  📁 Browse Files
                </label>
                <span style={{ color: '#94a3b8', fontSize: '0.9rem' }}>
                  {files.length > 0
                    ? `${files.length} file(s) selected`
                    : 'No files selected'}
                </span>
              </div>
              {files.length > 0 && (
                <div style={styles.fileList}>
                  {files.map((f, i) => (
                    <div key={i} style={styles.fileItem}>
                      📄 {f.name}
                      <span style={{ color: '#64748b', fontSize: '0.8rem' }}>
                        ({(f.size / 1024).toFixed(0)} KB)
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* Job Description */}
            <div style={styles.field}>
              <label style={styles.label}>JOB DESCRIPTION</label>
              <textarea
                style={styles.textarea}
                placeholder="Paste the job description here..."
                value={jd}
                onChange={(e) => setJd(e.target.value)}
                rows={8}
              />
            </div>

            <button onClick={handleUpload} style={styles.btn} disabled={loading}>
              {loading
                ? `🧠 Analyzing ${files.length} resumes...`
                : `🚀 Analyze ${files.length || ''} Resumes`}
            </button>

            {loading && (
              <div style={styles.loadingBox}>
                <div style={styles.spinner} />
                <p style={{ color: '#00f5ff', marginTop: '16px',
                  fontFamily: "'Orbitron', monospace", fontSize: '0.75rem' }}>
                  Processing and ranking all candidates...
                </p>
              </div>
            )}
          </div>
        )}

        {/* ════════════════════════════════
            RESULTS VIEW
        ════════════════════════════════ */}
        {view === 'results' && (
          <div>
            {/* Controls Row */}
            <div style={styles.controlsRow}>
              <div style={styles.statsRow}>
                <div style={styles.statBox}>
                  <span style={styles.statNum}>{candidates.length}</span>
                  <span style={styles.statLbl}>Total</span>
                </div>
                <div style={styles.statBox}>
                  <span style={{ ...styles.statNum, color: '#10b981' }}>
                    {candidates.filter(c => c.matchScore >= 70).length}
                  </span>
                  <span style={styles.statLbl}>Strong</span>
                </div>
                <div style={styles.statBox}>
                  <span style={{ ...styles.statNum, color: '#ef4444' }}>
                    {candidates.filter(c => c.decision === 'REJECTED').length}
                  </span>
                  <span style={styles.statLbl}>Rejected</span>
                </div>
                <div style={styles.statBox}>
                  <span style={{ ...styles.statNum, color: '#00f5ff' }}>
                    {candidates.filter(c => c.decision === 'ACCEPTED').length}
                  </span>
                  <span style={styles.statLbl}>Accepted</span>
                </div>
              </div>

              <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
                <label style={styles.label}>MIN SCORE: {minScore}%</label>
                <input type="range" min="0" max="100" step="5"
                  value={minScore}
                  onChange={(e) => setMinScore(Number(e.target.value))}
                  style={{ accentColor: '#00f5ff', width: '120px' }}
                />
                <button onClick={() => { setView('upload'); setFiles([]); setCandidates([]); setJd(''); }}
                  style={styles.btnOutline}>
                  🔄 New Upload
                </button>
              </div>
            </div>

            {/* Candidate Cards */}
            <div style={styles.candidateGrid}>
              {filtered.map((c, i) => (
                <div key={i} style={{
                  ...styles.candidateCard,
                  ...(selected === i ? styles.candidateCardSelected : {})
                }}
                  onClick={() => setSelected(selected === i ? null : i)}>

                  {/* Rank Badge */}
                  <div style={styles.rankBadge}>#{i + 1}</div>

                  {/* Score Circle */}
                  <div style={{ ...styles.scoreCircle,
                    borderColor: getScoreColor(c.matchScore),
                    color: getScoreColor(c.matchScore),
                    boxShadow: `0 0 15px ${getScoreColor(c.matchScore)}40`,
                  }}>
                    {Math.round(c.matchScore)}%
                  </div>

                  {/* File Name */}
                  <div style={styles.candidateName}>
                    📄 {c.fileName}
                  </div>

                  {/* Skills Tags */}
                  {c.keywordsMatched && (
                    <div style={styles.skillTags}>
                      {c.keywordsMatched.split(',').slice(0, 4).map((s, j) => (
                        <span key={j} style={styles.skillTag}>{s.trim()}</span>
                      ))}
                    </div>
                  )}

                  {/* Action Buttons */}
                  <div style={styles.actionRow}>
                    <button
                      onClick={(e) => { e.stopPropagation(); handleDecision(c.analysisId, 'ACCEPTED', i); }}
                      style={{
                        ...styles.actionBtn,
                        background: c.decision === 'ACCEPTED'
                          ? 'rgba(16,185,129,0.3)' : 'rgba(16,185,129,0.1)',
                        borderColor: '#10b981', color: '#10b981'
                      }}>
                      ✓ Accept
                    </button>
                    <button
                      onClick={(e) => { e.stopPropagation(); handleDecision(c.analysisId, 'REJECTED', i); }}
                      style={{
                        ...styles.actionBtn,
                        background: c.decision === 'REJECTED'
                          ? 'rgba(239,68,68,0.3)' : 'rgba(239,68,68,0.1)',
                        borderColor: '#ef4444', color: '#ef4444'
                      }}>
                      ✗ Reject
                    </button>
                  </div>

                  {/* Decision Badge */}
                  {c.decision && c.decision !== 'PENDING' && (
                    <div style={{
                      ...styles.decisionBadge,
                      background: c.decision === 'ACCEPTED'
                        ? 'rgba(16,185,129,0.15)' : 'rgba(239,68,68,0.15)',
                      color: c.decision === 'ACCEPTED' ? '#10b981' : '#ef4444',
                      borderColor: c.decision === 'ACCEPTED' ? '#10b981' : '#ef4444',
                    }}>
                      {c.decision === 'ACCEPTED' ? '✓ ACCEPTED' : '✗ REJECTED'}
                    </div>
                  )}
                </div>
              ))}
            </div>

            {filtered.length === 0 && (
              <div style={styles.emptyState}>
                <p>No candidates match the minimum score of {minScore}%</p>
                <p style={{ color: '#64748b', fontSize: '0.85rem', marginTop: '8px' }}>
                  Try lowering the minimum score filter
                </p>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}

const styles = {
  page: {
    minHeight: '100vh', background: '#050510',
    fontFamily: "'Rajdhani', sans-serif", position: 'relative',
  },
  orb: {
    position: 'fixed', top: '10%', left: '5%',
    width: '400px', height: '400px',
    background: 'radial-gradient(circle, rgba(168,85,247,0.06) 0%, transparent 70%)',
    borderRadius: '50%', pointerEvents: 'none',
  },
  container: {
    position: 'relative', zIndex: 2,
    maxWidth: '1100px', margin: '0 auto',
    padding: '120px 24px 60px',
  },
  header: { textAlign: 'center', marginBottom: '44px' },
  badge: {
    display: 'inline-block', padding: '6px 18px',
    background: 'rgba(168,85,247,0.1)',
    border: '1px solid rgba(168,85,247,0.3)',
    borderRadius: '20px', color: '#a855f7',
    fontFamily: "'Orbitron', monospace",
    fontSize: '0.65rem', letterSpacing: '2px', marginBottom: '18px',
  },
  title: {
    fontFamily: "'Orbitron', monospace",
    fontSize: '2rem', fontWeight: '900', marginBottom: '12px',
  },
  neon: {
    color: '#00f5ff',
    textShadow: '0 0 15px rgba(0,245,255,0.5)',
  },
  subtitle: { color: '#64748b', fontSize: '1rem' },
  errorBox: {
    background: 'rgba(239,68,68,0.1)',
    border: '1px solid rgba(239,68,68,0.3)',
    borderRadius: '12px', padding: '14px 20px',
    color: '#ef4444', marginBottom: '24px',
  },
  card: {
    background: 'rgba(255,255,255,0.03)',
    border: '1px solid rgba(168,85,247,0.15)',
    borderRadius: '20px', padding: '40px',
  },
  cardTitle: {
    fontFamily: "'Orbitron', monospace",
    fontSize: '1.1rem', fontWeight: '700',
    color: 'white', marginBottom: '28px',
  },
  field: { marginBottom: '24px' },
  label: {
    display: 'block',
    fontFamily: "'Orbitron', monospace",
    fontSize: '0.65rem', color: '#a855f7',
    letterSpacing: '2px', marginBottom: '10px',
  },
  fileInput: { display: 'flex', alignItems: 'center', gap: '16px' },
  fileBtn: {
    padding: '10px 20px',
    background: 'rgba(168,85,247,0.1)',
    border: '1px solid rgba(168,85,247,0.3)',
    borderRadius: '8px', color: '#a855f7',
    cursor: 'pointer', fontFamily: "'Orbitron', monospace",
    fontSize: '0.7rem', letterSpacing: '1px',
  },
  fileList: {
    marginTop: '12px', display: 'flex',
    flexDirection: 'column', gap: '6px',
  },
  fileItem: {
    padding: '8px 14px',
    background: 'rgba(255,255,255,0.03)',
    border: '1px solid rgba(255,255,255,0.06)',
    borderRadius: '8px', color: '#94a3b8',
    fontSize: '0.85rem',
    display: 'flex', justifyContent: 'space-between',
  },
  textarea: {
    width: '100%', padding: '16px',
    background: 'rgba(255,255,255,0.04)',
    border: '1px solid rgba(255,255,255,0.1)',
    borderRadius: '12px', color: 'white',
    fontFamily: "'Rajdhani', sans-serif",
    fontSize: '0.95rem', lineHeight: 1.7,
    outline: 'none', resize: 'vertical',
  },
  btn: {
    width: '100%', padding: '15px',
    background: 'linear-gradient(135deg, #a855f7, #6366f1)',
    border: 'none', borderRadius: '12px',
    color: 'white', fontFamily: "'Orbitron', monospace",
    fontSize: '0.8rem', fontWeight: '700',
    cursor: 'pointer', letterSpacing: '2px',
  },
  btnOutline: {
    padding: '10px 20px',
    background: 'transparent',
    border: '1px solid rgba(255,255,255,0.15)',
    borderRadius: '10px', color: '#94a3b8',
    fontFamily: "'Orbitron', monospace",
    fontSize: '0.65rem', cursor: 'pointer', letterSpacing: '1px',
  },
  loadingBox: { textAlign: 'center', padding: '30px', marginTop: '20px' },
  spinner: {
    width: '44px', height: '44px',
    border: '3px solid rgba(168,85,247,0.1)',
    borderTop: '3px solid #a855f7',
    borderRadius: '50%',
    animation: 'spin 1s linear infinite', margin: '0 auto',
  },
  controlsRow: {
    display: 'flex', justifyContent: 'space-between',
    alignItems: 'center', flexWrap: 'wrap',
    gap: '20px', marginBottom: '28px',
  },
  statsRow: { display: 'flex', gap: '16px' },
  statBox: {
    padding: '14px 20px',
    background: 'rgba(255,255,255,0.03)',
    border: '1px solid rgba(255,255,255,0.06)',
    borderRadius: '12px',
    display: 'flex', flexDirection: 'column', alignItems: 'center',
  },
  statNum: {
    fontFamily: "'Orbitron', monospace",
    fontSize: '1.4rem', fontWeight: '900', color: 'white',
  },
  statLbl: { color: '#64748b', fontSize: '0.75rem', marginTop: '2px' },
  candidateGrid: {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))',
    gap: '20px',
  },
  candidateCard: {
    padding: '28px 24px',
    background: 'rgba(255,255,255,0.03)',
    border: '1px solid rgba(255,255,255,0.07)',
    borderRadius: '16px', cursor: 'pointer',
    transition: 'all 0.3s', position: 'relative',
    display: 'flex', flexDirection: 'column', gap: '14px',
  },
  candidateCardSelected: {
    border: '1px solid rgba(0,245,255,0.3)',
    background: 'rgba(0,245,255,0.04)',
  },
  rankBadge: {
    position: 'absolute', top: '16px', right: '16px',
    fontFamily: "'Orbitron', monospace",
    fontSize: '0.7rem', color: '#475569',
  },
  scoreCircle: {
    width: '70px', height: '70px',
    borderRadius: '50%', border: '3px solid',
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    fontFamily: "'Orbitron', monospace",
    fontSize: '0.9rem', fontWeight: '900',
  },
  candidateName: {
    fontFamily: "'Share Tech Mono', monospace",
    fontSize: '0.82rem', color: '#94a3b8',
    wordBreak: 'break-all',
  },
  skillTags: { display: 'flex', flexWrap: 'wrap', gap: '6px' },
  skillTag: {
    padding: '3px 10px',
    background: 'rgba(255,255,255,0.05)',
    border: '1px solid rgba(255,255,255,0.1)',
    borderRadius: '12px', color: '#94a3b8',
    fontSize: '0.75rem',
  },
  actionRow: { display: 'flex', gap: '8px' },
  actionBtn: {
    flex: 1, padding: '8px',
    border: '1px solid', borderRadius: '8px',
    fontFamily: "'Orbitron', monospace",
    fontSize: '0.6rem', fontWeight: '700',
    cursor: 'pointer', letterSpacing: '1px',
    transition: 'all 0.2s',
  },
  decisionBadge: {
    textAlign: 'center', padding: '6px',
    border: '1px solid', borderRadius: '8px',
    fontFamily: "'Orbitron', monospace",
    fontSize: '0.65rem', fontWeight: '700', letterSpacing: '2px',
  },
  emptyState: {
    textAlign: 'center', padding: '60px',
    color: '#94a3b8', fontSize: '1rem',
  },
};
