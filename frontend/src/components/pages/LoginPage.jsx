import React, { useState } from "react";
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
        alert("Invalid email or password.");
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
      <small onClick={() => setPasswordResetPopupVisible(true)}>비밀번호 찾기</small>
      {errorMessage && <div className={styles.error}>{errorMessage}</div>}
    </div>
    </>
  );
};

export default LoginPage;
