// src/App.jsx
// This is the main routing file
// It tells React which page to show for which URL

import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import '../src/styles/global.css';
import Home from './pages/Home';
import Login from './pages/Login';
import Register from './pages/Register';
import CandidatePortal from './pages/CandidatePortal';
import InterviewerPortal from './pages/InterviewerPortal';
import { isLoggedIn, getAuthData } from './services/api';

// Protected Route: Only allow access if user is logged in
function ProtectedRoute({ children, requiredRole }) {
  const loggedIn = isLoggedIn();
  const { userRole } = getAuthData();

  if (!loggedIn) {
    return <Navigate to="/login" replace />;
  }

  if (requiredRole && userRole !== requiredRole) {
    return <Navigate to="/" replace />;
  }

  return children;
}

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public Routes */}
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />

        {/* Protected: Candidate only */}
        <Route
          path="/candidate"
          element={
            <ProtectedRoute requiredRole="CANDIDATE">
              <CandidatePortal />
            </ProtectedRoute>
          }
        />

        {/* Protected: Interviewer only */}
        <Route
          path="/interviewer"
          element={
            <ProtectedRoute requiredRole="INTERVIEWER">
              <InterviewerPortal />
            </ProtectedRoute>
          }
        />

        {/* Fallback: redirect unknown routes to home */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
