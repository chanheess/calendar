import React from 'react';
import ReactDOM from 'react-dom/client';
import './index.css';
import App from './App';
import reportWebVitals from './reportWebVitals';
import axios from "axios";

axios.defaults.baseURL = process.env.REACT_APP_API_URL;
axios.defaults.headers.common["Content-Type"] = "application/json";
axios.defaults.withCredentials = true; // 쿠키 포함
axios.interceptors.response.use(
  response => response,
  async error => {
      const originalRequest = error.config;

      if (error.response && error.response.status === 401) {
          if (error.response.data && error.response.data === "jwtToken expired" && !originalRequest._retry) {
              originalRequest._retry = true;
              try {
                  const refreshResponse = await axios.post('/auth/refresh');
                  if (refreshResponse.status === 200) {
                      const newAccessToken = refreshResponse.data.accessToken;
                      axios.defaults.headers.common['Authorization'] = 'Bearer ' + newAccessToken;
                      originalRequest.headers['Authorization'] = 'Bearer ' + newAccessToken;
                      return axios(originalRequest);
                  }
              } catch (refreshError) {
                  // 리프레시 토큰 처리 실패 시 로그인 페이지로 리다이렉트
                  window.location.href = "/auth/login";
                  return Promise.reject(refreshError);
              }
          } else {
              // 그 외 모든 401 에러는 로그인 페이지로 리다이렉트
              window.location.href = "/auth/login";
          }
      }
      return Promise.reject(error);
  }
);


const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
// If you want to start measuring performance in your app, pass a function
// to log results (for example: reportWebVitals(console.log))
// or send to an analytics endpoint. Learn more: https://bit.ly/CRA-vitals
reportWebVitals();
