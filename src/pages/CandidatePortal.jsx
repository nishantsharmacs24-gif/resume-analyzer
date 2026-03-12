// src/pages/CandidatePortal.jsx
import { useState, useRef } from 'react';
import Navbar from '../components/Navbar';
import ScoreCard from '../components/ScoreCard';
import { uploadResume, analyzeResume, getAuthData } from '../services/api';

export default function CandidatePortal() {
  const { userId, userName } = getAuthData();
  const [file, setFile] = useState(null);
  const [jd, setJd] = useState('');
  const [step, setStep] = useState('upload'); // upload | analyze | results
  const [resumeId, setResumeId] = useState(null);
  const [result, setResult] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const fileRef = useRef();

  // ── Handle File Drop ──
  const handleDrop = (e) => {
    e.preventDefault();
    const dropped = e.dataTransfer.files[0];
    if (dropped) setFile(dropped);
  };

  const handleDragOver = (e) => e.preventDefault();

  // ── Upload Resume ──
  const handleUpload = async () => {
    if (!file) return setError('Please select a resume file first!');
    setLoading(true); setError('');
    try {
      const res = await uploadResume(file, userId);
      setResumeId(res.data.resumeId);
      setStep('analyze');
    } catch (err) {
      setError('Upload failed. Make sure the backend is running!');
    } finally {
      setLoading(false);
    }
  };

  // ── Analyze Resume ──
  const handleAnalyze = async () => {
    if (!jd.trim()) return setError('Please paste the job description!');
    setLoading(true); setError('');
    try {
      const res = await analyzeResume(resumeId, jd);
      setResult(res.data);
      setStep('results');
    } catch (err) {
      setError('Analysis failed. Make sure the ML service is running!');
    } finally {
      setLoading(false);
    }
  };

  const reset = () => {
    setFile(null); setJd(''); setStep('upload');
    setResumeId(null); setResult(null); setError('');
  };

  return (
    <div style={styles.page}>
      <Navbar />
      <div style={styles.orb} />

      <div style={styles.container}>

        {/* ── Page Header ── */}
        <div style={styles.header}>
          <div style={styles.badge}>👤 CANDIDATE PORTAL</div>
          <h1 style={styles.title}>
            Hey <span style={styles.neon}>{userName || 'there'}</span>,<br />
            Let's analyze your resume
          </h1>
          <p style={styles.subtitle}>Upload your resume and paste a job description to get AI-powered insights</p>
        </div>

        {/* ── Step Indicator ── */}
        <div style={styles.stepIndicator}>
          {['Upload Resume', 'Add Job Description', 'View Results'].map((label, i) => {
            const stepKeys = ['upload', 'analyze', 'results'];
            const isActive = stepKeys.indexOf(step) >= i;
            return (
              <div key={i} style={styles.stepItem}>
                <div style={{ ...styles.stepCircle, ...(isActive ? styles.stepCircleActive : {}) }}>
                  {i + 1}
                </div>
                <span style={{ ...styles.stepLabel, ...(isActive ? { color: '#00f5ff' } : {}) }}>
                  {label}
                </span>
                {i < 2 && <div style={{ ...styles.stepLine, ...(isActive ? styles.stepLineActive : {}) }} />}
              </div>
            );
          })}
        </div>

        {/* ── Error Message ── */}
        {error && (
          <div style={styles.errorBox}>⚠️ {error}</div>
        )}

        {/* ════════════════════════════════════
            STEP 1: UPLOAD RESUME
        ════════════════════════════════════ */}
        {step === 'upload' && (
          <div style={styles.card}>
            <h2 style={styles.cardTitle}>📤 Upload Your Resume</h2>
            <p style={styles.cardDesc}>Supported formats: PDF, DOCX (max 15MB)</p>

            {/* Drag & Drop Zone */}
            <div
              style={{ ...styles.dropZone, ...(file ? styles.dropZoneActive : {}) }}
              onDrop={handleDrop}
              onDragOver={handleDragOver}
              onClick={() => fileRef.current.click()}>
              <input ref={fileRef} type="file" accept=".pdf,.docx"
                style={{ display: 'none' }}
                onChange={(e) => setFile(e.target.files[0])} />
              {file ? (
                <div>
                  <div style={{ fontSize: '3rem', marginBottom: '10px' }}>✅</div>
                  <div style={styles.fileName}>{file.name}</div>
                  <div style={{ color: '#64748b', fontSize: '0.9rem' }}>
                    {(file.size / 1024 / 1024).toFixed(2)} MB
                  </div>
                </div>
              ) : (
                <div>
                  <div style={{ fontSize: '3.5rem', marginBottom: '16px' }}>📄</div>
                  <div style={styles.dropText}>Drag & drop your resume here</div>
                  <div style={{ color: '#64748b', fontSize: '0.9rem' }}>or click to browse files</div>
                </div>
              )}
            </div>

            <button onClick={handleUpload} style={styles.btn} disabled={loading || !file}>
              {loading ? '⏳ Uploading...' : '🚀 Upload Resume'}
            </button>
          </div>
        )}

        {/* ════════════════════════════════════
            STEP 2: JOB DESCRIPTION
        ════════════════════════════════════ */}
        {step === 'analyze' && (
          <div style={styles.card}>
            <h2 style={styles.cardTitle}>📋 Paste Job Description</h2>
            <p style={styles.cardDesc}>
              ✅ Resume uploaded: <strong style={{ color: '#10b981' }}>{file?.name}</strong>
            </p>
            <p style={{ ...styles.cardDesc, marginTop: '6px' }}>
              Now paste the job description from the job posting you want to apply for:
            </p>

            <textarea
              style={styles.textarea}
              placeholder="Paste the full job description here...

Example:
We are looking for a Senior Java Developer with 3+ years of experience in Spring Boot, REST APIs, MySQL, and Docker. Knowledge of AWS and React is a plus..."
              value={jd}
              onChange={(e) => setJd(e.target.value)}
              rows={12}
            />

            <div style={{ display: 'flex', gap: '12px' }}>
              <button onClick={() => setStep('upload')} style={styles.btnOutline}>
                ← Back
              </button>
              <button onClick={handleAnalyze} style={styles.btn} disabled={loading || !jd.trim()}>
                {loading ? '🧠 Analyzing...' : '🤖 Analyze Now'}
              </button>
            </div>

            {loading && (
              <div style={styles.loadingBox}>
                <div style={styles.spinner} />
                <p style={{ color: '#00f5ff', marginTop: '16px', fontFamily: "'Orbitron', monospace", fontSize: '0.8rem' }}>
                  AI is analyzing your resume...
                </p>
                <p style={{ color: '#64748b', fontSize: '0.85rem', marginTop: '8px' }}>
                  Extracting keywords • Calculating match score • Generating feedback
                </p>
              </div>
            )}
          </div>
        )}

        {/* ════════════════════════════════════
            STEP 3: RESULTS
        ════════════════════════════════════ */}
        {step === 'results' && result && (
          <div>
            <div style={styles.resultHeader}>
              <h2 style={styles.cardTitle}>📊 Your Analysis Results</h2>
              <button onClick={reset} style={styles.btnOutline}>
                🔄 Analyze Another Resume
              </button>
            </div>
            <div style={styles.card}>
              <ScoreCard result={result} />
            </div>
          </div>
        )}

      </div>
    </div>
  );
}

const styles = {
  page: {
    minHeight: '100vh', background: '#050510',
    fontFamily: "'Rajdhani', sans-serif",
    position: 'relative', overflow: 'hidden',
  },
  orb: {
    position: 'fixed', top: '20%', right: '5%',
    width: '500px', height: '500px',
    background: 'radial-gradient(circle, rgba(0,245,255,0.05) 0%, transparent 70%)',
    borderRadius: '50%', pointerEvents: 'none',
  },
  container: {
    position: 'relative', zIndex: 2,
    maxWidth: '860px', margin: '0 auto',
    padding: '120px 24px 60px',
  },
  header: { textAlign: 'center', marginBottom: '50px' },
  badge: {
    display: 'inline-block', padding: '6px 18px',
    background: 'rgba(0,245,255,0.08)',
    border: '1px solid rgba(0,245,255,0.2)',
    borderRadius: '20px', color: '#00f5ff',
    fontFamily: "'Orbitron', monospace",
    fontSize: '0.65rem', letterSpacing: '2px',
    marginBottom: '20px',
  },
  title: {
    fontFamily: "'Orbitron', monospace",
    fontSize: '2.2rem', fontWeight: '900',
    lineHeight: 1.3, marginBottom: '14px',
  },
  neon: {
    color: '#00f5ff',
    textShadow: '0 0 15px rgba(0,245,255,0.5)',
  },
  subtitle: { color: '#64748b', fontSize: '1.05rem' },
  stepIndicator: {
    display: 'flex', alignItems: 'center',
    justifyContent: 'center', gap: '0', marginBottom: '40px',
  },
  stepItem: {
    display: 'flex', alignItems: 'center', gap: '8px',
  },
  stepCircle: {
    width: '36px', height: '36px',
    borderRadius: '50%',
    border: '2px solid rgba(255,255,255,0.15)',
    display: 'flex', alignItems: 'center', justifyContent: 'center',
    fontFamily: "'Orbitron', monospace",
    fontSize: '0.75rem', color: '#475569',
    transition: 'all 0.4s',
  },
  stepCircleActive: {
    border: '2px solid #00f5ff',
    background: 'rgba(0,245,255,0.15)',
    color: '#00f5ff',
    boxShadow: '0 0 12px rgba(0,245,255,0.3)',
  },
  stepLabel: {
    fontFamily: "'Rajdhani', sans-serif",
    fontSize: '0.85rem', color: '#475569',
    whiteSpace: 'nowrap',
  },
  stepLine: {
    width: '50px', height: '1px',
    background: 'rgba(255,255,255,0.1)',
    margin: '0 8px',
  },
  stepLineActive: { background: '#00f5ff' },
  errorBox: {
    background: 'rgba(239,68,68,0.1)',
    border: '1px solid rgba(239,68,68,0.3)',
    borderRadius: '12px', padding: '14px 20px',
    color: '#ef4444', marginBottom: '24px',
  },
  card: {
    background: 'rgba(255,255,255,0.03)',
    border: '1px solid rgba(0,245,255,0.1)',
    borderRadius: '20px', padding: '40px',
    backdropFilter: 'blur(10px)',
  },
  cardTitle: {
    fontFamily: "'Orbitron', monospace",
    fontSize: '1.2rem', fontWeight: '700',
    color: 'white', marginBottom: '10px',
  },
  cardDesc: { color: '#64748b', fontSize: '0.95rem', marginBottom: '28px' },
  dropZone: {
    border: '2px dashed rgba(255,255,255,0.15)',
    borderRadius: '16px', padding: '60px 40px',
    textAlign: 'center', cursor: 'pointer',
    transition: 'all 0.3s', marginBottom: '24px',
  },
  dropZoneActive: {
    border: '2px dashed #00f5ff',
    background: 'rgba(0,245,255,0.05)',
  },
  fileName: {
    fontFamily: "'Share Tech Mono', monospace",
    color: '#00f5ff', fontSize: '1rem', marginBottom: '8px',
  },
  dropText: {
    fontSize: '1.1rem', color: '#94a3b8',
    marginBottom: '8px', fontWeight: '600',
  },
  btn: {
    padding: '14px 32px', width: '100%',
    background: 'linear-gradient(135deg, #00f5ff, #3b82f6)',
    border: 'none', borderRadius: '12px',
    color: '#000', fontFamily: "'Orbitron', monospace",
    fontSize: '0.8rem', fontWeight: '700',
    cursor: 'pointer', letterSpacing: '2px',
    transition: 'all 0.3s',
  },
  btnOutline: {
    padding: '14px 24px',
    background: 'transparent',
    border: '1px solid rgba(255,255,255,0.2)',
    borderRadius: '12px', color: '#94a3b8',
    fontFamily: "'Orbitron', monospace",
    fontSize: '0.7rem', cursor: 'pointer',
  },
  textarea: {
    width: '100%', padding: '16px',
    background: 'rgba(255,255,255,0.05)',
    border: '1px solid rgba(255,255,255,0.1)',
    borderRadius: '12px', color: 'white',
    fontFamily: "'Rajdhani', sans-serif",
    fontSize: '0.95rem', lineHeight: 1.7,
    outline: 'none', resize: 'vertical',
    marginBottom: '24px',
  },
  loadingBox: {
    textAlign: 'center', padding: '30px',
    marginTop: '20px',
  },
  spinner: {
    width: '44px', height: '44px',
    border: '3px solid rgba(0,245,255,0.1)',
    borderTop: '3px solid #00f5ff',
    borderRadius: '50%',
    animation: 'spin 1s linear infinite',
    margin: '0 auto',
  },
  resultHeader: {
    display: 'flex', justifyContent: 'space-between',
    alignItems: 'center', marginBottom: '24px',
  },
};
