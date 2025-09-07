import React, { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import axios from 'utils/axiosInstance';
import styles from "styles/Login.module.css";
import Button from "../Button";
import PasswordResetPopup from "../popups/PasswordResetPopup";
import LoadingOverlay from "../LoadingOverlay";
import { getRedirectPath, clearRedirectPath } from "../../utils/authUtils";

const LoginPage = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [passwordResetPopupVisible, setPasswordResetPopupVisible] = useState(false);
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    const cookies = document.cookie.split(";").map(c => c.trim());
    const errorCookie = cookies.find(c => c.startsWith("oauth_error="));
    if (errorCookie) {
      const rawValue = errorCookie.split("=")[1];
      const decodedValue = decodeURIComponent(rawValue.replace(/\+/g, " "));
      alert(decodedValue);

      // Clear the cookie
      document.cookie = "oauth_error=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    }
  }, []);

  // 로그인 페이지 진입 시 현재 경로를 저장
  useEffect(() => {
    if (location.state?.from) {
      sessionStorage.setItem('redirectPath', location.state.from);
    } else if (location.pathname !== '/auth/login') {
      sessionStorage.setItem('redirectPath', location.pathname);
    }
  }, [location]);

  const handleSubmit = async (event) => {
    event.preventDefault();

    try {
      await axios.post(
        "/auth/login",
        { email, password },
        {
          headers: {
            "Content-Type": "application/json",
          },
        }
      );

      // 로그인 성공 후 OAuth 계정 연동 상태 확인
      const hasOAuthLink = await checkAndRequestOAuthLink();

      // OAuth 연동이 없을 때만 원래 페이지로 이동
      if (!hasOAuthLink) {
        const redirectPath = getRedirectPath();
        clearRedirectPath();
        navigate(redirectPath);
      }
    } catch (error) {
      if (error.response && error.response.data) {
        setErrorMessage(error.response.data.message || "Unknown error occurred");
      } else {
        setErrorMessage(error.message);
      }
    }
  };

  // OAuth 계정 연동 상태 확인 및 연동 요청
  const checkAndRequestOAuthLink = async () => {
    try {
      // OAuth 연동 상태 확인
      const providerResponse = await axios.get("/check/provider");
      
      const isGoogleLinked = providerResponse.data.some(
        provider => provider.provider.toLowerCase() === "google"
      );

      if (isGoogleLinked) {
        const response = await axios.post(`/auth/oauth2/login/${"local"}`);

        window.location.href = `${process.env.REACT_APP_DOMAIN}${response.data}`;
        return true;
      }
      return false;
    } catch (error) {
      console.error("OAuth 연동 상태 확인 중 오류:", error);
      return false;
    }
  };

  function closePasswordResetPopup () {
    setPasswordResetPopupVisible(false);
  }

  return (
    <>
    {passwordResetPopupVisible && (
      <PasswordResetPopup
        isOpen={passwordResetPopupVisible}
        onClose={closePasswordResetPopup}
      />
    )}
    <div className={styles.formContainer}>
      <h2>로그인</h2>
      <form onSubmit={handleSubmit}>
        <input
          type="email"
          name="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
          className={styles.input}
        />
        <input
          type="password"
          name="password"
          placeholder="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
          autoComplete="on"
          className={styles.input}
        />
        <Button variant="green" size="medium" margin="top" type="submit">
          로그인
        </Button>
      </form>
      <hr className={styles.divider} />
      <button
        className={styles.googleButton}
        onClick={async () => {
          setLoading(true);
          try {
            const response = await axios.post(`/auth/oauth2/login/${"oauth"}`);
            window.location.href = `${process.env.REACT_APP_DOMAIN}${response.data}`;
          } catch (error) {
            console.error("OAuth 로그인 요청 중 오류:", error);
            // 에러 발생 시 직접 호출
            window.location.href = `${process.env.REACT_APP_DOMAIN}/oauth2/authorization/google`;
          }
        }}
      >
        <img src="/images/google-logo.svg" alt="Google" className={styles.googleIcon} />
        <span className={styles.googleText}>Google 계정으로 로그인</span>
      </button>
      {loading && <LoadingOverlay fullScreen={true} />}
      <div className={styles.bottomLinksRow}>
        <button
          type="button"
          className={styles.textLink}
          onClick={() => navigate("/auth/register")}
        >
          회원가입
        </button>
        <span className={styles.dividerDot}>|</span>
        <button
          type="button"
          className={styles.textLink}
          onClick={() => setPasswordResetPopupVisible(true)}
        >
          비밀번호 찾기
        </button>
      </div>
      {errorMessage && <div className={styles.error}>{errorMessage}</div>}
    </div>
    </>
  );
};

export default LoginPage;
