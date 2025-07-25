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
            // 현재 경로를 저장하고 로그인 페이지로 리다이렉트
            setRedirectPath(window.location.pathname);
            window.location.href = "/auth/login";
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