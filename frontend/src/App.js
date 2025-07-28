import React, { useEffect } from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import LoginPage from "./components/pages/LoginPage";
import RegisterPage from "./components/pages/RegisterPage";
import ProfilePage from "./components/pages/ProfilePage";
import PrivacyPolicyPage from "./components/pages/PrivacyPolicyPage";
import TermsOfServicePage from "./components/pages/TermsOfServicePage";
import ConditionalRoute from "./components/ConditionalRoute";

function App() {
  // viewport 높이 동적 계산
  useEffect(() => {
    const setViewportHeight = () => {
      const vh = window.innerHeight * 0.01;
      document.documentElement.style.setProperty('--vh', `${vh}px`);
    };

    setViewportHeight();
    window.addEventListener('resize', setViewportHeight);
    window.addEventListener('orientationchange', setViewportHeight);

    // iOS Safari에서 주소창 표시/숨김에 대응
    let lastScrollTop = window.pageYOffset || document.documentElement.scrollTop;
    const handleScroll = () => {
      const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
      if (Math.abs(scrollTop - lastScrollTop) > 50) {  // 50px 이상 스크롤된 경우에만 처리
        setViewportHeight();
        lastScrollTop = scrollTop;
      }
    };

    window.addEventListener('scroll', handleScroll);

    return () => {
      window.removeEventListener('resize', setViewportHeight);
      window.removeEventListener('orientationchange', setViewportHeight);
      window.removeEventListener('scroll', handleScroll);
    };
  }, []);

  return (
    <Router>
      <Routes>
        <Route path="/user/profile" element={
          <ConditionalRoute>
            <ProfilePage />
          </ConditionalRoute>
        } />
        <Route path="/auth/login" element={<LoginPage />} />
        <Route path="/auth/register" element={<RegisterPage />} />
        <Route path="/" element={<ConditionalRoute />} />
        <Route path="/privacy-policy" element={<PrivacyPolicyPage />} />
        <Route path="/terms-of-service" element={<TermsOfServicePage />} />
      </Routes>
    </Router>
  );
}

export default App;
