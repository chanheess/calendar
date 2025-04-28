import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Nickname from "./Nickname";
import axios from "axios";
import styles from "styles/Header.module.css";
import Button from "./Button";
import { getFirebaseToken } from "components/FirebaseToken";
import LoadingOverlay from "components/LoadingOverlay";

const HeaderComponent = ({ mode, onSidebarToggle }) => {
  const [notifications, setNotifications] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);
  const [showMoreMenu, setShowMoreMenu] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isMobile, setIsMobile] = useState(window.innerWidth <= 600);

  const navigate = useNavigate();

  useEffect(() => {
    fetchNotifications();

    const handleResize = () => {
      setIsMobile(window.innerWidth <= 600);
    };
    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  useEffect(() => {
    if (showDropdown) {
      fetchNotifications();
    }
  }, [showDropdown]);

  const toggleDropdown = () => {
    setShowDropdown((prev) => !prev);
  };

  const toggleMoreMenu = () => {
    setShowMoreMenu((prev) => !prev);
  };

  async function fetchNotifications() {
    try {
      const response = await axios.get("/notifications", { withCredentials: true });
      setNotifications(response.data || []);
    } catch (error) {
      if (error.status === 401) {
        window.location.href = "/auth/login";
      }
      console.error("Error fetching notifications:", error);
    }
  }

  const handleLogout = async () => {
    try {
      setIsLoading(true);
      const token = await getFirebaseToken();
      if (token) {
        await axios.post(`/auth/logout/${token}`, {}, { withCredentials: true });
      } else {
        await axios.post(`/auth/logout`, {}, { withCredentials: true });
      }
      navigate("/auth/login");
    } catch (error) {
      console.error("Logout error:", error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleHome = () => {
    navigate("/");
  };

  const handleProfile = () => {
    navigate("/user/profile");
  };

  return (
    <header className={styles.header}>
      {isLoading && <LoadingOverlay fullScreen={true} />}

      {/* 왼쪽: 햄버거 + (데스크탑일 때 로고) */}
      <div className={styles.leftSection}>
        {isMobile && (
          <button className={styles.hamburgerButton} onClick={onSidebarToggle}>
            ☰
          </button>
        )}
        {!isMobile && (
          <a onClick={() => window.location.href = "/"} className={styles.logo}>
            chcalendar
          </a>
        )}
      </div>

      {/* 가운데: (모바일일 때 로고) */}
      {isMobile && (
        <div className={styles.centerSection}>
          <a onClick={() => window.location.href = "/"} className={styles.logo}>
            chcalendar
          </a>
        </div>
      )}

      {/* 오른쪽 */}
      <div className={styles.rightSection}>
        {/* 데스크탑 메뉴 */}
        {!isMobile && (
          <>
            <Nickname variant="" size="medium" />
            <Button variant="green" size="header" onClick={handleProfile}>
              내 정보
            </Button>
            <Button variant="logout" size="header" onClick={handleLogout}>
              로그아웃
            </Button>
          </>
        )}

        {/* 알림 버튼 (공통) */}
        <div className={styles.notificationWrapper}>
          <button
            className={styles.notificationBell}
            onClick={toggleDropdown}
            aria-label="알림 보기"
          >
            <span className={styles.bellIcon}>🔔</span>
            {notifications.length > 0 && (
              <span className={styles.badge}>{notifications.length}</span>
            )}
          </button>

          {showDropdown && (
            <div className={styles.notificationDropdown}>
              <h3 className={styles.dropdownHeader}>알림</h3>
              {notifications.length > 0 ? (
                <ul className={styles.notificationList}>
                  {notifications.map((notification, idx) => (
                    <li key={idx} className={styles.notificationCard}>
                      <p className={styles.notificationText}>{notification.message}</p>
                    </li>
                  ))}
                </ul>
              ) : (
                <p className={styles.noNotifications}>알림이 없습니다.</p>
              )}
            </div>
          )}
        </div>

        {/* 모바일: 더보기 메뉴 */}
        {isMobile && (
          <div className={styles.mobileMoreWrapper}>
            <button className={styles.moreButton} onClick={toggleMoreMenu}>
              ⋮
            </button>

            {showMoreMenu && (
              <div className={styles.moreMenu}>
                <Nickname variant="" size="small" />
                <Button variant="green" size="full" onClick={handleProfile}>
                  내 정보
                </Button>
                <Button variant="logout" size="full" onClick={handleLogout}>
                  로그아웃
                </Button>
              </div>
            )}
          </div>
        )}
      </div>
    </header>
  );
};

export default HeaderComponent;