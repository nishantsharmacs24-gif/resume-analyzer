// src/services/api.js
// All API calls to Spring Boot backend are defined here
// This way, if backend URL changes, we only update it in ONE place

import axios from 'axios';

// Base URL for Spring Boot backend
const API = axios.create({
  baseURL:'https://resume-analyzer-production-b28d.up.railway.app/api'
});

// Automatically attach JWT token to every request
// So logged-in users don't need to manually add token each time
API.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// ─────────────────────────────────────────
// AUTH APIs
// ─────────────────────────────────────────

export const registerUser = (data) =>
  API.post('/auth/register', data);

export const loginUser = (data) =>
  API.post('/auth/login', data);

export const getUserProfile = (userId) =>
  API.get(`/auth/user/${userId}`);

// ─────────────────────────────────────────
// RESUME APIs
// ─────────────────────────────────────────

export const uploadResume = (file, userId) => {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('userId', userId);
  return API.post('/resume/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
};

export const analyzeResume = (resumeId, jobDescription) =>
  API.post('/resume/analyze', { resumeId, jobDescription });

export const getUserResumes = (userId) =>
  API.get(`/resume/user/${userId}`);

export const downloadResume = (resumeId) =>
  API.get(`/resume/download/${resumeId}`, { responseType: 'blob' });

// ─────────────────────────────────────────
// INTERVIEWER APIs
// ─────────────────────────────────────────

export const bulkUploadResumes = (files, jobDescription, userId) => {
  const formData = new FormData();
  files.forEach(file => formData.append('files', file));
  formData.append('jobDescription', jobDescription);
  formData.append('userId', userId);
  return API.post('/interviewer/bulk-upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
};

export const getInterviewerDashboard = () =>
  API.get('/interviewer/dashboard');

export const updateCandidateDecision = (analysisId, decision) =>
  API.put('/interviewer/decision', { analysisId, decision });

export const getTopCandidates = (minScore = 60) =>
  API.get(`/interviewer/top-candidates?minScore=${minScore}`);

// ─────────────────────────────────────────
// LOCAL STORAGE HELPERS
// ─────────────────────────────────────────

export const saveAuthData = (data) => {
  localStorage.setItem('token', data.token);
  localStorage.setItem('userId', data.userId);
  localStorage.setItem('userName', data.name);
  localStorage.setItem('userRole', data.role);
};

export const getAuthData = () => ({
  token: localStorage.getItem('token'),
  userId: localStorage.getItem('userId'),
  userName: localStorage.getItem('userName'),
  userRole: localStorage.getItem('userRole'),
});

export const clearAuthData = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('userId');
  localStorage.removeItem('userName');
  localStorage.removeItem('userRole');
};

export const isLoggedIn = () => !!localStorage.getItem('token');
