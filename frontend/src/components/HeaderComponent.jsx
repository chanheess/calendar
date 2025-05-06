import React, { useState, useEffect, useRef } from "react";
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

  const openDropdownRef = useRef(null);

  const handleToggleDropdown = (newRef) => {
    if (openDropdownRef.current && openDropdownRef.current !== newRef) {
      openDropdownRef.current.dispatchEvent(new CustomEvent("closeDropdown"));
    }
    openDropdownRef.current = newRef;
  };

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
      onCloseSidebarPopups(true); // true 전달로 "모든 팝업 닫기" 신호
    }
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

  if (mode === "profile") {
    return (
      <header className={styles.header}>
        <div className={styles.leftSection}>
          {isMobile && !isProfilePage && (
           <button className={styles.hamburgerButton} onClick={handleSidebarToggle}>
             ☰
           </button>
          )}
          {!isMobile && (
           <button type="button" onClick={handleHome} className={styles.logo}>
             chcalendar
           </button>
          )}
        </div>
        <div className={styles.rightSection}>
         {isMobile && (
           <div className={styles.centerSection}>
             <button type="button" onClick={handleHome} className={styles.logo}>
               chcalendar
             </button>
           </div>
         )}

         {!isMobile && (
           <>
             <Nickname variant="" size="medium" />
             <Button variant="logout" size="header" onClick={handleLogout}>
               로그아웃
             </Button>
           </>
         )}

         {isMobile && (
           <div className={styles.mobileMoreWrapper}>
             <button className={styles.moreButton} onClick={toggleMoreMenu}>
               ⋮
             </button>

             {showMoreMenu && (
               <div className={styles.moreMenu}>
                 <Nickname variant="" size="small" />
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
   }

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
          <button type="button" onClick={handleHome} className={styles.logo}>
            chcalendar
          </button>
        )}
      </div>

      {isMobile && (
        <div className={styles.centerSection}>
          <button type="button" onClick={handleHome} className={styles.logo}>
            chcalendar
          </button>
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
                      <MoreActions
                        notification={notification}
                        index={idx}
                        onAction={handleNotificationAction}
                        onToggle={handleToggleDropdown}
                      />
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

function MoreActions({ notification, index, onAction, onToggle }) {
  const [open, setOpen] = useState(false);
  const containerRef = useRef(null);

  const toggleDropdown = () => {
    if (!open) {
      onToggle(containerRef.current);
    }
    setOpen(!open);
  };

  useEffect(() => {
    const ref = containerRef.current;
    const handleClose = () => setOpen(false);
    if (ref) ref.addEventListener("closeDropdown", handleClose);
    return () => {
      if (ref) ref.removeEventListener("closeDropdown", handleClose);
    };
  }, []);

  const handleClick = (action) => {
    onAction(notification, action);
    setOpen(false);
  };

  return (
    <div className={styles.notificationContainer} ref={containerRef}>
      <button onClick={toggleDropdown}>더보기</button>
      {open && (
        <div className={styles.notificationSelect}>
          <button onClick={() => handleClick("accept")}>예</button>
          <button onClick={() => handleClick("reject")}>아니오</button>
          {notification.category === 'SCHEDULE' && (
            <button onClick={() => handleClick("maybe")}>미정</button>
          )}
        </div>
      )}
    </div>
  );
}

export default HeaderComponent;