import { useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";


const CheckLoginStatus = ({ children }) => {
  const navigate = useNavigate();

  const checkLoginStatus = useCallback(async () => {
    try {
      const response = await axios.get("/auth/check", {
        withCredentials: true,
      });

      const isLoggedIn = response.data;

      if (!isLoggedIn) {
        alert("로그인이 필요합니다.");
        navigate("/auth/login");
      }
    } catch (error) {
      console.error("Error checking login status:", error);
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
