import React, { useState, useEffect } from "react";
import { useLocation } from "react-router-dom";
import MainLandingPage from "./pages/MainLandingPage";
import HomePage from "./pages/HomePage";
import LoadingOverlay from "./LoadingOverlay";
import { checkLoginStatus } from "../utils/authUtils";

const ConditionalRoute = ({ children, fallback = <MainLandingPage /> }) => {
  const [isLoggedIn, setIsLoggedIn] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const location = useLocation();

  useEffect(() => {
    const verifyLoginStatus = async () => {
      try {
        const isLoggedIn = await checkLoginStatus();
        setIsLoggedIn(isLoggedIn);
      } catch (error) {
        console.error("로그인 상태를 확인할 수 없습니다.", error);
        setIsLoggedIn(false);
      } finally {
        setIsLoading(false);
      }
    };

    verifyLoginStatus();
  }, [location.pathname]); // 경로가 변경될 때마다 로그인 상태 재확인

  if (isLoading) {
    return <LoadingOverlay fullScreen={true} />;
  }

  // children이 제공된 경우 해당 컴포넌트를 렌더링, 아니면 경로에 따라 기본 컴포넌트 렌더링
  if (children) {
    return isLoggedIn ? children : fallback;
  }
  
  // 루트 경로의 경우
  return isLoggedIn ? <HomePage /> : fallback;
};

export default ConditionalRoute; 