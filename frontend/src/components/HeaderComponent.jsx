import React, { useState } from "react";
import Nickname from "./Nickname";
import styles from "../styles/Header.module.css";
import Button from "./Button";

const HeaderComponent = ({ onProfileClick, onLogout, onSidebarToggle }) => {
  const [notifications, setNotifications] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);

  const toggleDropdown = () => {
    console.log("Dropdown toggled:", !showDropdown);
    setShowDropdown((prevState) => !prevState);
  };


  return (
    <header className={styles.header}>
      <div className={styles.leftSection}>
        <Button
          variant="function" size="small"
          onClick={onSidebarToggle}
          aria-label="Toggle Sidebar"
        >
          ☰
        </Button>
        <span className={styles.logo}>chcalendar</span>
      </div>

      <div className={styles.rightSection}>
        <Nickname variant="" size="medium" />

        <Button variant="green" size="small" onClick={onProfileClick}>
          Profile
        </Button>
        <Button variant="logout" size="small" onClick={onLogout}>
          Logout
        </Button>
        <div className={styles.notificationWrapper}>
          <Button
            variant="function" size="small"
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
