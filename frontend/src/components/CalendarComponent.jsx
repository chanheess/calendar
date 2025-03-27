import React, { useState, useRef, useEffect, useCallback } from "react";
import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import timeGridPlugin from "@fullcalendar/timegrid";
import interactionPlugin from "@fullcalendar/interaction";
import koLocale from "@fullcalendar/core/locales/ko";
import axios from "axios";

import styles from "styles/Calendar.module.css";
import Popup from "./popups/Popup";
import SchedulePopup from "./popups/SchedulePopup";
import LoadingOverlay from "components/LoadingOverlay";

import {
  fetchScheduleNotifications,
  fetchRepeatDetails,
  getScheduleGroupList,
  applyDeltaDate,
} from "components/ScheduleUtility";

const CalendarComponent = ({ selectedCalendarList, refreshKey, refreshSchedules }) => {
  const [currentUserId, setCurrentUserId] = useState(null);

  // "더보기" 팝업 상태
  const [popupVisible, setPopupVisible] = useState(false);
  const [popupTitle, setPopupTitle] = useState("");
  const [popupData, setPopupData] = useState([]);
  // ⬇️ We'll store the actual Date object here
  const [popupSelectedDate, setPopupSelectedDate] = useState(null);

  // 스케줄 팝업 상태
  const [schedulePopupVisible, setSchedulePopupVisible] = useState(false);
  const [schedulePopupMode, setSchedulePopupMode] = useState("create");
  const [schedulePopupData, setSchedulePopupData] = useState(null);

  const [fetchEvents, setFetchEvents] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const [autoLoadComplete, setAutoLoadComplete] = useState(false);

  const eventCacheRef = useRef([]);
  const cursorTimeRef = useRef("");
  const cursorIdRef = useRef(null);
  const prevStartRef = useRef(null);
  const prevEndRef = useRef(null);
  const isFetchingRef = useRef(false);
  const calendarRef = useRef(null);

  const pageSize = 1000;
  const maxLoops = 10;

  // 사용자 ID
  useEffect(() => {
    axios
      .get("/user/id", {
        withCredentials: true,
        headers: { "Content-Type": "application/json" },
      })
      .then((response) => {
        setCurrentUserId(response.data);
      })
      .catch((error) => {
        console.error("Error fetching user id:", error);
      });
  }, []);

  // 일정 불러오기
  const loadEvents = useCallback(
    async (startDateObj, endDateObj, firstLoad) => {
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

          if (data.nextCursor && data.nextCursor !== "null" && data.nextCursor !== "undefined") {
            cursorTimeRef.current = data.nextCursor;
          } else {
            cursorTimeRef.current = "";
          }

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
            backgroundColor: selectedCalendarList[evt.calendarId]?.color || "#3788d8",
          }));
          const filtered = newEvents.filter((ev) => selectedCalendarList[ev.calendarId]);
          eventCacheRef.current.push(...filtered);
          setFetchEvents([...eventCacheRef.current]);

          if (filtered.length === 0 || !cursorTimeRef.current) {
            keepLoading = false;
          }
        }

        if (firstLoad && loopCount >= maxLoops && cursorTimeRef.current) {
          setAutoLoadComplete(true);
        } else if (!cursorTimeRef.current) {
          setAutoLoadComplete(false);
        }
      } catch (err) {
        console.error("[loadEvents] error:", err);
      } finally {
        isFetchingRef.current = false;
        setIsLoading(false);
      }
    },
    [selectedCalendarList]
  );

  // refreshKey 변경 시 재로딩
  useEffect(() => {
    eventCacheRef.current = [];
    cursorTimeRef.current = "";
    cursorIdRef.current = null;
    prevStartRef.current = null;
    prevEndRef.current = null;
    setFetchEvents([]);
    setAutoLoadComplete(false);

    if (calendarRef.current) {
      const calApi = calendarRef.current.getApi();
      const v = calApi.view;
      if (v.activeStart && v.activeEnd) {
        loadEvents(v.activeStart, v.activeEnd, true);
      }
    }
  }, [selectedCalendarList, refreshKey, loadEvents]);

  // 뷰가 바뀌거나 날짜 범위가 바뀔 때마다
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

      prevStartRef.current = start;
      prevEndRef.current = end;
      loadEvents(start, end, true);
    }
  };

  // "더보기" 버튼
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
      console.log("[handleLoadMore] 더 이상 불러올 데이터가 없습니다.");
      return;
    }
    await loadEvents(prevStartRef.current, prevEndRef.current, false);
  };

  // 일정 드래그/리사이즈 후 업데이트
  const handleEventDrop = async (info, type) => {
    const eventData = {
      ...info.event.extendedProps,
      id: info.event.id,
      title: info.event.title,
      startAt: info.event.startStr,
      endAt: info.event.endStr,
    };

    let updateData = {
      scheduleDto: {
        id: eventData.id,
        title: eventData.title,
        description: eventData.description,
        startAt: eventData.startAt.substring(0, 16),
        endAt: eventData.endAt.substring(0, 16),
        repeatId: eventData.repeatId,
        userId: eventData.userId,
        calendarId: eventData.calendarId,
      },
      notificationDto: await fetchScheduleNotifications(eventData.id),
      repeatDto: await fetchRepeatDetails(eventData.repeatId),
      groupDto: await getScheduleGroupList(eventData.id),
    };

    let delta = type === "drop" ? info.delta : info.startDelta;
    // 알림 일자/반복 종료일 등에 delta 적용 if needed
    updateData.notificationDto = updateData.notificationDto.map((notification) => ({
      ...notification,
      notificationAt: applyDeltaDate(notification.notificationAt, delta),
    }));

    if (updateData.scheduleDto.repeatId && updateData.repeatDto && updateData.repeatDto.endAt) {
      updateData.repeatDto.endAt = applyDeltaDate(updateData.repeatDto.endAt, delta);
    }

    const url = updateData.scheduleDto.repeatId
      ? `/schedules/${updateData.scheduleDto.id}/current-and-future?repeat=true`
      : `/schedules/${updateData.scheduleDto.id}?repeat=false`;

    try {
      await axios.patch(url, updateData, {
        withCredentials: true,
        headers: { "Content-Type": "application/json" },
      });
      alert("일정이 성공적으로 업데이트되었습니다.");
      if (typeof refreshSchedules === "function") {
        refreshSchedules();
      }
    } catch (error) {
      console.error("일정 업데이트 실패:", error);
      alert("일정 업데이트에 실패했습니다.");
      info.revert();
    }
  };

  // 날짜 클릭 → 새 일정 생성
  const dateClickEvent = (info) => {
    openCreatePopup(info.dateStr);
  };

  // 일정 클릭 → 수정 팝업
  const handleEventClick = (eventData) => {
    setSchedulePopupMode("edit");
    setSchedulePopupData(eventData);
    setSchedulePopupVisible(true);
  };

  // "새 일정" 팝업 열기
  const openCreatePopup = (selectedDate) => {
    // selectedDate는 YYYY-MM-DD 형태의 문자열
    const now = new Date();
    const hh = now.getHours();
    const mm = now.getMinutes();

    // Parse the 'selectedDate' string into a Date
    // e.g. "2025-03-05" -> new Date(2025, 2, 5, ...)
    let year = now.getFullYear();
    let month = now.getMonth();
    let day = now.getDate();

    if (selectedDate) {
      const parts = selectedDate.split("-");
      if (parts.length === 3) {
        year = parseInt(parts[0], 10);
        month = parseInt(parts[1], 10) - 1;
        day = parseInt(parts[2], 10);
      }
    }
    const startAt = new Date(year, month, day, hh, mm);
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

  // 팝업/스케줄 팝업 닫기
  const closeAllPopups = (updated) => {
    setPopupVisible(false);
    setPopupData([]);
    setPopupTitle("");
    setSchedulePopupVisible(false);
    setSchedulePopupData(null);
    if (updated && typeof refreshSchedules === "function") {
      refreshSchedules();
    }
  };

  // 날짜 포맷
  const formatScheduleDate = (startAt, endAt) => {
    const start = new Date(startAt);
    const end = new Date(endAt);
    const startDate = start.toLocaleDateString("ko-KR", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
    const startTime = start.toLocaleTimeString("ko-KR", {
      hour: "numeric",
      minute: "2-digit",
    });
    const endDate = end.toLocaleDateString("ko-KR", {
      year: "numeric",
      month: "long",
      day: "numeric",
    });
    const endTime = end.toLocaleTimeString("ko-KR", {
      hour: "numeric",
      minute: "2-digit",
    });
    return `${startDate}, ${startTime} ~ ${endDate}, ${endTime}`;
  };

  return (
    <div className={styles.calendarContainer}>
      {isLoading && <LoadingOverlay fullScreen={false} />}

      <FullCalendar
        key={refreshKey}
        ref={calendarRef}
        plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin]}
        locale={koLocale}
        timeZone="Asia/Seoul"
        initialView="dayGridMonth"
        aspectRatio={1}
        editable={true}
        height="99%"
        eventResizableFromStart={true}
        datesSet={handleDatesSet}
        headerToolbar={{
          left: `prev,next today${autoLoadComplete ? ",loadMore" : ""}`,
          center: "title",
          right: "dayGridMonth,timeGridWeek,timeGridDay",
        }}
        views={{
          dayGridMonth: {
            buttonText: "월",
            // 날짜마다 "오전 7:53" 등의 시간이 표시되는 것을 제거하고
            // 단순히 일(day)만 보이도록
            dayCellContent: (args) => {
              const dayNum = args.date.getDate();
              return { html: String(dayNum) };
            },
          },
          timeGridWeek: { buttonText: "주" },
          timeGridDay: { buttonText: "일" },
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
            ? arg.date.toLocaleDateString("ko-KR", { month: "long", day: "numeric" })
            : "";

          // store the actual date so we can create a schedule on that day
          setPopupSelectedDate(arg.date);

          setPopupTitle(`${formattedDate} 일정`);
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
        eventDrop={(data) => {
          handleEventDrop(data, "drop");
        }}
        eventResize={(data) => {
          handleEventDrop(data, "resize");
        }}
        dayMaxEventRows={true}
        fixedWeekCount={true}
      />

      {/* "더보기" 팝업 */}
      {popupVisible && (
        <Popup
          title={popupTitle}
          onClose={() => closeAllPopups(false)}
          actions={[
            {
              label: "일정 추가",
              variant: "green",
              size: "medium",
              onClick: () => {
                if (popupSelectedDate) {
                  const yyyy = popupSelectedDate.getFullYear();
                  const mm = String(popupSelectedDate.getMonth() + 1).padStart(2, "0");
                  const dd = String(popupSelectedDate.getDate()).padStart(2, "0");
                  const isoDateStr = `${yyyy}-${mm}-${dd}`;

                  openCreatePopup(isoDateStr);
                }
              },
            },
          ]}
        >
          {popupData.length > 0 ? (
            <ul>
              {popupData.map((ev, idx) => (
                <li
                  key={idx}
                  onClick={() => handleEventClick(ev)}
                  style={{ cursor: "pointer", marginBottom: "12px" }}
                >
                  <div style={{ display: "flex", alignItems: "center", marginBottom: "4px" }}>
                    <div
                      style={{
                        width: "10px",
                        height: "10px",
                        borderRadius: "50%",
                        backgroundColor:
                          selectedCalendarList[ev.calendarId]?.color || "#3788d8",
                        marginRight: "8px",
                      }}
                    />
                    <strong>{ev.title || "(제목 없음)"}</strong>
                  </div>
                  <div style={{ fontSize: "13px", color: "#666" }}>
                    {selectedCalendarList[ev.calendarId]?.title || "알 수 없는 캘린더"}
                  </div>
                  <div style={{ fontSize: "13px", color: "#666" }}>
                    {formatScheduleDate(ev.startAt, ev.endAt)}
                  </div>
                </li>
              ))}
            </ul>
          ) : (
            <p>일정이 없습니다.</p>
          )}
        </Popup>
      )}

      {/* 일정 생성/편집 팝업 */}
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
          currentUserId={currentUserId}
        />
      )}
    </div>
  );
};

export default CalendarComponent;