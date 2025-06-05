import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import axios from 'utils/axiosInstance';
import styles from "styles/Login.module.css";
import Button from "../Button";
import PasswordResetPopup from "../popups/PasswordResetPopup";

const LoginPage = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const [passwordResetPopupVisible, setPasswordResetPopupVisible] = useState(false);

  const navigate = useNavigate();

  useEffect(() => {
    const cookies = document.cookie.split(";").map(c => c.trim());
    const errorCookie = cookies.find(c => c.startsWith("login_error="));
    if (errorCookie) {
      const rawValue = errorCookie.split("=")[1];
      const decodedValue = decodeURIComponent(rawValue.replace(/\+/g, " "));
      alert(decodedValue);

      // Clear the cookie
      document.cookie = "login_error=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    }
  }, []);

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

      navigate("/"); // 로그인 성공 시 리다이렉트
    } catch (error) {
      if (error.response && error.response.data) {
        alert("로그인에 실패했습니다. 다시 시도해주세요.");
        setErrorMessage(error.response.data.message || "Unknown error occurred");
      } else {
        setErrorMessage(error.message);
      }
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
      <Button variant="blue" size="medium" margin="top" onClick={() => navigate("/auth/register")}>
        회원가입
      </Button>
      <hr className={styles.divider} />
      <button
        className={styles.googleButton}
        onClick={() => window.location.href = "https://localhost/oauth2/authorization/google"}
      >
        <img src="/images/google-logo.svg" alt="Google" className={styles.googleIcon} />
        <span className={styles.googleText}>Google 계정으로 로그인</span>
      </button>
      <small onClick={() => setPasswordResetPopupVisible(true)}>비밀번호 찾기</small>
      {errorMessage && <div className={styles.error}>{errorMessage}</div>}
    </div>
    </>
  );
};

export default LoginPage;
