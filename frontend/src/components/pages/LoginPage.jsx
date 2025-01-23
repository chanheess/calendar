import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import styles from "../../styles/Login.module.css";
import Button from "../Button";

const LoginPage = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  const navigate = useNavigate();

  const handleSubmit = async (event) => {
    event.preventDefault();

    try {
      const response = await axios.post(
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
        // 서버에서 반환한 에러 메시지
        setErrorMessage(error.response.data.message || "Unknown error occurred");
      } else {
        // 기타 에러 처리
        setErrorMessage(error.message);
      }
      console.error("Error:", error.message);
    }
  };

  return (
    <div className={styles.formContainer}>
      <h2>Sign in</h2>
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
          className={styles.input}
        />
        <Button variant="green" size="medium" type="submit">
          Sign in
        </Button>
      </form>
      <Button variant="blue" size="medium" onClick={() => navigate("/auth/register")}>
        Sign up
      </Button>
      {errorMessage && <div className={styles.error}>{errorMessage}</div>}
    </div>
  );
};

export default LoginPage;
