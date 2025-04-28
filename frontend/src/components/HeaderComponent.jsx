import React, { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom"; // useLocation 추가
import Nickname from "./Nickname";
import axios from "axios";
import styles from "styles/Header.module.css";
import Button from "./Button";
import { getFirebaseToken } from "components/FirebaseToken";
import LoadingOverlay from "components/LoadingOverlay";

const HeaderComponent = ({ mode, onSidebarToggle, onCloseSidebarPopups }) => {
  const [notifications, setNotifications] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);
  const [showMoreMenu, setShowMoreMenu] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isMobile, setIsMobile] = useState(window.innerWidth <= 600);

  const navigate = useNavigate();
  const location = useLocation();
  const isProfilePage = location.pathname === "/user/profile";

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
    setShowDropdown((prev) => {
      if (!prev) {
        setShowMoreMenu(false); // 수정: 알림 열 때 더보기 닫기
        if (typeof onCloseSidebarPopups === "function") {
          onCloseSidebarPopups();
        }
      }
      return !prev;
    });
  };

  const toggleMoreMenu = () => {
    setShowMoreMenu((prev) => {
      if (!prev) {
        setShowDropdown(false); // 수정: 더보기 열 때 알림 닫기
        if (typeof onCloseSidebarPopups === "function") {
          onCloseSidebarPopups();
        }
      }
      return !prev;
    });
  };

  const handleSidebarToggle = () => {
    onSidebarToggle();
    setShowDropdown(false);
    setShowMoreMenu(false);
    if (typeof onCloseSidebarPopups === "function") {
      onCloseSidebarPopups();
    }
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

      <div className={styles.leftSection}>
        {isMobile && !isProfilePage && (
          <button className={styles.hamburgerButton} onClick={handleSidebarToggle}>
            ☰
          </button>
        )}
        {!isMobile && (
          <a onClick={handleHome} className={styles.logo}>
            chcalendar
          </a>
        )}
      </div>

      {isMobile && (
        <div className={styles.centerSection}>
          <a onClick={handleHome} className={styles.logo}>
            chcalendar
          </a>
        </div>
      )}

      <div className={styles.rightSection}>
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