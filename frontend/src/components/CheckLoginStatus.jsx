import { useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import axios from 'utils/axiosInstance';
import { getFirebaseToken } from "components/FirebaseToken";


const CheckLoginStatus = ({ children }) => {
  const navigate = useNavigate();

  const checkLoginStatus = useCallback(async () => {
    const token = await getFirebaseToken();

    try {
      let response;
      if (token) {
        response = await axios.get(`/auth/check/${token}`, { withCredentials: true });
      } else {
        response = await axios.get(`/auth/check`, { withCredentials: true });
      }

      if (!response.data) {
        throw new Error("Invalid login status");
      }
    } catch (error) {
      console.error("로그인 상태를 확인할 수 없습니다.", error);
      navigate("/auth/login");
    }
  }, [navigate]);

  useEffect(() => {
    checkLoginStatus();
  }, [checkLoginStatus]);

  return <>{children}</>;
};

export default CheckLoginStatus;
