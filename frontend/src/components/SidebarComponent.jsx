import React, { useState, useRef, useEffect, forwardRef, useImperativeHandle } from "react";
import styles from "styles/Sidebar.module.css";
import Button from "./Button";
import CalendarList from "./CalendarList";
import getCalendarList from "./GetCalendarList";
import AddCalendarPopup from "./popups/AddCalendarPopup";
import ManageCalendarPopup from "./popups/ManageCalendarPopup";
import SchedulePopup from "./popups/SchedulePopup";
import axios from 'utils/axiosInstance';

const SidebarComponent = forwardRef(({
  isOpen,
  onClose,
  selectedCalendarList,
  onCalendarChange,
  userId,
  refreshSchedules, // LayoutComponent에서 전달받은 캘린더 새로고침 콜백
  onCloseSidebarPopups
}, ref) => {
  const [myCalendars, setMyCalendars] = useState({});
  const [groupCalendars, setGroupCalendars] = useState({});
  const [googleCalendars, setGoogleCalendars] = useState({});
  const [isGoogleLinked, setIsGoogleLinked] = useState(false);
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

  // 구글 캘린더 연동 상태 확인
  const checkGoogleCalendarStatus = async () => {
    try {
      // 구글 연동 상태 확인
      const providerResponse = await axios.get("/check/provider", {
        withCredentials: true,
        headers: { "Content-Type": "application/json" },
      });
      
      const isLinked = providerResponse.data.some(
        provider => provider.provider.toLowerCase() === "google"
      );
      setIsGoogleLinked(isLinked);

      // 구글 연동이 되어 있다면 구글 캘린더 목록 가져오기
      if (isLinked) {
        try {
          const googleCalendarsResponse = await axios.get("/calendars", {
            params: { category: "GOOGLE" },
            withCredentials: true,
            headers: { "Content-Type": "application/json" },
          });

          const googleCalWithSelected = googleCalendarsResponse.data.reduce(
            (acc, calendar) => {
              acc[calendar.id] = { 
                title: calendar.title, 
                color: calendar.color, 
                category: "GOOGLE",
                isSelected: calendar.checked !== false,
                fileAuthority: calendar.fileAuthority
              };
              return acc;
            },
            {}
          );
          setGoogleCalendars(googleCalWithSelected);
        } catch (error) {
          console.error("Error fetching Google calendars:", error);
          setGoogleCalendars({});
        }
      } else {
        setGoogleCalendars({});
      }
    } catch (error) {
      console.error("Error checking Google calendar status:", error);
      setIsGoogleLinked(false);
      setGoogleCalendars({});
    }
  };

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
            acc[id] = { ...data, category: "USER" };
            return acc;
          },
          {}
        );
        const groupCalWithSelected = Object.entries(groupCalendars).reduce(
          (acc, [id, data]) => {
            acc[id] = { ...data, category: "GROUP" };
            return acc;
          },
          {}
        );

        setMyCalendars(userCalWithSelected);
        setGroupCalendars(groupCalWithSelected);

        // 구글 캘린더 상태 확인
        await checkGoogleCalendarStatus();
      } catch (error) {
        console.error("Error fetching calendars:", error);
      }
    };

    fetchAllCalendars();
  }, []);

  useEffect(() => {
    const merged = { ...myCalendars, ...groupCalendars, ...googleCalendars };
    onCalendarChange(merged);
  }, [myCalendars, groupCalendars, googleCalendars, onCalendarChange]);

  // 캘린더 선택 토글
  const handleCalendarSelection = async (id, checked) => {
    const calendar = selectedCalendarList[id];
    // 구글캘린더라면 googleCalendars 상태도 직접 갱신
    if (calendar && calendar.category === "GOOGLE") {
      setGoogleCalendars((prev) => ({
        ...prev,
        [id]: {
          ...prev[id],
          isSelected: checked,
        },
      }));
    }
    // 일반/그룹 캘린더라면 myCalendars/groupCalendars도 직접 갱신
    if (calendar && calendar.category === "USER") {
      setMyCalendars((prev) => ({
        ...prev,
        [id]: {
          ...prev[id],
          isSelected: checked,
        },
      }));
    }
    if (calendar && calendar.category === "GROUP") {
      setGroupCalendars((prev) => ({
        ...prev,
        [id]: {
          ...prev[id],
          isSelected: checked,
        },
      }));
    }
    // onCalendarChange로도 즉시 반영
    const updatedList = {
      ...selectedCalendarList,
      [id]: {
        ...selectedCalendarList[id],
        isSelected: checked,
      },
    };
    onCalendarChange(updatedList);
    try {
      if (calendar) {
        await axios.patch(`/calendars/${id}`, {
          checked: checked,
          category: calendar.category
        }, {
          withCredentials: true,
          headers: { "Content-Type": "application/json" },
        });
      }
    } catch (error) {
      console.error("Error updating calendar selection:", error);
      // 에러 발생 시 원래 상태로 되돌리기 (구글/일반/그룹 모두)
      if (calendar && calendar.category === "GOOGLE") {
        setGoogleCalendars((prev) => ({
          ...prev,
          [id]: {
            ...prev[id],
            isSelected: !checked,
          },
        }));
      }
      if (calendar && calendar.category === "USER") {
        setMyCalendars((prev) => ({
          ...prev,
          [id]: {
            ...prev[id],
            isSelected: !checked,
          },
        }));
      }
      if (calendar && calendar.category === "GROUP") {
        setGroupCalendars((prev) => ({
          ...prev,
          [id]: {
            ...prev[id],
            isSelected: !checked,
          },
        }));
      }
      const revertedList = {
        ...selectedCalendarList,
        [id]: {
          ...selectedCalendarList[id],
          isSelected: !checked,
        },
      };
      onCalendarChange(revertedList);
    }
  };

  // 새 캘린더 추가
  const handleCalendarAdded = (type, newCalendar) => {
    const calendarObj = {
      title: newCalendar.title,
      color: newCalendar.color,
      category: type,
      isSelected: true
    };
    const allCalendars = { ...myCalendars, ...groupCalendars, [newCalendar.id]: calendarObj };

    if (type === "USER") {
      setMyCalendars((prev) => ({ ...prev, [newCalendar.id]: calendarObj }));
    } else if (type === "GROUP") {
      setGroupCalendars((prev) => ({ ...prev, [newCalendar.id]: calendarObj }));
    }
    onCalendarChange(allCalendars);
  };

  // 캘린더 관리 팝업 열기
  const openManageCalendarPopup = (calendar, sectionId) => {
    setManagePopupCalendar({ ...calendar, category: sectionId });
    setManagePopupVisible(true);
  };

  // 일정 생성 팝업 열기
  const handleCreateSchedule = () => {
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
      calendarId: "",
    });
    setSchedulePopupMode("create");
    setSchedulePopupVisible(true);
    setDropdownOpen(false);
  };

  // 캘린더 추가 팝업 열기
  const openAddCalendarPopup = () => {
    setAddCalendarPopupVisible(true);
    setDropdownOpen(false);
  };

  // 팝업 닫기 핸들러
  const handlePopupClose = (shouldRefresh = false) => {
    setAddCalendarPopupVisible(false);
    setManagePopupVisible(false);
    setSchedulePopupVisible(false);
    if (shouldRefresh && typeof refreshSchedules === "function") {
      refreshSchedules();
    }
  };

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
          onClose={handlePopupClose}
          onCalendarAdded={handleCalendarAdded}
        />
      )}
      {managePopupVisible && (
        <ManageCalendarPopup
          isOpen={managePopupVisible}
          onClose={handlePopupClose}
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
            handlePopupClose(updated);
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
        {isGoogleLinked && (
        <CalendarList
          title="구글 캘린더"
            calendars={Object.entries(googleCalendars).map(([id, calendar]) => ({
              id,
              ...calendar,
            }))}
            sectionId="GOOGLE"
          onCalendarSelection={handleCalendarSelection}
          onManageClick={openManageCalendarPopup}
        />
        )}

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