import React, { useState, useEffect } from "react";
import styles from "../styles/Sidebar.module.css";
import Button from "./Button";
import CalendarList from "./CalendarList";
import getCalendarList from "./GetCalendarList";

const SidebarComponent = ({ isOpen, onClose, onAddCalendar, onCalendarChange }) => {
  const [myCalendars, setMyCalendars] = useState({});
  const [groupCalendars, setGroupCalendars] = useState({});
  const [selectedCalendars, setSelectedCalendars] = useState({}); // 선택된 캘린더 데이터 저장

  useEffect(() => {
    const fetchAllCalendars = async () => {
      try {
        const [userCalendars, groupCalendars] = await Promise.all([
          getCalendarList("USER"),
          getCalendarList("GROUP"),
        ]);
        setMyCalendars(userCalendars);
        setGroupCalendars(groupCalendars);

        // 초기 상태: 모든 캘린더를 기본 선택 상태로 설정
        setSelectedCalendars({ ...userCalendars, ...groupCalendars });
        onCalendarChange({ ...userCalendars, ...groupCalendars }); // 부모 컴포넌트로 전달
      } catch (error) {
        console.error("Error fetching calendars:", error);
      }
    };
    fetchAllCalendars();
  }, [onCalendarChange]);

  const handleCalendarSelection = (updatedSelectedIds) => {
    const allCalendars = { ...myCalendars, ...groupCalendars };

    // 선택된 캘린더 ID를 기반으로 필터링된 데이터 생성
    const updatedSelectedCalendars = updatedSelectedIds.reduce((acc, id) => {
      if (allCalendars[id]) acc[id] = allCalendars[id];
      return acc;
    }, {});

    setSelectedCalendars(updatedSelectedCalendars); // 선택된 캘린더 상태 업데이트
    onCalendarChange(updatedSelectedCalendars); // 부모 컴포넌트로 전달
  };

  return (
    <div className={`${styles.sidebar} ${isOpen ? styles.open : ""}`} role="navigation">
      <CalendarList
        title="내 캘린더"
        calendars={Object.entries(myCalendars).map(([id, title]) => ({ id, title }))}
        sectionId="myCalendarList"
        onCalendarSelection={handleCalendarSelection}
        selectedIds={Object.keys(selectedCalendars)}
      />
      <CalendarList
        title="그룹 캘린더"
        calendars={Object.entries(groupCalendars).map(([id, title]) => ({ id, title }))}
        sectionId="groupCalendarList"
        onCalendarSelection={handleCalendarSelection}
        selectedIds={Object.keys(selectedCalendars)}
      />
      <Button id="addCalendarButton" variant="blue" size="medium" onClick={onAddCalendar}>
        +
      </Button>
    </div>
  );
};

export default SidebarComponent;
