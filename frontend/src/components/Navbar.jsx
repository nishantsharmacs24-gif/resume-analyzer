// src/components/Navbar.jsx
import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { clearAuthData, getAuthData, isLoggedIn } from '../services/api';

export default function Navbar() {
  const navigate = useNavigate();
  const { userName, userRole } = getAuthData();
  const loggedIn = isLoggedIn();
  const [menuOpen, setMenuOpen] = useState(false);

  const handleLogout = () => {
    clearAuthData();
    navigate('/');
  };

  return (
    <nav style={styles.nav}>
      {/* Logo */}
      <div style={styles.logo} onClick={() => navigate('/')}>
        <span style={styles.logoIcon}>⬡</span>
        <span style={styles.logoText}>RESUME<span style={styles.logoAccent}>AI</span></span>
      </div>

      {/* Nav Links */}
      <div style={styles.links}>
        <Link to="/" style={styles.link}>Home</Link>
        <Link to="/#how-it-works" style={styles.link}>How It Works</Link>
        <Link to="/#gallery" style={styles.link}>Gallery</Link>
        <Link to="/#contact" style={styles.link}>Contact</Link>
      </div>

      {/* Auth Buttons */}
      <div style={styles.authArea}>
        {loggedIn ? (
          <>
            <span style={styles.userBadge}>
              {userRole === 'INTERVIEWER' ? '🏢' : '👤'} {userName}
            </span>
            <button
              onClick={() => navigate(userRole === 'INTERVIEWER' ? '/interviewer' : '/candidate')}
              style={styles.btnPrimary}>
              Dashboard
            </button>
            <button onClick={handleLogout} style={styles.btnOutline}>
              Logout
            </button>
          </>
        ) : (
          <>
            <button onClick={() => navigate('/login')} style={styles.btnOutline}>
              Sign In
            </button>
            <button onClick={() => navigate('/register')} style={styles.btnPrimary}>
              Sign Up
            </button>
          </>
        )}
      </div>
    </nav>
  );
}

const styles = {
  nav: {
    position: 'fixed', top: 0, left: 0, right: 0,
    display: 'flex', alignItems: 'center', justifyContent: 'space-between',
    padding: '16px 60px',
    background: 'rgba(5, 5, 16, 0.8)',
    backdropFilter: 'blur(20px)',
    borderBottom: '1px solid rgba(0, 245, 255, 0.1)',
    zIndex: 1000,
  },
  logo: {
    display: 'flex', alignItems: 'center', gap: '10px',
    cursor: 'pointer',
  },
  logoIcon: { fontSize: '1.8rem', color: '#00f5ff', lineHeight: 1 },
  logoText: {
    fontFamily: "'Orbitron', monospace",
    fontSize: '1.2rem', fontWeight: '900',
    color: 'white', letterSpacing: '2px',
  },
  logoAccent: { color: '#00f5ff' },
  links: { display: 'flex', gap: '32px' },
  link: {
    color: '#94a3b8', textDecoration: 'none',
    fontFamily: "'Rajdhani', sans-serif",
    fontSize: '1rem', fontWeight: '600',
    transition: 'color 0.2s',
    letterSpacing: '1px',
  },
  authArea: { display: 'flex', alignItems: 'center', gap: '12px' },
  userBadge: {
    color: '#00f5ff', fontSize: '0.9rem',
    fontFamily: "'Rajdhani', sans-serif", fontWeight: '600',
    padding: '6px 14px',
    background: 'rgba(0,245,255,0.08)',
    borderRadius: '20px',
    border: '1px solid rgba(0,245,255,0.2)',
  },
  btnOutline: {
    padding: '8px 20px',
    background: 'transparent',
    border: '1px solid #00f5ff',
    borderRadius: '8px', color: '#00f5ff',
    fontFamily: "'Orbitron', monospace",
    fontSize: '0.7rem', fontWeight: '600',
    cursor: 'pointer', letterSpacing: '1px',
    transition: 'all 0.2s',
  },
  btnPrimary: {
    padding: '8px 20px',
    background: 'linear-gradient(135deg, #00f5ff, #3b82f6)',
    border: 'none', borderRadius: '8px',
    color: '#000',
    fontFamily: "'Orbitron', monospace",
    fontSize: '0.7rem', fontWeight: '700',
    cursor: 'pointer', letterSpacing: '1px',
  },
};
