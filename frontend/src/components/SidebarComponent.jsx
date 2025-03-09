import React, { useState, useEffect } from "react";
import styles from "styles/Sidebar.module.css";
import Button from "./Button";
import CalendarList from "./CalendarList";
import getCalendarList from "./GetCalendarList";
import AddCalendarPopup from "./popups/AddCalendarPopup";
import ManageCalendarPopup from "./popups/ManageCalendarPopup";

const SidebarComponent = ({ isOpen, onClose, selectedCalendarList, onCalendarChange }) => {
  // 최초 데이터 로딩을 위한 로컬 상태 (초기 로딩 시 사용)
  const [myCalendars, setMyCalendars] = useState({});
  const [groupCalendars, setGroupCalendars] = useState({});
  const [managePopupCalendar, setManagePopupCalendar] = useState({ title: "", id: "", color: "", category: "" });
  const [addCalendarPopupVisible, setAddCalendarPopupVisible] = useState(false);
  const [managePopupVisible, setManagePopupVisible] = useState(false);

  // 최초 로딩 시 캘린더에 isSelected: true, 그리고 category를 추가
  useEffect(() => {
    const fetchAllCalendars = async () => {
      try {
        const [userCalendars, groupCalendars] = await Promise.all([
          getCalendarList("USER"),
          getCalendarList("GROUP"),
        ]);

        const userCalWithSelected = Object.entries(userCalendars).reduce((acc, [id, data]) => {
          acc[id] = { ...data, isSelected: true, category: "USER" };
          return acc;
        }, {});
        const groupCalWithSelected = Object.entries(groupCalendars).reduce((acc, [id, data]) => {
          acc[id] = { ...data, isSelected: true, category: "GROUP" };
          return acc;
        }, {});

        setMyCalendars(userCalWithSelected);
        setGroupCalendars(groupCalWithSelected);

        const merged = { ...userCalWithSelected, ...groupCalWithSelected };
        onCalendarChange(merged);
      } catch (error) {
        console.error("Error fetching calendars:", error);
      }
    };

    fetchAllCalendars();
  }, [onCalendarChange]);

  // 체크박스 선택 시, 선택 상태(isSelected)를 토글하는 함수
  const handleCalendarSelection = (id, checked) => {
    // 모든 캘린더를 통합한 상태
    const allCalendars = { ...myCalendars, ...groupCalendars };
    if (allCalendars[id]) {
      const updatedCalendar = { ...allCalendars[id], isSelected: checked };
      const updatedList = { ...allCalendars, [id]: updatedCalendar };
      onCalendarChange(updatedList);
    }
  };

  const handleCalendarAdded = (type, newCalendar) => {
    const calendarObj = { title: newCalendar.title, color: newCalendar.color, category: type, isSelected: true };
    const allCalendars = { ...myCalendars, ...groupCalendars, [newCalendar.id]: calendarObj };

    if (type === "USER") {
      setMyCalendars((prev) => ({ ...prev, [newCalendar.id]: calendarObj }));
    } else if (type === "GROUP") {
      setGroupCalendars((prev) => ({ ...prev, [newCalendar.id]: calendarObj }));
    }
    onCalendarChange(allCalendars);
  };

  function openAddCalendarPopup() {
    setAddCalendarPopupVisible(true);
  }

  // 관리 팝업 열 때, sectionId (USER/GROUP)도 함께 설정
  const openManageCalendarPopup = (calendar, sectionId) => {
    setManagePopupVisible(true);
    setManagePopupCalendar({ ...calendar, category: sectionId });
  };

  function handleClose() {
    setAddCalendarPopupVisible(false);
    setManagePopupVisible(false);
  }

  // 전체 캘린더 목록은 부모의 selectedCalendarList를 그대로 사용
  const calendarsArray = Object.entries(selectedCalendarList || {}).map(([id, data]) => ({
    id,
    ...data,
  }));

  // 그룹과 개인을 구분하여 전달
  const userCalendars = calendarsArray.filter((cal) => cal.category === "USER");
  const groupCalendarsArray = calendarsArray.filter((cal) => cal.category === "GROUP");

  return (
    <>
      {addCalendarPopupVisible && (
        <AddCalendarPopup
          isOpen={addCalendarPopupVisible}
          onClose={handleClose}
          onCalendarAdded={handleCalendarAdded}
        />
      )}
      {managePopupVisible && (
        <ManageCalendarPopup
          isOpen={managePopupVisible}
          onClose={handleClose}
          calendarInfo={managePopupCalendar}
          selectedCalendarList={selectedCalendarList}
          onCalendarChange={onCalendarChange}
        />
      )}
      <div className={`${styles.sidebar} ${isOpen ? styles.open : ""}`} role="navigation">
        <CalendarList
          title="내 캘린더"
          calendars={userCalendars}
          sectionId="USER"
          onCalendarSelection={handleCalendarSelection}
          onManageClick={openManageCalendarPopup}
        />
        <CalendarList
          title="그룹 캘린더"
          calendars={groupCalendarsArray}
          sectionId="GROUP"
          onCalendarSelection={handleCalendarSelection}
          onManageClick={openManageCalendarPopup}
        />
        <Button variant="blue" size="medium" onClick={openAddCalendarPopup}>
          +
        </Button>
      </div>
    </>
  );
};

export default SidebarComponent;