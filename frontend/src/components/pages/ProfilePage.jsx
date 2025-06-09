import React, { useState, useEffect } from "react";
import axios from 'utils/axiosInstance';
import styles from "styles/Profile.module.css";
import Button from "../Button";
import HeaderComponent from "../HeaderComponent";

const ProfilePage = () => {
  const [email, setEmail] = useState("");
  const [nickname, setNickname] = useState("");
  const [currentPassword, setCurrentPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [isNicknameChanged, setIsNicknameChanged] = useState(false);
  const [providers, setProviders] = useState([]);

  useEffect(() => {
    fetchUserInfo();
    fetchProviders();
    checkOAuthError();
  }, []);

  const checkOAuthError = () => {
    const cookies = document.cookie.split(";").map(c => c.trim());
    const errorCookie = cookies.find(c => c.startsWith("oauth_error="));
    if (errorCookie) {
      const rawValue = errorCookie.split("=")[1];
      const decodedValue = decodeURIComponent(rawValue.replace(/\+/g, " "));
      alert(decodedValue);

      // Clear the cookie
      document.cookie = "oauth_error=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    }
  };

  const fetchUserInfo = async () => {
    try {
      const response = await axios.get("/user/info", { withCredentials: true });
      setEmail(response.data.email);
      setNickname(response.data.nickname);
    } catch (error) {
      console.error("Failed to fetch user info:", error);
    }
  };

  const fetchProviders = async () => {
    try {
      const response = await axios.get("/check/provider", { withCredentials: true });
      setProviders(response.data);
    } catch (error) {
      console.error("Failed to fetch providers:", error);
    }
  };

  const handleNicknameChange = async () => {
    if (!nickname.trim()) {
      alert("닉네임을 입력해주세요.");
      return;
    }

    try {
      await axios.patch("/user/info",
        { nickname },
        {
          headers: {
            "Content-Type": "application/json"
          },
          withCredentials: true,
        }
      );

      alert("닉네임이 변경되었습니다.");
      setIsNicknameChanged(false); // 변경 상태 초기화
      fetchUserInfo(); // 사용자 정보 새로 가져오기
    } catch (error) {
      alert(error.response.data.message);
    }
  };

  const handlePasswordChange = async () => {
    if (newPassword !== confirmPassword) {
      alert("비밀번호가 일치하지 않습니다.");
      return;
    }

    try {
      await axios.patch(
        "/user/password",
        { currentPassword, newPassword },
        { headers: { "Content-Type": "application/json" }, withCredentials: true }
      );
      alert("비밀번호가 성공적으로 변경되었습니다.");
      setCurrentPassword("");
      setNewPassword("");
      setConfirmPassword("");
    } catch (error) {
      alert(error.response.data.message);
    }
  };

  const handleNicknameInput = (e) => {
    const value = e.target.value;
    setNickname(value);
    setIsNicknameChanged(value !== email && value.trim() !== "");
  };

  const handleGoogleLink = async () => {
    try {
      const response = await axios.post("/auth/oauth2/link", null, {
        withCredentials: true
      });
      
      // 응답으로 받은 URL로 리다이렉트
      window.location.href = `${process.env.REACT_APP_DOMAIN}${response.data}`;
    } catch (error) {
      console.error("Failed to initiate Google account linking:", error);
      alert("Google 계정 연동을 시작할 수 없습니다. 다시 시도해주세요.");
    }
  };

  const isGoogleLinked = providers.some(provider => provider.provider.toLowerCase() === "google");

  return (
    <div>
      <HeaderComponent mode="profile" />

      <div className={styles.formContainer}>
        <h2>프로필 수정</h2>

        <form>
          <div className={styles.infoRow}>
            <label>이메일:</label>
            <span id="email" className={styles.email}>{email}</span>
          </div>

          <div className={styles.infoRow}>
            <label htmlFor="nickname">닉네임:</label>
            <input
              type="text"
              id="nickname"
              value={nickname}
              onChange={handleNicknameInput}
            />
            <Button
              type="button"
              variant={isNicknameChanged ? "green" : "disabled"}
              size="input"
              onClick={handleNicknameChange}
              disabled={!isNicknameChanged}
            >
              변경
            </Button>
          </div>

          <div className={styles.infoRow}>
            <label htmlFor="currentPassword">현재 비밀번호:</label>
            <input
              type="password"
              id="currentPassword"
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              placeholder="현재 비밀번호를 입력하세요."
              autoComplete="off"
            />
          </div>

          <div className={styles.infoRow}>
            <label htmlFor="newPassword">새 비밀번호:</label>
            <input
              type="password"
              id="newPassword"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              placeholder="새 비밀번호를 입력하세요."
              autoComplete="off"
            />
          </div>

          <div className={styles.infoRow}>
            <label htmlFor="confirmPassword">비밀번호 확인:</label>
            <input
              type="password"
              id="confirmPassword"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              placeholder="비밀번호를 확인하세요."
              autoComplete="off"
            />
          </div>
          <div className={styles.passwordText}>
            <small>
              영문, 숫자, 특수문자를 포함하여 8자 이상의 비밀번호를 사용하세요.<br/>
            </small>
          </div>
          <Button
            type="button"
            variant="green"
            size="medium"
            onClick={handlePasswordChange}
          >
            비밀번호 변경
          </Button>
        </form>

        <hr className={styles.divider} />
        
        <button
          className={styles.googleButton}
          onClick={isGoogleLinked ? undefined : handleGoogleLink}
          disabled={isGoogleLinked}
        >
          <img src="/images/google-logo.svg" alt="Google" className={styles.googleIcon} />
          <span className={styles.googleText}>
            {isGoogleLinked ? "Google 계정 연동됨" : "Google 계정 연동"}
          </span>
        </button>
      </div>
    </div>
  );
};

export default ProfilePage;
