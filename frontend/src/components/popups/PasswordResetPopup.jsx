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

  const [timeLeft, setTimeLeft] = useState(300); // second
  const [isCodeExpired, setIsCodeExpired] = useState(false);

  useEffect(() => {
    setIsEmailValid(emailPattern.test(email));

    const isPasswordValid = password === confirmPassword && password !== "";
    setIsPasswordMatch(isPasswordValid);
  }, [email, emailPattern, emailCode, password, confirmPassword]);

  // Esc 키 눌렀을 때 팝업 닫기
  useEffect(() => {
    const handleEsc = (event) => {
      if (event.key === "Escape") {
        onClose();
      }
    };
    window.addEventListener("keydown", handleEsc);
    return () => window.removeEventListener("keydown", handleEsc);
  }, [onClose]);

  useEffect(() => {
    if (timeLeft > 0 && isCodeVisible) {
      const timer = setInterval(() => {
        setTimeLeft((prev) => prev - 1);
      }, 1000);
      return () => clearInterval(timer);
    } else if (timeLeft === 0) {
      setIsCodeExpired(true); // 타이머 만료 시
    }
  }, [timeLeft, isCodeVisible]);

  if (!isOpen) return null;

  const handleEmailVerification = async () => {
    if (!isEmailValid) return;
    const data = { email: email, type: "PASSWORD_RESET" };

    try {
      const response = await axios.post("/auth/mail", data);
      openChangePassword();
      alert(response.data);
    } catch (error) {
      alert(error.response.data.message);
    }
  };

  const handlePasswordChange = async (e) => {
    e.preventDefault();

    if (password !== confirmPassword) {
      alert("비밀번호가 일치하지 않습니다!");
      return;
    }

    const data = { email: email, password: password, emailCode: emailCode };
    try {
      const response = await axios.patch("/auth/change-password", data);
      alert(response.data);
      onClose();
    } catch (error) {
      alert(error.response.data.message);
    }
  };

  function openChangePassword() {
    setIsCodeVisible(true);
    setTimeLeft(300);
    setPassword("");
    setConfirmPassword("");
  }

  function cancelPasswordChange() {
    setIsCodeVisible(false);
    setEmail("");
    setEmailCode("");
    setIsCodeExpired(false);
  }

  // 이메일 포맷을 숨기기 위한 함수
  const formatEmail = (email) => {
    const parts = email.split("@");
    const localPart = parts[0];
    const domainPart = parts[1];

    return `${localPart.slice(0, 5)}*****@${domainPart}`;
  };

  // 시간 포맷팅 함수
  const formatTime = (seconds) => {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;
    return `${minutes}:${remainingSeconds < 10 ? "0" : ""}${remainingSeconds}`;
  };

  return (
    <div className={styles.popupOverlay} style={{ zIndex: 1001 }} onClick={onClose}>
      <div className={styles.popup} onClick={(e) => e.stopPropagation()}>
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
            <form onSubmit={handlePasswordChange}>
              <div className={styles.infoRow}>
                <label htmlFor="email">이메일:</label>
                <p>{formatEmail(email)}</p>
              </div>
              <div className={styles.infoRow}>
                <label htmlFor="email-verification">인증 코드:</label>
                <input
                  type="text"
                  id="email-verification"
                  value={emailCode}
                  onChange={(e) => setEmailCode(e.target.value)}
                  placeholder={`인증 코드를 입력하세요 (${formatTime(timeLeft)})`} // 타이머와 함께 표시
                  required
                  autoComplete="off"
                  disabled={isCodeExpired} // 코드 만료되면 비활성화
                />
              </div>
              <div className={styles.infoRow}>
                <label htmlFor="password">비밀번호:</label>
                <input
                  type="password"
                  id="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="새 비밀번호를 입력해주세요."
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
                  문자, 숫자, 기호를 포함한 8-20자의 비밀번호를 사용하세요.<br/>
                </small>
                {!isPasswordMatch && (
                  <small className={styles.passwordError}>비밀번호가 일치하지 않습니다.<br/></small>
                )}
                {isCodeExpired && <small className={styles.passwordError}>인증 코드 유효기간이 만료되었습니다. 새 코드를 요청하세요.</small>}
              </div>
              <div className={styles.infoRow}>
                <Button
                  variant="green" size="medium"
                  type="summit"
                  disabled={isCodeExpired} // 코드 만료되면 비활성화
                >
                  비밀번호 변경
                </Button>
                <Button
                  variant="logout" size="medium"
                  onClick={cancelPasswordChange}
                >
                  취소
                </Button>
              </div>
            </form>
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default PasswordResetPopup;
