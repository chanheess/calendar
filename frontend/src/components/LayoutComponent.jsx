import React, { useState, useCallback } from "react";
import SidebarComponent from "./SidebarComponent";
import HeaderComponent from "./HeaderComponent";
import CalendarComponent from "./CalendarComponent";
import styles from "styles/Layout.module.css";
import PushNotification from "../PushNotification";

const LayoutComponent = ({ userId }) => {
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [selectedCalendarList, setSelectedCalendars] = useState({});
  const [refreshKey, setRefreshKey] = useState(0);

  const toggleSidebar = () => setIsSidebarOpen((prev) => !prev);
  const closeSidebar = () => setIsSidebarOpen(false);

  const handleCalendarChange = useCallback((selectedCalendars) => {
    setSelectedCalendars(selectedCalendars);
  }, []);

  const refreshSchedules = useCallback(() => {
    setRefreshKey((prev) => prev + 1);
  }, []);

  return (
    <div className={styles.layout}>
      <HeaderComponent mode="main" onSidebarToggle={toggleSidebar} />
      <div className={styles.content}>
        <SidebarComponent
          isOpen={isSidebarOpen}
          onClose={closeSidebar}
          selectedCalendarList={selectedCalendarList}
          onCalendarChange={handleCalendarChange}
          userId={userId}
          refreshSchedules={refreshSchedules}  // Sidebar에서 일정 생성 후 호출
        />
        <main className={`${styles.mainContent} ${isSidebarOpen ? styles.sidebarOpen : ""}`}>
          <CalendarComponent
            selectedCalendarList={selectedCalendarList}
            refreshKey={refreshKey}
            refreshSchedules={refreshSchedules}
          />
        </main>
      </div>
      <PushNotification />
    </div>
  );
};

export default LayoutComponent;