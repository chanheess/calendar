import { useEffect, useCallback } from "react";
import { useNavigate } from "react-router-dom";


const CheckLoginStatus = ({ children }) => {
  const navigate = useNavigate();

  const checkLoginStatus = useCallback(async () => {
    try {
      const response = await fetch("/auth/check", {
        method: "GET",
        credentials: "include",
      });

      if (response.ok) {
        const isLoggedIn = await response.json();

        if (!isLoggedIn) {
          alert("로그인이 필요합니다.");
          navigate("/auth/login");
        }
      } else {
        throw new Error("Failed to check login status");
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

  return <>{children}</>; // 로그인 상태 확인 후 자식 컴포넌트 렌더링
};

export default CheckLoginStatus;
