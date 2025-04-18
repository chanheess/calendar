import React, { useState, useEffect, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import axios from "axios";
import styles from "styles/Register.module.css";
import Button from "../Button";

const RegisterPage = () => {
  const [email, setEmail] = useState("");
  const [emailCode, setEmailCode] = useState("");
  const [nickname, setNickname] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [isEmailValid, setIsEmailValid] = useState(false);
  const [isPasswordMatch, setIsPasswordMatch] = useState(true);
  const [isSubmitEnabled, setIsSubmitEnabled] = useState(false);

  const navigate = useNavigate();

  const emailPattern = useMemo(() => /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, []);

  useEffect(() => {
    setIsEmailValid(emailPattern.test(email));

    const isPasswordValid = password === confirmPassword && password !== "";
    setIsPasswordMatch(isPasswordValid);

    const isFormValid =
      emailPattern.test(email) &&
      isPasswordValid &&
      emailCode.trim() !== "" &&
      nickname.trim() !== "";

    setIsSubmitEnabled(isFormValid);
  }, [email, emailCode, nickname, password, confirmPassword, emailPattern]);

  const handleEmailVerification = async () => {
    if (!isEmailValid) return;
    const data = { email: email, type: "REGISTER" };

    try {
      const response = await axios.post("/auth/mail", data);
      alert(response.data);
    } catch (error) {
      alert(error.response.data.message);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const payload = {
        email,
        emailCode,
        nickname,
        password,
      };

      await axios.post("/auth/register", payload, {
        headers: {
            "Content-Type": "application/json",
            },
        });

      alert("Registration successful!");
      navigate("/auth/login");
    } catch (error) {
      if (error.response) {
        alert(error.response.data.message);
      } else {
        alert(error.message);
      }
    }
  };

  return (
    <div className={styles.formContainer}>
      <h2>회원가입</h2>
      <form onSubmit={handleSubmit}>
        <div className={styles.infoRow}>
          <label htmlFor="email">이메일:</label>
          <input
            type="email"
            id="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="example@nicesite.com"
            required
          />
          <Button
            type="button"
            size="input"
            variant={isEmailValid ? "green" : "disabled"}
            onClick={handleEmailVerification}
            disabled={!isEmailValid}
          >
            인증
          </Button>
        </div>
        <div className={styles.infoRow}>
          <label htmlFor="email-verification">인증 코드:</label>
          <input
            type="text"
            id="email-verification"
            value={emailCode}
            onChange={(e) => setEmailCode(e.target.value)}
            placeholder="인증 코드를 입력하세요."
            required
          />
        </div>
        <div className={styles.infoRow}>
          <label htmlFor="password">비밀번호:</label>
          <input
            type="password"
            id="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="비밀번호를 입력하세요."
            autoComplete="new-password"
            required
          />
        </div>
        <div className={styles.infoRow}>
          <label htmlFor="confirmPassword">비밀번호 확인:</label>
          <input
            type="password"
            id="confirmPassword"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            placeholder="비밀번호를 다시 입력하세요."
            autoComplete="new-password"
            required
          />
        </div>
        <div className={styles.passwordText}>
          <small>
            영문, 숫자, 특수문자를 포함하여 8자 이상의 비밀번호를 사용하세요.<br/>
          </small>
          {!isPasswordMatch && (
            <small className={styles.passwordError}>비밀번호가 일치하지 않습니다.</small>
          )}
        </div>
        <div className={styles.infoRow}>
          <label htmlFor="nickname">닉네임:</label>
          <input
            type="text"
            id="nickname"
            value={nickname}
            onChange={(e) => setNickname(e.target.value)}
            placeholder="닉네임을 입력하세요."
            required
          />
        </div>
        <Button
          type="submit"
          variant={isSubmitEnabled ? "green" : "disabled"}
          size="medium"
          margin="top"
          disabled={!isSubmitEnabled}
        >
          회원가입
        </Button>
        <Button
          type="button"
          variant="danger"
          size="medium"
          margin="top"
          onClick={() => (navigate("/auth/login"))}
        >
          돌아가기
        </Button>
      </form>
    </div>
  );
};

export default RegisterPage;
