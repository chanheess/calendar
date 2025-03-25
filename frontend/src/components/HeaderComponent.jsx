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
  const [isLoading, setIsLoading] = useState(false);

  const navigate = useNavigate();

  useEffect(() => {
      fetchNotifications(); // 처음 마운트될 때 실행
  }, []);

  useEffect(() => {
    if (showDropdown) {
      fetchNotifications(); // 알림 드롭다운을 열 때만 다시 가져옴
    }
  }, [showDropdown]);

  const toggleDropdown = () => {
    setShowDropdown((prevState) => !prevState);
  };

  async function fetchNotifications() {
    try {
      const response = await axios.get("/notifications", {
        withCredentials: true,
      });
      setNotifications(response.data || []);
    } catch (error) {
      if (error.status === 401) {
        window.location.href = "/auth/login";
      }

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
        alert(`알림 ${action} 처리 완료`);
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
      setIsLoading(true); // 로딩 시작
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
      setIsLoading(false); // 로딩 종료
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
            Logout
          </Button>
        </div>
      </header>
    );
  }

  // "main" 모드 렌더링
  return (
    <header className={styles.header}>
      {isLoading && <LoadingOverlay fullScreen={true} />}
      <div className={styles.leftSection}>
        <span className={styles.logo} onClick={handleHome}>
          chcalendar
        </span>
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
                      <div className={styles.infoRow}>
                        <p>{notification.message}</p>
                        <div className={styles.infoRow}>
                          <Button
                            variant="green"
                            size="small"
                            onClick={() => handleNotificationAction(notification, "accept")}
                          >
                            Yes
                          </Button>
                          <Button
                            variant="logout"
                            size="small"
                            onClick={() => handleNotificationAction(notification, "reject")}
                          >
                            No
                          </Button>
                          {notification.category === "SCHEDULE" && (
                            <Button
                              variant="secondary"
                              size="small"
                              onClick={() => handleNotificationAction(notification, "maybe")}
                            >
                              Maybe
                            </Button>
                          )}
                        </div>


                      </div> {/* <div className={styles.infoRow}> */}
                    </li>
                  ))}
                </ul>
              ) : (
                <p className={styles.noNotifications}>No notifications.</p>
              )}
              <div className={styles.dropdownFooter}/>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default HeaderComponent;
