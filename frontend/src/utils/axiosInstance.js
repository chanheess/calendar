import axios from 'axios';
import { setRedirectPath } from './authUtils';

let refreshPromise = null;
let requestQueue = [];

axios.interceptors.response.use(
  response => response,
  async error => {
    const originalRequest = error.config;

    if (
      error.response?.status === 401 &&
      !originalRequest._retry
    ) {
      if (!refreshPromise) {
        refreshPromise = axios.post('/auth/refresh', null, { withCredentials: true })
          .then(() => {
            // 모든 대기 중인 요청 재처리
            requestQueue.forEach(cb => cb());
            requestQueue = [];
          })
          .catch(err => {
            requestQueue = [];
            
            // 에러 타입에 따른 처리
            if (err.response?.status === 401) {
              // 인증 실패 - 리프레시 토큰도 만료됨
              console.warn('Refresh token expired, redirecting to login');
              setRedirectPath(window.location.pathname);
              window.location.href = "/auth/login";
            } else if (err.response?.status >= 500) {
              // 서버 에러 - 잠시 후 재시도
              console.warn('Server error during token refresh, will retry on next request');
            } else {
              // 기타 에러 (네트워크 등)
              console.warn('Network error during token refresh, redirecting to login');
              setRedirectPath(window.location.pathname);
              window.location.href = "/auth/login";
            }
            throw err;
          })
          .finally(() => {
            refreshPromise = null;
          });
      }

      // 새 Promise를 만들어서 큐에 등록
      return new Promise((resolve, reject) => {
        requestQueue.push(() => {
          originalRequest._retry = true;
          // withCredentials 옵션을 반드시 true로 설정
          originalRequest.withCredentials = true;
          axios(originalRequest).then(resolve).catch(reject);
        });
      });
    }

    return Promise.reject(error);
  }
);

export default axios;