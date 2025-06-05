import React, { useState, useEffect, useRef, forwardRef, useImperativeHandle } from "react";
import { createPortal } from "react-dom";
import { useNavigate, useLocation } from "react-router-dom"; // useLocation 추가
import Nickname from "./Nickname";
import axios from 'utils/axiosInstance';
import styles from "styles/Header.module.css";
import Button from "./Button";
import { getFirebaseToken } from "components/FirebaseToken";
import LoadingOverlay from "components/LoadingOverlay";

const HeaderComponent = forwardRef(({ mode, onSidebarToggle, onCloseSidebarPopups }, ref) => {
  const [notifications, setNotifications] = useState([]);
  const [showDropdown, setShowDropdown] = useState(false);
  const [showMoreMenu, setShowMoreMenu] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isMobile, setIsMobile] = useState(window.innerWidth <= 600);

  const navigate = useNavigate();
  const location = useLocation();
  const isProfilePage = location.pathname === "/user/profile";

  const openDropdownRef = useRef(null);
  const dropdownRef = useRef(null);
  const moreMenuRef = useRef(null);

  useImperativeHandle(ref, () => ({
    closeAllPopups: () => {
      setShowDropdown(false);
      setShowMoreMenu(false);
    }
  }));

  // 초기 알림 데이터 로딩
  useEffect(() => {
    fetchNotifications();
  }, []);

  // 리사이즈 이벤트 처리
  useEffect(() => {
    const handleResize = () => {
      setIsMobile(window.innerWidth <= 600);
    };
    window.addEventListener("resize", handleResize);
    return () => window.removeEventListener("resize", handleResize);
  }, []);

  // 알림 드롭다운이 열릴 때 알림 데이터 새로고침
  useEffect(() => {
    if (showDropdown) {
      fetchNotifications();
    }
  }, [showDropdown]);

  // 외부 클릭 감지
  useEffect(() => {
    const handleOutsideClick = (event) => {
      // 알림 버튼과 더보기 버튼 클릭은 무시
      if (event.target.closest(`.${styles.notificationBell}`) ||
          event.target.closest(`.${styles.moreButton}`)) {
        return;
      }
      // 알림 드롭다운 외부 클릭시 닫기
      if (showDropdown && dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setShowDropdown(false);
      }
      // 더보기 메뉴 외부 클릭시 닫기
      if (showMoreMenu && moreMenuRef.current && !moreMenuRef.current.contains(event.target)) {
        setShowMoreMenu(false);
      }
    };

    document.addEventListener("click", handleOutsideClick);
    return () => {
      document.removeEventListener("click", handleOutsideClick);
    };
  }, [showDropdown, showMoreMenu]);

  const toggleDropdown = () => {
    setShowMoreMenu(false);
    setShowDropdown((prev) => !prev);
  };

  const toggleMoreMenu = () => {
    setShowDropdown(false);
    setShowMoreMenu((prev) => !prev);
  };

  const handleSidebarToggle = () => {
    onSidebarToggle();
    setShowDropdown(false);
    setShowMoreMenu(false);
    if (onCloseSidebarPopups) {
      setTimeout(() => {
        onCloseSidebarPopups();
      }, 0);
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
        fetchNotifications();
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
    // 현재 경로가 홈페이지('/')인 경우 새로고침
    if (location.pathname === '/') {
      window.location.reload();
    } else {
      navigate('/');
    }
  };

  const handleProfile = () => {
    navigate("/user/profile");
  };

  const handleToggleDropdown = useRef((newRef) => {
    if (openDropdownRef.current && openDropdownRef.current !== newRef) {
      openDropdownRef.current.dispatchEvent(new CustomEvent("closeDropdown"));
    }
    openDropdownRef.current = newRef;
  }).current;

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
                <div className={styles.moreMenu} ref={moreMenuRef}>
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
            <div className={styles.notificationDropdown} ref={dropdownRef}>
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
              <div className={styles.moreMenu} ref={moreMenuRef}>
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
});

const notificationSelectRoot = typeof window !== "undefined"
  ? (document.getElementById("notification-select-root") ||
      (() => {
        const el = document.createElement("div");
        el.id = "notification-select-root";
        document.body.appendChild(el);
        return el;
      })())
  : null;

function MoreActions({ notification, index, onAction, onToggle }) {
  const [open, setOpen] = useState(false);
  const containerRef = useRef(null);
  const selectRef = useRef(null);

  const toggleDropdown = () => {
    setOpen((prev) => !prev);
  };

  useEffect(() => {
    if (open) {
      onToggle(containerRef.current);
    }
  }, [open, onToggle]);

  useEffect(() => {
    const handleOutsideClick = (event) => {
      if (selectRef.current && !selectRef.current.contains(event.target)) {
        setOpen(false);
      }
    };
    if (open) {
      document.addEventListener("mousedown", handleOutsideClick);
    }
    return () => {
      document.removeEventListener("mousedown", handleOutsideClick);
    };
  }, [open]);

  const handleClick = async (action) => {
    try {
      await onAction(notification, action);
      setOpen(false);
    } catch (error) {
      console.error("Action failed:", error);
      alert("요청 처리에 실패했습니다. 다시 시도해주세요.");
    }
  };

  const [coords, setCoords] = useState(null);
  useEffect(() => {
    if (open && containerRef.current) {
      const rect = containerRef.current.getBoundingClientRect();
      setCoords({
        top: rect.bottom,
        left: rect.left + rect.width / 2,
      });
    }
  }, [open]);

  return (
    <div className={styles.notificationContainer} ref={containerRef}>
      <button onClick={toggleDropdown}>더보기</button>
      {open && notificationSelectRoot &&
        createPortal(
          <div
            className={styles.notificationSelect}
            ref={selectRef}
            style={
              coords
                ? {
                    position: "fixed",
                    top: Math.min(coords.top, window.innerHeight - 150),
                    left: Math.min(coords.left, window.innerWidth - 160),
                    transform: "translate(-50%, 8px)",
                  }
                : undefined
            }
          >
            <button onClick={() => handleClick("accept")}>예</button>
            <button onClick={() => handleClick("reject")}>아니오</button>
            {notification.category === 'SCHEDULE' && (
              <button onClick={() => handleClick("maybe")}>미정</button>
            )}
          </div>,
          notificationSelectRoot
        )
      }
    </div>
  );
}

export default HeaderComponent;