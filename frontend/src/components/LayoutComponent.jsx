import React, { useState, useCallback } from "react";
import SidebarComponent from "./SidebarComponent";
import HeaderComponent from "./HeaderComponent";
import CalendarComponent from "./CalendarComponent";
import styles from "styles/Layout.module.css";
import PushNotification from "../PushNotification";

const LayoutComponent = ({ userId }) => {
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [selectedCalendarList, setSelectedCalendars] = useState([]);

  const toggleSidebar = () => setIsSidebarOpen((prev) => !prev);
  const closeSidebar = () => setIsSidebarOpen(false);

  const handleCalendarChange = useCallback((selectedCalendars) => {
    setSelectedCalendars(selectedCalendars); // 선택된 목록 업데이트
  }, []);


  return (
    <div className={styles.layout}>
      <HeaderComponent
        mode="main"
        onSidebarToggle={toggleSidebar}
      />
      <div className={styles.content}>
        <SidebarComponent
          isOpen={isSidebarOpen}
          onClose={closeSidebar}
          selectedCalendarList={selectedCalendarList}
          onCalendarChange={handleCalendarChange}
          userId={userId}
        />
        <main
          className={`${styles.mainContent} ${isSidebarOpen ? styles.sidebarOpen : ""}`}
        >
          <CalendarComponent selectedCalendarList={selectedCalendarList} />
        </main>
      </div>
      <PushNotification />
    </div>
  );
};

export default LayoutComponent;
