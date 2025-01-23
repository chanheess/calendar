import React, { useState } from "react";
import Nickname from "./Nickname";
import axios from "axios";
import styles from "../styles/Header.module.css";
import Button from "./Button";

const HeaderComponent = ({ mode, onSidebarToggle }) => {
  const [notifications, setNotifications] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);

  const toggleDropdown = () => {
    console.log("Dropdown toggled:", !showDropdown);
    setShowDropdown((prevState) => !prevState);
  };

  const handleLogout = () => {
    axios
      .post(`/auth/logout`, null, {
        withCredentials: true,
      })
      .then((response) => {
        if (response.status === 200) {
          window.location.href = "/auth/login";
        } else {
          alert("Unexpected response from the server");
        }
      })
      .catch((error) => {
        if (error.response) {
          alert("Error: " + (error.response.data.message || error.response.data));
        } else {
          alert("Error: " + error.message);
        }
      });
  };

  const handleHome = () => {
    window.location.href = "/";
  };

  const handleProfile = () => {
    window.location.href = "/user/profile";
  };

  // "profile" 모드 렌더링
  if (mode === "profile") {
    return (
      <header className={styles.header}>
        <div className={styles.leftSection}>
          <span className={styles.logo}>chcalendar</span>
        </div>

        <div className={styles.rightSection}>
          <Nickname variant="" size="medium" />
          <Button variant="green" size="small" onClick={handleHome}>
            Home
          </Button>
          <Button variant="logout" size="small" onClick={handleLogout}>
            Logout
          </Button>
        </div>
      </header>
    );
  }

  // "main" 모드 렌더링
  return (
    <header className={styles.header}>
      <div className={styles.leftSection}>
        <Button
          variant="function"
          size="small"
          onClick={onSidebarToggle}
          aria-label="Toggle Sidebar"
        >
          ☰
        </Button>
        <span className={styles.logo}>chcalendar</span>
      </div>

      <div className={styles.rightSection}>
        <Nickname variant="" size="medium" />
        <Button variant="green" size="small" onClick={handleProfile}>
          Profile
        </Button>
        <Button variant="logout" size="small" onClick={handleLogout}>
          Logout
        </Button>
        <div className={styles.notificationWrapper}>
          <Button
            variant="function"
            size="small"
            onClick={toggleDropdown}
            aria-label="View Notifications"
          >
            🔔
            {notifications.length > 0 && (
              <span className={styles.badge}>{notifications.length}</span>
            )}
          </Button>
          {showDropdown && (
            <div className={styles.dropdown}>
              <h3>Notifications</h3>
              {notifications.length > 0 ? (
                <ul className={styles.notificationList}>
                  {notifications.map((notification, index) => (
                    <li key={index} className={styles.notificationItem}>
                      {notification}
                    </li>
                  ))}
                </ul>
              ) : (
                <p className={styles.noNotifications}>알림이 없습니다.</p>
              )}
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default HeaderComponent;
