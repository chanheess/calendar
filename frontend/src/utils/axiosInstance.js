import axios from 'axios';

let refreshPromise = null;
let requestQueue = [];

axios.interceptors.response.use(
  response => response,
  async error => {
    const originalRequest = error.config;

    // 401이고, 재시도한 적 없으면
    if (
      error.response?.status === 401 &&
      !originalRequest._retry
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