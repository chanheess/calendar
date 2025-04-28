import React, { useState, useCallback, useRef } from "react";
import SidebarComponent from "./SidebarComponent";
import HeaderComponent from "./HeaderComponent";
import CalendarComponent from "./CalendarComponent";
import styles from "styles/Layout.module.css";
import PushNotification from "../PushNotification";

const LayoutComponent = ({ userId }) => {
  const sidebarRef = useRef(null);
  const calendarRef = useRef(null);

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

  const handleCloseSidebarPopups = () => {
    if (sidebarRef.current && typeof sidebarRef.current.closeAllPopups === "function") {
      sidebarRef.current.closeAllPopups();
    }
    if (calendarRef.current && typeof calendarRef.current.closeAllPopups === "function") {
      calendarRef.current.closeAllPopups();
    }
  };

  return (
    <div className={styles.layout}>
      <HeaderComponent
        mode="main"
        onSidebarToggle={toggleSidebar}
        onCloseSidebarPopups={handleCloseSidebarPopups}
      />
      <div className={styles.content}>
        <SidebarComponent
          ref={sidebarRef}
          isOpen={isSidebarOpen}
          onClose={closeSidebar}
          selectedCalendarList={selectedCalendarList}
          onCalendarChange={handleCalendarChange}
          userId={userId}
          refreshSchedules={refreshSchedules}
        />
        {isSidebarOpen && (
          <div
            className={styles.dimmedOverlay}
            onClick={closeSidebar}
          />
        )}
        <main className={`${styles.mainContent} ${isSidebarOpen ? styles.sidebarOpen : ""}`}>
          <CalendarComponent
            ref={calendarRef}
            selectedCalendarList={selectedCalendarList}
            refreshKey={refreshKey}
            refreshSchedules={refreshSchedules}
            onCloseAllPopups={handleCloseSidebarPopups}
          />
        </main>
      </div>
      <PushNotification />
    </div>
  );
};

export default LayoutComponent;