import Axios from 'axios';
import { setRedirectPath } from './authUtils';

const API_BASE = process.env.REACT_APP_API_URL || '';

const axios = Axios.create({
  baseURL: API_BASE,
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' },
});

let refreshPromise = null;
let requestQueue = [];

// 우리 API로 향한 요청인지 판별 (GA/CDN 등 외부 실패는 스킵)
function isApiRequest(cfg) {
  try {
    const apiBase = new URL(API_BASE || '/', window.location.origin);
    const reqUrl = new URL(cfg?.url ?? '', apiBase.origin);
    return reqUrl.origin === apiBase.origin;
  } catch {
    return true;
  }
}

axios.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config || {};

    // 응답이 없으면(네트워크 오류 등) 리프레시 로직 태우지 말고 그대로 넘김
    if (!error.response) return Promise.reject(error);

    // 외부 도메인(예: google-analytics) 실패는 건드리지 않음
    if (!isApiRequest(originalRequest)) return Promise.reject(error);

    // refresh 요청 자체의 401은 가로채지 않음(무한루프 방지)
    const urlStr = originalRequest.url || '';
    if (urlStr.includes('/auth/refresh')) return Promise.reject(error);

    if (error.response.status === 401 && !originalRequest._retry) {
      if (!refreshPromise) {
        // 같은 인스턴스로 refresh 호출 (쿠키/기본설정 유지)
        refreshPromise = axios.post('/auth/refresh')
          .then(() => {
            requestQueue.forEach((cb) => cb());
            requestQueue = [];
          })
          .catch((err) => {
            requestQueue = [];
            if (err.response?.status === 401) {
              // 진짜 만료
              setRedirectPath(window.location.pathname);
              window.location.href = '/auth/login';
            }
            throw err;
          })
          .finally(() => { refreshPromise = null; });
      }

      // refresh 끝난 뒤 원 요청 재시도
      return new Promise((resolve, reject) => {
        requestQueue.push(() => {
          originalRequest._retry = true;
          originalRequest.withCredentials = true; // 안전하게 보장
          axios(originalRequest).then(resolve).catch(reject); // 같은 인스턴스로 재요청
        });
      });
    }

    return Promise.reject(error);
  }
);

export default axios;