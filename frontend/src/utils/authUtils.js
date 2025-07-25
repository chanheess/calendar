import axios from './axiosInstance';
import { getFirebaseToken } from "../components/FirebaseToken";

export const checkLoginStatus = async () => {
  try {
    const token = await getFirebaseToken();
    let response;
    
    if (token) {
      response = await axios.get(`/auth/check/${token}`, { withCredentials: true });
    } else {
      response = await axios.get(`/auth/check`, { withCredentials: true });
    }

    return response.data;
  } catch (error) {
    console.error("로그인 상태를 확인할 수 없습니다.", error);
    return false;
  }
};

export const getRedirectPath = () => {
  const redirectPath = sessionStorage.getItem('redirectPath');
  return redirectPath || '/';
};

export const setRedirectPath = (path) => {
  sessionStorage.setItem('redirectPath', path);
};

export const clearRedirectPath = () => {
  sessionStorage.removeItem('redirectPath');
}; 