import React, { useState, useRef, useEffect } from "react";
import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import timeGridPlugin from "@fullcalendar/timegrid";
import interactionPlugin from "@fullcalendar/interaction";
import axios from "axios";

import styles from "styles/Calendar.module.css";
import Popup from "./popups/Popup";
import SchedulePopup from "./popups/SchedulePopup";

import LoadingOverlay from "components/LoadingOverlay";

const CalendarComponent = ({ selectedCalendarList }) => {
  // 팝업 / 스케줄 팝업 상태
  const [popupVisible, setPopupVisible] = useState(false);
  const [popupData, setPopupData] = useState([]);
  const [popupTitle, setPopupTitle] = useState("");

  const [schedulePopupVisible, setSchedulePopupVisible] = useState(false);
  const [schedulePopupMode, setSchedulePopupMode] = useState("create");
  const [schedulePopupData, setSchedulePopupData] = useState(null);

  // 이벤트 / 로딩 / "더보기" 상태
  const [fetchEvents, setFetchEvents] = useState([]);   // FullCalendar에 표시할 최종 이벤트
  const [isLoading, setIsLoading] = useState(false);
  const [autoLoadComplete, setAutoLoadComplete] = useState(false);
  const [refreshKey, setRefreshKey] = useState(0);

  // 캐시 / 커서 / 이전 범위 / 중복 fetch 방지
  const eventCacheRef = useRef([]);
  const cursorTimeRef = useRef("");
  const cursorIdRef = useRef(null);
  const prevStartRef = useRef(null);
  const prevEndRef = useRef(null);
  const isFetchingRef = useRef(false); // 중복 fetch 방지
  const calendarRef = useRef(null);

  const pageSize = 1000;
  const maxLoops = 10;

  // selectedCalendarList 변경 시 캐시/상태 초기화
  useEffect(() => {
    // 캐시 및 커서, 이전 범위, "더보기" 상태 모두 리셋
    eventCacheRef.current = [];
    cursorTimeRef.current = "";
    cursorIdRef.current = null;
    prevStartRef.current = null;
    prevEndRef.current = null;
    setFetchEvents([]);
    setAutoLoadComplete(false);

    // 이미 Calendar가 마운트되어 있다면, 현재 뷰 범위로 재로드
    if (calendarRef.current) {
      const calApi = calendarRef.current.getApi();
      const v = calApi.view;
      if (v.activeStart && v.activeEnd) {
        loadEvents(v.activeStart, v.activeEnd, true);
      }
    }

  }, [selectedCalendarList, refreshKey]);

  const handleDatesSet = (arg) => {
    const { start, end } = arg;
    if (
      !prevStartRef.current ||
      start.getTime() !== prevStartRef.current.getTime() ||
      !prevEndRef.current ||
      end.getTime() !== prevEndRef.current.getTime()
    ) {
      eventCacheRef.current = [];
      cursorTimeRef.current = "";
      cursorIdRef.current = null;
      setFetchEvents([]);
      setAutoLoadComplete(false);

      // 새로운 범위 기록
      prevStartRef.current = start;
      prevEndRef.current = end;

      // 해당 범위 일정 자동 로드
      loadEvents(start, end, true);
    }
  };

  const loadEvents = async (startDateObj, endDateObj, firstLoad) => {
    if (!selectedCalendarList || Object.keys(selectedCalendarList).length === 0) {
      setFetchEvents([]);
      return;
    }

    if (isFetchingRef.current) return;
    isFetchingRef.current = true;
    setIsLoading(true);

    try {
      const startStr = startDateObj.toISOString().split("T")[0];
      const endStr = endDateObj.toISOString().split("T")[0];

      let loopCount = 0;
      let keepLoading = true;

      // firstLoad 시에는 최대 maxLoops번 반복, 더보기 시에는 1번만
      const loopLimit = firstLoad ? maxLoops : 1;

      while (keepLoading && loopCount < loopLimit) {
        loopCount++;

        const params = new URLSearchParams({
          start: startStr,
          end: endStr,
          size: pageSize,
        });
        if (cursorTimeRef.current && cursorTimeRef.current !== "null" && cursorTimeRef.current !== "undefined") {
          params.append("cursor-time", cursorTimeRef.current);
        }
        if (cursorIdRef.current !== null) {
          params.append("cursor-id", cursorIdRef.current);
        }

        const res = await axios.get(`/schedules/date?${params.toString()}`, {
          withCredentials: true,
          headers: { "Content-Type": "application/json" },
        });
        const data = res.data;

        // nextCursor 갱신
        if (data.nextCursor && data.nextCursor !== "null" && data.nextCursor !== "undefined") {
          cursorTimeRef.current = data.nextCursor;
        } else {
          cursorTimeRef.current = "";
        }

        // 마지막 이벤트 ID
        if (data.content && data.content.length > 0) {
          const lastEvent = data.content[data.content.length - 1];
          cursorIdRef.current = lastEvent.id;
        } else {
          cursorIdRef.current = null;
        }

        const newEvents = (data.content || []).map((evt) => ({
          id: evt.id,
          title: evt.title,
          start: evt.startAt,
          end: evt.endAt,
          description: evt.description,
          repeatId: evt.repeatId,
          userId: evt.userId,
          calendarId: evt.calendarId,
        }));

        const filtered = newEvents.filter(
          (ev) => selectedCalendarList[ev.calendarId]
        );

        eventCacheRef.current.push(...filtered);

        setFetchEvents([...eventCacheRef.current]);

        if (filtered.length === 0) {
          keepLoading = false;
        }

        if (!cursorTimeRef.current) {
          keepLoading = false;
        }
      }

      if (firstLoad && loopCount >= maxLoops && cursorTimeRef.current) {
        setAutoLoadComplete(true);
      }
      else if (!cursorTimeRef.current) {
        setAutoLoadComplete(false);
      }
    } catch (err) {
      console.error("[loadEvents] error:", err);
    } finally {
      isFetchingRef.current = false;
      setIsLoading(false);
    }
  };

  const handleLoadMore = async () => {
    if (!prevStartRef.current) {
      const calApi = calendarRef.current?.getApi();
      if (!calApi) return;
      const v = calApi.view;
      if (v.activeStart && v.activeEnd) {
        prevStartRef.current = v.activeStart;
        prevEndRef.current = v.activeEnd;
      } else {
        return;
      }
    }

    if (!cursorTimeRef.current) {
      console.log("[handleLoadMore] cursor empty => no more data.");
      return;
    }

    await loadEvents(prevStartRef.current, prevEndRef.current, false);
  };

  const dateClickEvent = (info) => {
    openCreatePopup(info.dateStr);
  };

  const handleEventClick = (eventData) => {
    setSchedulePopupMode("edit");
    setSchedulePopupData(eventData);
    setSchedulePopupVisible(true);
  };

  const openCreatePopup = (selectedDate) => {
    const now = new Date();
    const hh = now.getHours();
    const mm = now.getMinutes();
    const selectedDateObj = selectedDate
      ? new Date(selectedDate)
      : new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const startAt = new Date(
      selectedDateObj.getFullYear(),
      selectedDateObj.getMonth(),
      selectedDateObj.getDate(),
      hh,
      mm
    );
    const endAt = new Date(startAt.getTime() + 60 * 60 * 1000);

    const fmt = (d) => {
      const y = d.getFullYear();
      const m = String(d.getMonth() + 1).padStart(2, "0");
      const dd = String(d.getDate()).padStart(2, "0");
      const h = String(d.getHours()).padStart(2, "0");
      const min = String(d.getMinutes()).padStart(2, "0");
      return `${y}-${m}-${dd}T${h}:${min}`;
    };

    setSchedulePopupMode("create");
    setSchedulePopupData({
      startAt: fmt(startAt),
      endAt: fmt(endAt),
      title: "",
      description: "",
      calendarId: "",
    });
    setSchedulePopupVisible(true);
  };

  const closeAllPopups = (updated) => {
    if (updated && calendarRef.current) {
      setRefreshKey((prev) => prev + 1);
    }

    setPopupVisible(false);
    setPopupData([]);
    setPopupTitle("");
    setSchedulePopupVisible(false);
    setSchedulePopupData(null);
  };

  return (
    <div className={styles.calendarContainer}>
      {isLoading && <LoadingOverlay fullScreen={false} />}

      <FullCalendar
        key={refreshKey}
        ref={calendarRef}
        plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin]}
        initialView="dayGridMonth"
        aspectRatio={1}
        height="99%"
        datesSet={handleDatesSet}
        headerToolbar={{
          left: `prev,next today${autoLoadComplete ? ",loadMore" : ""}`,
          center: "title",
          right: "dayGridMonth,timeGridWeek,timeGridDay",
        }}
        customButtons={{
          loadMore: {
            text: "더보기",
            click: handleLoadMore,
          },
        }}
        moreLinkClick={(arg) => {
          const customEvents = arg.allSegs.map((seg) => ({
            ...seg.event.extendedProps,
            id: seg.event.id,
            title: seg.event.title,
            startAt: seg.event.startStr,
            endAt: seg.event.endStr,
          }));

          const formattedDate = arg.date
              ? arg.date.toLocaleDateString("en-US", { month: "long", day: "numeric" })
              : "";

          setPopupTitle(`${formattedDate}`);
          setPopupData(customEvents);
          setPopupVisible(true);

          return true;
        }}
        events={fetchEvents}
        dateClick={dateClickEvent}
        eventClick={(arg) => {
          const eventData = {
            ...arg.event.extendedProps,
            id: arg.event.id,
            title: arg.event.title,
            startAt: arg.event.startStr,
            endAt: arg.event.endStr,
          };
          handleEventClick(eventData);
        }}
        dayMaxEventRows={true}
        fixedWeekCount={true}
      />

      {popupVisible && (
        <Popup
          title={popupTitle}
          onClose={() => closeAllPopups(false)}
          actions={[
            {
              label: "New Event",
              variant: "green",
              size: "medium",
              onClick: () => {
                const selectedDate = popupTitle.includes("Events on ")
                  ? popupTitle.split("Events on ")[1]
                  : new Date().toISOString().split("T")[0];
                openCreatePopup(selectedDate);
              },
            },
          ]}
        >
          {popupData.length > 0 ? (
            <ul>
              {popupData.map((ev, idx) => (
                <li key={idx} onClick={() => handleEventClick(ev)}>
                  <strong>{ev.title}</strong>
                  <p>
                    {ev.startAt} - {ev.endAt}
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