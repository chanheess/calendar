import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';
import axios from 'utils/axiosInstance';

const root = ReactDOM.createRoot(document.getElementById('root'));

axios.get('/auth/check')
  .then(() => {
    root.render(
      <React.StrictMode>
        <App />
      </React.StrictMode>
    );
  })
  .catch(() => {
    window.location.href = "/auth/login";
  });

reportWebVitals();