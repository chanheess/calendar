import React, { useState, useEffect, useMemo } from "react";
import styles from "styles/Popup.module.css";
import axios from "axios";
import Button from "components/Button";

const PasswordResetPopup = ({ isOpen, onClose }) => {
  const [email, setEmail] = useState("");
  const [isEmailValid, setIsEmailValid] = useState(false);
  const emailPattern = useMemo(() => /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/, []);

  const [emailCode, setEmailCode] = useState("");
  const [isCodeVisible, setIsCodeVisible] = useState(false);

  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [isPasswordMatch, setIsPasswordMatch] = useState(true);

  useEffect(() => {
    setIsEmailValid(emailPattern.test(email));

    const isPasswordValid = password === confirmPassword && password !== "";
    setIsPasswordMatch(isPasswordValid);

  }, [email, emailPattern, emailCode, password, confirmPassword, isPasswordMatch]);

  if (!isOpen) return null;

  const handleEmailVerification = async () => {
    if (!isEmailValid) return;
    const data = { email: email, type: "PASSWORD_RESET" };

    try {
      const response = await axios.post("/auth/mail", data);
      openChangePassword();
      alert(response.data);
    } catch (error) {
      console.log(error);
      alert(error.response.data.message);
    }
  };

  const handlePasswordChange = async () => {
    if (password !== confirmPassword) {
      alert("Passwords do not match!");
      return;
    }

    const data = { email: email, password: password, emailCode: emailCode };
    try {
      const response = await axios.patch("/auth/change-password", data);
      console.log(response);

      alert(response.data);
      onClose();
    } catch (error) {
      alert(error.response.data.message);
    }
  };

  function openChangePassword() {
    setIsCodeVisible(true);
    setPassword("");
    setConfirmPassword("");
  }

  function cancelPasswordChange() {
    setIsCodeVisible(false);
    setEmail("");
  }

  return (
    <div className={styles.popupOverlay} style={{ zIndex: 1001 }}>
      <div className={styles.popup}>
        <div className={styles.popupHeader}>
          <h2>비밀번호 찾기</h2>
          <Button variant="close" size="" onClick={onClose}>×</Button>
        </div>
        <div className={styles.popupContent}>
          {!isCodeVisible && (
            <>
            <div className={styles.infoRow}>비밀번호를 찾기위한 이메일을 입력해주세요.</div>
            <div className={styles.infoRow}>
              <input
                type="email"
                name="email"
                placeholder="example@nicesite.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
              />
              <Button
                variant={isEmailValid ? "green" : "disabled"}
                size="input"
                onClick={handleEmailVerification}
                disabled={!isEmailValid || isCodeVisible}
                type="button"
              >
                Send
              </Button>
            </div>
            </>
          )}
          {isCodeVisible && (
            <>
            <div className={styles.infoRow}>
              <label htmlFor="email">Email:</label>
              <p>{email}</p>
            </div>
            <div className={styles.infoRow}>
              <label htmlFor="email-verification">Email code:</label>
              <input
                type="text"
                id="email-verification"
                value={emailCode}
                onChange={(e) => setEmailCode(e.target.value)}
                placeholder="Enter verification code"
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
                placeholder="Enter new password"
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
                placeholder="Confirm new password"
                required
              />
            </div>
            <div className={styles.passwordText}>
              <small>
                Use 8-20 characters with letters, numbers and symbols.<br/>
              </small>
              {!isPasswordMatch && (
                <small className={styles.passwordError}>Passwords do not match</small>
              )}
            </div>
            <div className={styles.infoRow}>
              <Button
                variant="green" size="medium"
                onClick={handlePasswordChange}
              >
                Change Password
              </Button>
              <Button
                variant="logout" size="medium"
                onClick={cancelPasswordChange}
              >
                Cancel
              </Button>
            </div>
            </>
          )}
        </div>  {/* <div className={styles.popupContent}> */}
      </div>
    </div>
  );
};

export default PasswordResetPopup;
