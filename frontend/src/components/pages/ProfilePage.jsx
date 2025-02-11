import React, { useState, useEffect } from "react";
import axios from "axios";
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

  useEffect(() => {
    fetchUserInfo();
  }, []);

  const fetchUserInfo = async () => {
    try {
      const response = await axios.get("/user/info", { withCredentials: true });
      setEmail(response.data.email);
      setNickname(response.data.nickname);
    } catch (error) {
      console.error("Failed to fetch user info:", error);
      alert("Failed to fetch user info. Please try again.");
    }
  };

  const handleNicknameChange = async () => {
    if (!nickname.trim()) {
      alert("Nickname cannot be empty!");
      return;
    }

    try {
      const response = await axios.patch("/user/info",
        { nickname },
        {
          headers: {
            "Content-Type": "application/json"
          },
          withCredentials: true,
        }
      );

      alert(response.data);
      setIsNicknameChanged(false); // 변경 상태 초기화
      fetchUserInfo(); // 사용자 정보 새로 가져오기
    } catch (error) {
      alert(error.response.data.message);
    }
  };

  const handlePasswordChange = async () => {
    if (newPassword !== confirmPassword) {
      alert("Passwords do not match!");
      return;
    }

    try {
      await axios.patch(
        "/user/password",
        { currentPassword, newPassword },
        { headers: { "Content-Type": "application/json" }, withCredentials: true }
      );
      alert("Password updated successfully!");
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

  return (
    <div>
      <HeaderComponent mode="profile" />

      <div className={styles.formContainer}>
        <h2>Edit Profile</h2>

        <form>
          <div className={styles.infoRow}>
            <label>Email:</label>
            <span id="email" className={styles.email}>{email}</span>
          </div>

          <div className={styles.infoRow}>
            <label htmlFor="nickname">Nickname:</label>
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
              Rename
            </Button>
          </div>

          <div className={styles.infoRow}>
            <label htmlFor="currentPassword">Current Password:</label>
            <input
              type="password"
              id="currentPassword"
              value={currentPassword}
              onChange={(e) => setCurrentPassword(e.target.value)}
              placeholder="Enter current password"
              autoComplete="off"
            />
          </div>

          <div className={styles.infoRow}>
            <label htmlFor="newPassword">New Password:</label>
            <input
              type="password"
              id="newPassword"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              placeholder="Enter new password"
              autoComplete="off"
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
              autoComplete="off"
            />
          </div>
          <Button
            type="button"
            variant="green"
            size="medium"
            onClick={handlePasswordChange}
          >
            Change Password
          </Button>
        </form>
      </div>
    </div>
  );
};

export default ProfilePage;
