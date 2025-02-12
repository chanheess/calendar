import React, { useState } from "react";
import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import timeGridPlugin from "@fullcalendar/timegrid";
import interactionPlugin from "@fullcalendar/interaction";

import axios from "axios";
import styles from "styles/Calendar.module.css";
import Popup from "./popups/Popup";
import SchedulePopup from "./popups/SchedulePopup";

const CalendarComponent = ({ selectedCalendarList }) => {
  const [popupVisible, setPopupVisible] = useState(false);
  const [popupData, setPopupData] = useState(null);
  const [popupTitle, setPopupTitle] = useState("");

  const [schedulePopupVisible, setSchedulePopupVisible] = useState(false);
  const [schedulePopupMode, setSchedulePopupMode] = useState("create");
  const [schedulePopupData, setSchedulePopupData] = useState(null);


  // Fetch events for the calendar
  const fetchEvents = async (fetchInfo, successCallback, failureCallback) => {
    const params = new URLSearchParams({
      start: fetchInfo.start.toISOString().split("T")[0],
      end: fetchInfo.end.toISOString().split("T")[0],
    });

    try {
      const response = await axios.get(`/schedules/date?${params.toString()}`, {
        withCredentials: true,
        headers: { "Content-Type": "application/json" },
      });

      const data = response.data;
      let events = [];

      if (!selectedCalendarList || Object.keys(selectedCalendarList).length === 0) {
        successCallback([]);
        return;
      }

      Object.entries(selectedCalendarList).forEach(([calendarId, calendar]) => {
        if (data[calendarId]) {
          events.push(
            ...data[calendarId].map((event) => ({
              id: event.id,
              title: event.title,
              start: event.startAt,
              end: event.endAt,
              description: event.description,
              repeatId: event.repeatId,
              userId: event.userId,
              calendarId: event.calendarId,
            }))
          );
        }
      });

      successCallback(events);
    } catch (error) {
      failureCallback(error);
    }
  };


  // Date click event handler (기존 팝업 표시)
  const dateClickEvent = async (info) => {
    const selectedDate = info.dateStr;

    try {
      // Fetch events for the clicked date
      const response = await axios.get(`/schedules/date?start=${selectedDate}&end=${selectedDate}`, {
        withCredentials: true,
      });

      const data = response.data;
      const events = [];

      for (const [calendarId, eventList] of Object.entries(data)) {
        eventList.forEach((event) => {
          events.push({ calendarId, ...event });
        });
      }

      setPopupTitle(`Events on ${selectedDate}`);
      setPopupData(events);
      setPopupVisible(true);
    } catch (error) {
      console.error("Error fetching events:", error);
      setPopupTitle(`Events on ${selectedDate}`);
      setPopupData([{ title: "Error loading events" }]);
      setPopupVisible(true);
    }
  };

  // Handle event click in the list (새 팝업 edit 모드로 열기)
  const handleEventClick = (eventData) => {
    setSchedulePopupMode("edit");
    setSchedulePopupData(eventData);
    setSchedulePopupVisible(true);
  };

  // Open the new popup in create mode
  const openCreatePopup = (selectedDate) => {
    const now = new Date(); // 현재 시간 가져오기
    const currentHours = now.getHours();
    const currentMinutes = now.getMinutes();

    const selectedDateObj = selectedDate
      ? new Date(selectedDate) // 선택한 날짜가 있을 경우 해당 날짜 사용
      : new Date(now.getFullYear(), now.getMonth(), now.getDate()); // 선택한 날짜가 없을 경우 오늘 날짜

    const startAt = new Date(
      selectedDateObj.getFullYear(),
      selectedDateObj.getMonth(),
      selectedDateObj.getDate(),
      currentHours,
      currentMinutes
    );

    const endAt = new Date(startAt.getTime() + 60 * 60 * 1000); // 시작 시간 + 1시간

    const formatDateTime = (date) => {
      const year = date.getFullYear();
      const month = String(date.getMonth() + 1).padStart(2, "0");
      const day = String(date.getDate()).padStart(2, "0");
      const hours = String(date.getHours()).padStart(2, "0");
      const minutes = String(date.getMinutes()).padStart(2, "0");
      return `${year}-${month}-${day}T${hours}:${minutes}`;
    };

    setSchedulePopupMode("create");
    setSchedulePopupData({
      startAt: formatDateTime(startAt), // 시작 시간 설정
      endAt: formatDateTime(endAt), // 종료 시간 설정
      title: "",
      description: "",
      calendarId: "",
    });

    setSchedulePopupVisible(true); // 팝업 열기
  };

  // Close both popups
  const closeAllPopups = () => {
    setPopupVisible(false);
    setSchedulePopupVisible(false);
    setPopupData(null);
    setSchedulePopupData(null);
  };

  return (
    <div className={styles.calendarContainer}>
      <FullCalendar
        plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin]}
        initialView="dayGridMonth"
        editable={true}
        aspectRatio={1.3}
        height="100%"
        headerToolbar={{
          left: "prev,next today",
          center: "title",
          right: "dayGridMonth,timeGridWeek,timeGridDay",
        }}
        events={fetchEvents}
        dateClick={dateClickEvent}
        dayMaxEventRows={true}  // 날짜 칸 내에서 이벤트 개수 제한
        fixedWeekCount={false}
      />

      {popupVisible && (
        <Popup
          title={popupTitle}
          onClose={closeAllPopups}
          actions={[
            {
              label: "New Event",
              variant: "green",
              size: "medium",
              onClick: () => {
                const selectedDate = popupTitle.includes("Events on ")
                  ? popupTitle.split("Events on ")[1]
                  : new Date().toISOString().split("T")[0]; // 기본값 설정
                openCreatePopup(selectedDate);
              },
            },
            {
              label: "Close",
              variant: "logout",
              size: "medium",
              onClick: closeAllPopups,
            },
          ]}
        >
          {popupData && popupData.length > 0 ? (
            <ul>
              {popupData.map((event, index) => (
                <li key={index} onClick={() => handleEventClick(event)}>
                  <strong>{event.title}</strong>
                  {event.description && <p>{event.description}</p>}
                  <p>
                    {event.startAt} - {event.endAt}
                  </p>
                </li>
              ))}
            </ul>
          ) : (
            <p>No events available.</p>
          )}
        </Popup>
      )}

      {schedulePopupVisible && (
        <SchedulePopup
          isOpen={schedulePopupVisible}
          mode={schedulePopupMode}
          eventDetails={schedulePopupData}
          onClose={closeAllPopups}
          selectedCalendarList={selectedCalendarList}
        />
      )}
    </div>
  );
};

export default CalendarComponent;
