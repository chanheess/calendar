import axios from 'axios';

let refreshPromise = null;
let requestQueue = [];

axios.interceptors.response.use(
  response => response,
  async error => {
    const originalRequest = error.config;

    // CountAuthenticationException 등 인증 횟수 초과 메시지 예외 처리
    const isCountAuthError =
      error.response?.status === 401 &&
      typeof error.response?.data?.message === "string" &&
      error.response.data.message.includes("인증 요청") &&
      error.response.data.message.includes("초과");

    // 401이고, 재시도한 적 없고, 인증 횟수 초과 에러가 아닐 때만 토큰 갱신 시도
    if (
      error.response?.status === 401 &&
      !originalRequest._retry &&
      !isCountAuthError
    ) {
      if (!refreshPromise) {
        refreshPromise = axios.post('/auth/refresh', null, { withCredentials: true })
          .then(() => {
            requestQueue.forEach(cb => cb());
            requestQueue = [];
          })
          .catch(err => {
            requestQueue = [];
            window.location.href = "/auth/login";
            throw err;
          })
          .finally(() => {
            refreshPromise = null;
          });
      }

      return new Promise((resolve, reject) => {
        requestQueue.push(() => {
          originalRequest._retry = true;
          axios(originalRequest).then(resolve).catch(reject);
        });
      });
    }

    return Promise.reject(error);
  }
);

export default axios;