import axios from 'axios';

const API = axios.create({
  baseURL: 'https://backend-production-b383.up.railway.app/api'
});

API.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default API;
