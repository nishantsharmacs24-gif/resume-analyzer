// src/index.js
// This is the entry point of the React app
// It renders <App /> into the div with id="root" in public/index.html

import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
