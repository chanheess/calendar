import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';

const root = ReactDOM.createRoot(document.getElementById('root'));

root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);

const API_BASE = process.env.REACT_APP_DOMAIN || window.location.origin;

function postApiBaseToSW(registration) {
  const target = registration?.active || navigator.serviceWorker.controller;
  if (target) {
    try {
      target.postMessage({ type: 'SET_API_BASE', value: API_BASE });
    } catch (e) {
    }
  }
}

if ('serviceWorker' in navigator) {
  window.addEventListener('load', () => {
    navigator.serviceWorker
      .register('/firebase-messaging-sw.js')
      .then((registration) => {
        postApiBaseToSW(registration);
        navigator.serviceWorker.ready.then((reg) => {
          postApiBaseToSW(reg);
        });

        navigator.serviceWorker.addEventListener('controllerchange', () => {
          navigator.serviceWorker.ready.then((reg) => {
            postApiBaseToSW(reg);
          });
        });

        if (registration.updatefound) {
          const sw = registration.installing;
          sw && sw.addEventListener('statechange', () => {
            if (sw.state === 'activated') {
              postApiBaseToSW(registration);
            }
          });
        }
      })
      .catch(() => {
      });
  });
}

reportWebVitals();