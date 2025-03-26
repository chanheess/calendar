import React, { useState, useEffect, useRef } from "react";
import styles from "styles/Sidebar.module.css";
import Button from "./Button";
import CalendarList from "./CalendarList";
import getCalendarList from "./GetCalendarList";
import AddCalendarPopup from "./popups/AddCalendarPopup";
import ManageCalendarPopup from "./popups/ManageCalendarPopup";

const SidebarComponent = ({
  isOpen,
  onClose,
  selectedCalendarList,
  onCalendarChange,
}) => {
  const [myCalendars, setMyCalendars] = useState({});
  const [groupCalendars, setGroupCalendars] = useState({});
  const [managePopupCalendar, setManagePopupCalendar] = useState({
    title: "",
    id: "",
    color: "",
    category: "",
  });
  const [addCalendarPopupVisible, setAddCalendarPopupVisible] = useState(false);
  const [managePopupVisible, setManagePopupVisible] = useState(false);
  const [dropdownOpen, setDropdownOpen] = useState(false);
  const dropdownRef = useRef(null);

  // 바깥 영역 클릭 감지
  useEffect(() => {
    function handleClickOutside(e) {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(e.target)
      ) {
        setDropdownOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  // --- 초기 로딩: USER / GROUP 캘린더 불러오기 ---
  useEffect(() => {
    const fetchAllCalendars = async () => {
      try {
        const [userCalendars, groupCalendars] = await Promise.all([
          getCalendarList("USER"),
          getCalendarList("GROUP"),
        ]);

        const userCalWithSelected = Object.entries(userCalendars).reduce(
          (acc, [id, data]) => {
            acc[id] = { ...data, isSelected: true, category: "USER" };
            return acc;
          },
          {}
        );
        const groupCalWithSelected = Object.entries(groupCalendars).reduce(
          (acc, [id, data]) => {
            acc[id] = { ...data, isSelected: true, category: "GROUP" };
            return acc;
          },
          {}
        );

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

  // 체크박스 선택 (캘린더 보이기/숨기기)
  const handleCalendarSelection = (id, checked) => {
    const allCalendars = { ...myCalendars, ...groupCalendars };
    if (allCalendars[id]) {
      const updatedCalendar = { ...allCalendars[id], isSelected: checked };
      const updatedList = { ...allCalendars, [id]: updatedCalendar };
      onCalendarChange(updatedList);
    }
  };

  // 새 캘린더 추가
  const handleCalendarAdded = (type, newCalendar) => {
    const calendarObj = {
      title: newCalendar.title,
      color: newCalendar.color,
      category: type,
      isSelected: true,
    };
    const allCalendars = {
      ...myCalendars,
      ...groupCalendars,
      [newCalendar.id]: calendarObj,
    };

    if (type === "USER") {
      setMyCalendars((prev) => ({ ...prev, [newCalendar.id]: calendarObj }));
    } else if (type === "GROUP") {
      setGroupCalendars((prev) => ({ ...prev, [newCalendar.id]: calendarObj }));
    }
    onCalendarChange(allCalendars);
  };

  // --- (2) 일정 생성 로직(예시) ---
  // 실제 일정 생성 팝업이나 로직을 연결
  const handleCreateSchedule = () => {
    alert("일정(이벤트) 생성 로직을 여기서 실행하세요!");
    // 예: openSchedulePopup();
    // 또는 라우팅, etc...
    // ...
    setDropdownOpen(false);
  };

  // 캘린더 추가 팝업 열기
  const openAddCalendarPopup = () => {
    setAddCalendarPopupVisible(true);
    setDropdownOpen(false);
  };

  // 캘린더 관리 팝업 열기
  const openManageCalendarPopup = (calendar, sectionId) => {
    setManagePopupVisible(true);
    setManagePopupCalendar({ ...calendar, category: sectionId });
  };

  // 팝업 닫기
  function handleClose() {
    setAddCalendarPopupVisible(false);
    setManagePopupVisible(false);
  }

  // 전체 캘린더
  const calendarsArray = Object.entries(selectedCalendarList || {}).map(
    ([id, data]) => ({
      id,
      ...data,
    })
  );
  const userCalendarsArray = calendarsArray.filter(
    (cal) => cal.category === "USER"
  );
  const groupCalendarsArray = calendarsArray.filter(
    (cal) => cal.category === "GROUP"
  );

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
          calendars={userCalendarsArray}
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

        <div ref={dropdownRef}>
          <Button
            variant="blue"
            size=""
            onClick={() => setDropdownOpen(!dropdownOpen)}
          >
            +
          </Button>

          {dropdownOpen && (
            <div className={styles.dropdownMenu}>
              <div
                className={styles.dropdownItem}
                onClick={handleCreateSchedule}
              >
                일정
              </div>
              <div
                className={styles.dropdownItem}
                onClick={openAddCalendarPopup}
              >
                캘린더
              </div>
            </div>
          )}
        </div>
      </div>
    </>
  );
};

export default SidebarComponent;