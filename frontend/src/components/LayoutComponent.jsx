import React, { useState, useCallback } from "react";
import SidebarComponent from "./SidebarComponent";
import HeaderComponent from "./HeaderComponent";
import CalendarComponent from "./CalendarComponent";
import styles from "../styles/Layout.module.css";

const LayoutComponent = ({ userId }) => {
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [selectedCalendarIds, setSelectedCalendarIds] = useState([]);

  const toggleSidebar = () => setIsSidebarOpen((prev) => !prev);
  const closeSidebar = () => setIsSidebarOpen(false);

  const handleCalendarChange = useCallback((selectedCalendars) => {
    const selectedIds = Object.keys(selectedCalendars);
    setSelectedCalendarIds(selectedIds); // 선택된 목록 업데이트
  }, []);


  return (
    <div className={styles.layout}>
      <HeaderComponent
        onSidebarToggle={toggleSidebar}
        onProfileClick={() => alert("프로필 클릭")}
      />
      <div className={styles.content}>
        <SidebarComponent
          isOpen={isSidebarOpen}
          onClose={closeSidebar}
          onAddCalendar={() => alert("새 캘린더 추가!")}
          onCalendarChange={handleCalendarChange}
          userId={userId}
        />
        <main
          className={`${styles.mainContent} ${isSidebarOpen ? styles.sidebarOpen : ""}`}
        >
          <CalendarComponent selectedCalendarIds={selectedCalendarIds} />
        </main>
      </div>
    </div>
  );
};

export default LayoutComponent;
