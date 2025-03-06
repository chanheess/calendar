import { useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import { getToken, deleteToken } from "firebase/messaging";
import { messaging } from "../firebase";


const CheckLoginStatus = ({ children }) => {
  const navigate = useNavigate();

  const checkLoginStatus = useCallback(async () => {
    const token = await getToken(messaging, {
      vapidKey: "BOOYYhMRpzdRL1n3Nnwm8jAhu1be-_tiMQKpCRPzBs4hXY85KB4yX9kR65__1hOB43Uj7ixfhHyPPSYA1NsNBSI"
    });

    try {
      const response = await axios.get(`/auth/check/${token}`, { withCredentials: true });

      if (!response.data) {
        throw new Error("Invalid login status");
      }
    } catch (error) {
      if (token) {
        await deleteToken(messaging, token);
        localStorage.removeItem("fcmToken");
      }

      alert("로그인 상태를 확인할 수 없습니다.");
      navigate("/auth/login");
    }
  }, [navigate]);

  useEffect(() => {
    checkLoginStatus();
  }, [checkLoginStatus]);

  return <>{children}</>;
};

export default CheckLoginStatus;
