import React, { useState, useCallback, useRef } from "react";
import SidebarComponent from "./SidebarComponent";
import HeaderComponent from "./HeaderComponent";
import CalendarComponent from "./CalendarComponent";
import styles from "styles/Layout.module.css";
import PushNotification from "../PushNotification";

const LayoutComponent = ({ userId }) => {
  const sidebarRef = useRef(null);
  const calendarRef = useRef(null);
  const headerRef = useRef(null);

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

  const handleCloseSidebarPopups = useCallback(() => {
    // 각 컴포넌트의 팝업을 순차적으로 닫기
    setTimeout(() => {
      if (sidebarRef.current?.closeAllPopups) {
        sidebarRef.current.closeAllPopups();
      }
    }, 0);

    setTimeout(() => {
      if (calendarRef.current?.closeAllPopups) {
        calendarRef.current.closeAllPopups();
      }
    }, 0);
  }, []);

  return (
    <div className={styles.layout}>
      <HeaderComponent
        ref={headerRef}
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
          onCloseSidebarPopups={handleCloseSidebarPopups}
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