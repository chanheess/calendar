import React, { useState, useRef, useEffect, forwardRef, useImperativeHandle } from "react";
import styles from "styles/Sidebar.module.css";
import Button from "./Button";
import CalendarList from "./CalendarList";
import getCalendarList from "./GetCalendarList";
import AddCalendarPopup from "./popups/AddCalendarPopup";
import ManageCalendarPopup from "./popups/ManageCalendarPopup";
import SchedulePopup from "./popups/SchedulePopup";

const SidebarComponent = forwardRef(({
  isOpen,
  onClose,
  selectedCalendarList,
  onCalendarChange,
  userId,
  refreshSchedules, // LayoutComponent에서 전달받은 캘린더 새로고침 콜백
}, ref) => {
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

  // SchedulePopup 상태 추가
  const [schedulePopupVisible, setSchedulePopupVisible] = useState(false);
  const [schedulePopupMode, setSchedulePopupMode] = useState("create");
  const [schedulePopupData, setSchedulePopupData] = useState(null);

  useImperativeHandle(ref, () => ({
    closeAllPopups: () => {
      setAddCalendarPopupVisible(false);
      setManagePopupVisible(false);
      setSchedulePopupVisible(false);
      setDropdownOpen(false);
    }
  }));

  // 바깥 영역 클릭 감지하여 드롭다운 닫기
  useEffect(() => {
    function handleClickOutside(e) {
      if (dropdownRef.current && !dropdownRef.current.contains(e.target)) {
        setDropdownOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  // 캘린더 데이터 로딩
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

  // 캘린더 선택 토글
  const handleCalendarSelection = (id, checked) => {
    const updatedList = {
      ...selectedCalendarList,
      [id]: {
        ...selectedCalendarList[id],
        isSelected: checked,
      },
    };
    onCalendarChange(updatedList);
  };

  // 새 캘린더 추가
  const handleCalendarAdded = (type, newCalendar) => {
    const calendarObj = {
      title: newCalendar.title,
      color: newCalendar.color,
      category: type,
      isSelected: true,
    };
    const allCalendars = { ...myCalendars, ...groupCalendars, [newCalendar.id]: calendarObj };

    if (type === "USER") {
      setMyCalendars((prev) => ({ ...prev, [newCalendar.id]: calendarObj }));
    } else if (type === "GROUP") {
      setGroupCalendars((prev) => ({ ...prev, [newCalendar.id]: calendarObj }));
    }
    onCalendarChange(allCalendars);
  };

  // "일정" 드롭다운 항목 클릭 시: 오늘 날짜 기준으로 SchedulePopup 생성 (일정 생성 팝업 열기)
  const handleCreateSchedule = () => {
    if (typeof onClose === "function") {
      onClose();
    }
    const now = new Date();
    const hh = now.getHours();
    const mm = now.getMinutes();
    const startAt = new Date(now.getFullYear(), now.getMonth(), now.getDate(), hh, mm);
    const endAt = new Date(startAt.getTime() + 60 * 60 * 1000);
    const fmt = (d) => {
      const y = d.getFullYear();
      const m = String(d.getMonth() + 1).padStart(2, "0");
      const dd = String(d.getDate()).padStart(2, "0");
      const h = String(d.getHours()).padStart(2, "0");
      const min = String(d.getMinutes()).padStart(2, "0");
      return `${y}-${m}-${dd}T${h}:${min}`;
    };

    setSchedulePopupData({
      startAt: fmt(startAt),
      endAt: fmt(endAt),
      title: "",
      description: "",
      calendarId: "", // 사용자가 선택하도록 비워둠
    });
    setSchedulePopupMode("create");
    setSchedulePopupVisible(true);
    setDropdownOpen(false);
  };

  const openAddCalendarPopup = () => {
    if (typeof onClose === "function") {
      onClose();
    }
    setAddCalendarPopupVisible(true);
    setDropdownOpen(false);
  };

  const openManageCalendarPopup = (calendar, sectionId) => {
    if (typeof onClose === "function") {
      onClose();
    }
    setManagePopupVisible(true);
    setManagePopupCalendar({ ...calendar, category: sectionId });
  };

  function handleClose() {
    setAddCalendarPopupVisible(false);
    setManagePopupVisible(false);
  }

  // 전체 캘린더 목록
  const calendarsArray = Object.entries(selectedCalendarList || {}).map(
    ([id, data]) => ({
      id,
      ...data,
    })
  );
  const userCalendarsArray = calendarsArray.filter((cal) => cal.category === "USER");
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
      {schedulePopupVisible && (
        <SchedulePopup
          isOpen={schedulePopupVisible}
          mode={schedulePopupMode}
          eventDetails={schedulePopupData}
          onClose={(updated) => {
            setSchedulePopupVisible(false);
            if (updated && typeof refreshSchedules === "function") {
              refreshSchedules();
            }
          }}
          selectedCalendarList={selectedCalendarList}
          currentUserId={userId}
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

        <div className={styles.dropdownWrapper} ref={dropdownRef}>
          <Button
            variant="blue"
            size=""
            onClick={() => setDropdownOpen(!dropdownOpen)}
          >
            +
          </Button>
          {dropdownOpen && (
            <div className={styles.dropdownMenu}>
              <div className={styles.dropdownItem} onClick={handleCreateSchedule}>
                일정
              </div>
              <div className={styles.dropdownItem} onClick={openAddCalendarPopup}>
                캘린더
              </div>
            </div>
          )}
        </div>
      </div>
    </>
  );
});

export default SidebarComponent;