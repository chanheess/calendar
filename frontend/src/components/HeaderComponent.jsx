import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import Nickname from "./Nickname";
import axios from "axios";
import styles from "styles/Header.module.css";
import Button from "./Button";
import { getFirebaseToken } from "components/FirebaseToken";
import LoadingOverlay from "components/LoadingOverlay";

/** 우측하단에 "예/아니오/미정" 버튼을 드롭다운으로 띄우는 작은 컴포넌트 */
function MoreActions({ notification, onAction }) {
  const [open, setOpen] = useState(false);
  const toggleDropdown = () => setOpen(!open);

  const handleClick = (action) => {
    onAction(notification, action);
    setOpen(false);
  };

  return (
    <div className={styles.actionContainer}>
      {/* "더보기" 버튼 (또는 아이콘) */}
      <button className={styles.moreButton} onClick={toggleDropdown}>
        더보기
      </button>

      {open && (
        <div className={styles.actionDropdown}>
          <button onClick={() => handleClick("accept")}>예</button>
          <button onClick={() => handleClick("reject")}>아니오</button>
          <button onClick={() => handleClick("maybe")}>미정</button>
        </div>
      )}
    </div>
  );
}

const HeaderComponent = ({ mode, onSidebarToggle }) => {
  const [notifications, setNotifications] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const navigate = useNavigate();

  useEffect(() => {
    fetchNotifications(); // 처음 마운트될 때 실행
  }, []);

  useEffect(() => {
    if (showDropdown) {
      fetchNotifications(); // 알림 드롭다운을 열 때마다 다시 가져옴
    }
  }, [showDropdown]);

  const toggleDropdown = () => {
    setShowDropdown((prev) => !prev);
  };

  async function fetchNotifications() {
    try {
      const response = await axios.get("/notifications", { withCredentials: true });
      setNotifications(response.data || []);
    } catch (error) {
      console.error("Error fetching notifications:", error);
    }
  }

  const handleNotificationAction = async (notification, action) => {
    let url = "";
    let method = "";

    switch (action) {
      case "accept":
        url = "/notifications/accept";
        method = "POST";
        break;
      case "reject":
        url = "/notifications/reject";
        method = "DELETE";
        break;
      case "maybe":
        url = "/notifications/maybe";
        method = "POST";
        break;
      default:
        console.error(`Unknown action: ${action}`);
        return;
    }

    try {
      const response = await axios({
        method,
        url,
        data: notification,
        withCredentials: true,
        headers: { "Content-Type": "application/json" },
      });

      if (response.status === 200) {
        alert(`알림 처리 완료`);
        fetchNotifications(); // 다시 목록 불러오기
      } else {
        alert("요청 처리 중 문제가 발생했습니다.");
      }
    } catch (error) {
      console.error("Error processing notification:", error);
    }
  };

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

  // "profile" 모드 렌더링
  if (mode === "profile") {
    return (
      <header className={styles.header}>
        {isLoading && <LoadingOverlay fullScreen={true} />}
        <span className={styles.logo} onClick={handleHome}>
          chcalendar
        </span>

        <div className={styles.rightSection}>
          <Nickname variant="" size="medium" />
          <Button variant="logout" size="small" onClick={handleLogout}>
            로그아웃
          </Button>
        </div>
      </header>
    );
  }

  // "main" 모드 렌더링
  return (
    <header className={styles.header}>
      {isLoading && <LoadingOverlay fullScreen={true} />}

      {/* 왼쪽 로고 */}
      <div className={styles.leftSection}>
        <a href={process.env.REACT_APP_HOME_URL} className={styles.logo}>
          chcalendar
        </a>
      </div>

      {/* 오른쪽 영역 */}
      <div className={styles.rightSection}>
        <Nickname variant="" size="medium" />

        <Button variant="green" size="header" onClick={handleProfile}>
          내 정보
        </Button>
        <Button variant="logout" size="header" onClick={handleLogout}>
          로그아웃
        </Button>

        {/* 알림(벨) 아이콘 버튼 + 드롭다운 */}
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
                  {notifications.map((notification, index) => (
                    <li key={index} className={styles.notificationCard}>
                      <div className={styles.notificationContent}>
                        <p className={styles.notificationText}>
                          {notification.message}
                        </p>
                        {/* 우측 하단 "더보기" → 예/아니오/미정 드롭다운 */}
                        <MoreActions
                          notification={notification}
                          onAction={handleNotificationAction}
                        />
                      </div>
                    </li>
                  ))}
                </ul>
              ) : (
                <p className={styles.noNotifications}>알림이 없습니다.</p>
              )}
              <div className={styles.dropdownFooter} />
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default HeaderComponent;