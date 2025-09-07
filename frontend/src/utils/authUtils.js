import axios from './axiosInstance';
import { getFirebaseToken } from "../components/FirebaseToken";

export const checkLoginStatus = async () => {
  try {
    const token = await getFirebaseToken();
    if (token) {
      await axios.get(`/auth/check`, { params: { firebaseToken: token } });
    } else {
      await axios.get(`/auth/check`);
    }
    return true;
  } catch (error) {
    // 401이면 인터셉터가 먼저 refresh 시도
    // refresh도 실패하면 최종적으로 여기서 잡힘
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