import React, { useState, useEffect } from "react";
import axios from "axios";
import styles from "../../styles/Register.module.css";
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

  const emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

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
  }, [email, emailCode, nickname, password, confirmPassword]);

  const handleEmailVerification = () => {
    if (!isEmailValid) return;

    axios
      .post(`/auth/mail/${encodeURIComponent(email)}`)
      .then((response) => {
        alert("Verification email sent: " + response.data);
      })
      .catch((error) => {
        if (error.response) {
          alert("Error: " + error.response.data);
        } else {
          alert("Error: " + error.message);
        }
      });
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

      const response = await axios.post("/auth/register", payload, {
        headers: {
            "Content-Type": "application/json",
            },
        });

        alert("Registration successful!");
        window.location.href = "/auth/login";
    } catch (error) {
      if (error.response) {
        alert("Error: " + error.response.data.message);
      } else {
        alert("Error: " + error.message);
      }
    }
  };

  return (
    <div className={styles.formContainer}>
      <h2>Sign up</h2>
      <form onSubmit={handleSubmit}>
        <div className={styles.infoRow}>
          <label htmlFor="email">Email:</label>
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
            Verify
          </Button>
        </div>
        <div className={styles.infoRow}>
          <label htmlFor="email-verification">Email code:</label>
          <input
            type="text"
            id="email-verification"
            value={emailCode}
            onChange={(e) => setEmailCode(e.target.value)}
            placeholder="Enter your verification code"
            required
          />
        </div>
        <div className={styles.infoRow}>
          <label htmlFor="password">Password:</label>
          <input
            type="password"
            id="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="Enter your password"
            required
          />
        </div>
        <div className={styles.infoRow}>
          <label htmlFor="confirmPassword">Confirm Password:</label>
          <input
            type="password"
            id="confirmPassword"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            placeholder="Confirm your password"
            required
          />
        </div>
        {!isPasswordMatch && (
          <small className={styles.passwordError}>Passwords do not match</small>
        )}
        <div className={styles.infoRow}>
          <label htmlFor="nickname">Nickname:</label>
          <input
            type="text"
            id="nickname"
            value={nickname}
            onChange={(e) => setNickname(e.target.value)}
            placeholder="Enter your nickname"
            required
          />
        </div>
        <Button
          type="submit"
          variant={isSubmitEnabled ? "green" : "disabled"}
          size="medium"
          disabled={!isSubmitEnabled}
        >
          Sign Up
        </Button>
        <Button
          type="button"
          variant="danger"
          size="medium"
          onClick={() => (window.location.href = "/auth/login")}
        >
          Back
        </Button>
      </form>
    </div>
  );
};

export default RegisterPage;
