import axios from 'axios';

axios.interceptors.response.use(
  response => response,
  async error => {
    const originalRequest = error.config;

    if (error.response?.status === 401 && error.response.data === "jwtToken expired" && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const refreshResponse = await axios.post('/auth/refresh');
        const newAccessToken = refreshResponse.data.accessToken;

        axios.defaults.headers.common['Authorization'] = 'Bearer ' + newAccessToken;
        originalRequest.headers['Authorization'] = 'Bearer ' + newAccessToken;

        return axios(originalRequest);
      } catch (refreshError) {
        window.location.href = "/auth/login";
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default axios;