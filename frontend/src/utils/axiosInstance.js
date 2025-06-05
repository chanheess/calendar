import axios from 'axios';

let refreshPromise = null;
let requestQueue = [];

axios.interceptors.response.use(
  response => response,
  async error => {
    const originalRequest = error.config;

    if (
      error.response?.status === 401 &&
      error.response.data === "jwtToken expired" &&
      !originalRequest._retry
    ) {
      if (!refreshPromise) {
        refreshPromise = axios.post('/auth/refresh', null, { withCredentials: true })
          .then(res => {
            const newToken = res.data.accessToken;
            axios.defaults.headers.common['Authorization'] = 'Bearer ' + newToken;

            // 모든 대기 중인 요청 재처리
            requestQueue.forEach(cb => cb(newToken));
            requestQueue = [];

            return newToken;
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

      // 새 Promise를 만들어서 큐에 등록
      return new Promise((resolve, reject) => {
        requestQueue.push(token => {
          originalRequest._retry = true;
          originalRequest.headers['Authorization'] = 'Bearer ' + token;
          axios(originalRequest).then(resolve).catch(reject);
        });
      });
    }

    return Promise.reject(error);
  }
);

export default axios;