import React, { useState, useRef, useEffect, useCallback, forwardRef, useImperativeHandle } from "react";
import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import timeGridPlugin from "@fullcalendar/timegrid";
import interactionPlugin from "@fullcalendar/interaction";
import koLocale from "@fullcalendar/core/locales/ko";
import axios from 'utils/axiosInstance';

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

const CalendarComponent = forwardRef(({ selectedCalendarList, refreshKey, refreshSchedules }, ref) => {
  const [currentUserId, setCurrentUserId] = useState(null);

  // "더보기" 팝업 상태
  const [popupVisible, setPopupVisible] = useState(false);
  const [popupTitle, setPopupTitle] = useState("");
  const [popupData, setPopupData] = useState([]);
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

  const pageSize = 100;
  const maxLoops = 1;

  // 모바일 환경 감지
  const [isMobile, setIsMobile] = useState(false);

  useImperativeHandle(ref, () => ({
    closeAllPopups() {
      setPopupVisible(false);
      setPopupData([]);
      setPopupTitle("");
      setSchedulePopupVisible(false);
      setSchedulePopupData(null);
    }
  }));

  // 사용자 ID
  useEffect(() => {
    axios.get("/user/id")
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
          const res = await axios.get(`/schedules/date?${params.toString()}`);
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
          const filtered = newEvents.filter(
            (ev) => selectedCalendarList[ev.calendarId] && selectedCalendarList[ev.calendarId].isSelected
          );
          eventCacheRef.current.push(...filtered);
          setFetchEvents([...eventCacheRef.current]);

          if (filtered.length === 0 || !cursorTimeRef.current) {
            keepLoading = false;
          }
        }
        // "더보기" 버튼 노출 조건 수정
        if (cursorTimeRef.current && eventCacheRef.current.length >= pageSize) {
          setAutoLoadComplete(true);
        } else {
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
      await axios.patch(url, updateData);
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

  // Safari detection and body class toggling
  useEffect(() => {
    const isSafari = /^((?!chrome|android).)*safari/i.test(navigator.userAgent);
    if (isSafari) {
      document.body.classList.add("safari");
    } else {
      document.body.classList.remove("safari");
    }
  }, []);

  // 배경색 대비에 맞춰 회색 계열 가독성 색을 반환 (밝은 배경→진한 회색, 어두운 배경→연한 회색)
  const getReadableGrayTextColor = useCallback((bg) => {
    if (!bg) return null;
    let r, g, b;
    try {
      if (bg.startsWith('#')) {
        const hex = bg.replace('#', '');
        if (hex.length === 3) {
          r = parseInt(hex[0] + hex[0], 16);
          g = parseInt(hex[1] + hex[1], 16);
          b = parseInt(hex[2] + hex[2], 16);
        } else if (hex.length === 6) {
          r = parseInt(hex.substring(0, 2), 16);
          g = parseInt(hex.substring(2, 4), 16);
          b = parseInt(hex.substring(4, 6), 16);
        }
      } else if (bg.startsWith('rgb')) {
        const nums = bg.match(/\d+\.?\d*/g);
        if (nums && nums.length >= 3) {
          r = Number(nums[0]); g = Number(nums[1]); b = Number(nums[2]);
        }
      }
    } catch (_) { /* ignore */ }
    if ([r, g, b].some(v => typeof v !== 'number' || Number.isNaN(v))) return null;
    const srgb = [r, g, b].map(v => {
      v /= 255;
      return v <= 0.03928 ? v / 12.92 : Math.pow((v + 0.055) / 1.055, 2.4);
    });
    const luminance = 0.2126 * srgb[0] + 0.7152 * srgb[1] + 0.0722 * srgb[2];
    // 밝은 배경이면 진한 회색, 어두운 배경이면 연한 회색
    return luminance > 0.5 ? '#2f2f2f' : '#e9ecef';
  }, []);

  // 월 뷰에서 시간보다 제목이 먼저 보이도록 커스텀 렌더링
  const renderMonthEventContent = useCallback((arg) => {
    const title = arg.event.title || "";
    const color = arg.backgroundColor || arg.borderColor || arg.event.backgroundColor || arg.event.extendedProps?.color;
    const start = arg.event.start;
    const end = arg.event.end;

    // 시간 표기는 FullCalendar가 포맷팅한 값을 그대로 사용해 타임존/뷰 설정을 따르도록 한다
    const timeText = (!arg.event.allDay && arg.timeText) ? arg.timeText : "";

    // 긴 일정(배경을 넓게 덮는 유형) 판별: 하루 이상 또는 allDay
    const oneDayMs = 24 * 60 * 60 * 1000;
    const isLong = arg.event.allDay || (start && end && (end.getTime() - start.getTime() >= oneDayMs)) || (start && end && start.toDateString() !== end.toDateString());
    // 배경색 있는 긴 일정만 글자색 보정
    const textColor = (isLong && color) ? getReadableGrayTextColor(color) : null;
    const titleStyle = textColor ? { color: textColor } : undefined;
    const timeStyle = textColor ? { color: textColor, opacity: 0.9 } : undefined;

    return (
      <div className={styles.monthEvent}>
        <div className={styles.monthEventHeader}>
          <span className={styles.monthEventColor} style={{ backgroundColor: color }} />
          <span className={styles.monthEventTitle} style={titleStyle}>{title}</span>
        </div>
        {timeText ? (<div className={styles.monthEventTime} style={timeStyle}>{timeText}</div>) : null}
      </div>
    );
  }, [getReadableGrayTextColor]);

  // 새로운 일정을 기존 이벤트 배열에 추가하는 함수
  const addNewEventToCalendar = (newEventData, isRepeatEnabled) => {
    // 반복 일정이거나 반복 설정이 활성화된 경우 전체 리프레시
    if (newEventData.repeatId || isRepeatEnabled) {
      if (typeof refreshSchedules === "function") {
        refreshSchedules();
      }
      return;
    }

    const formattedEvent = {
      id: newEventData.id,
      title: newEventData.title,
      start: newEventData.startAt,
      end: newEventData.endAt,
      description: newEventData.description,
      repeatId: newEventData.repeatId,
      userId: newEventData.userId,
      calendarId: newEventData.calendarId,
      backgroundColor: selectedCalendarList[newEventData.calendarId]?.color || "#3788d8",
    };

    if (selectedCalendarList[newEventData.calendarId]?.isSelected) {
      eventCacheRef.current = [...eventCacheRef.current, formattedEvent];
      setFetchEvents([...eventCacheRef.current]);
    }
  };



  // 모바일 환경 감지
  useEffect(() => {
    const checkMobile = () => {
      setIsMobile(window.innerWidth <= 768);
    };

    // 초기 체크
    checkMobile();

    // 리사이즈 이벤트 리스너
    window.addEventListener('resize', checkMobile);

    // 클린업
    return () => window.removeEventListener('resize', checkMobile);
  }, []);

  // iOS Safari 주소창 높이 변경 대응
  useEffect(() => {
    const handleResize = () => {
      // 100vh 대신 실제 viewport 높이 사용
      document.documentElement.style.setProperty('--vh', `${window.innerHeight * 0.01}px`);
    };

    handleResize();
    window.addEventListener('resize', handleResize);
    window.addEventListener('orientationchange', handleResize);

    return () => {
      window.removeEventListener('resize', handleResize);
      window.removeEventListener('orientationchange', handleResize);
    };
  }, []);

  return (
    <div className={`${styles.calendarContainer} ${typeof document !== "undefined" && document.body.classList.contains("safari") ? 'safari' : ''}`} style={{ paddingBottom: 'env(safe-area-inset-bottom, 0px)' }}>
      {isLoading && <LoadingOverlay fullScreen={false} />}

      <FullCalendar
        key={refreshKey}
        ref={calendarRef}
        plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin]}
        locale={koLocale}
        timeZone="Asia/Seoul"
        initialView={isMobile ? "dayGridMonth" : "dayGridMonth"}
        editable={true}
        eventResizableFromStart={true}
        fixedWeekCount={false}
        height="100%"
        headerToolbar={{
          left: isMobile ? 'prev,next' : `prev,next today${autoLoadComplete ? ",loadMore" : ""}`,
          center: 'title',
          right: isMobile ? 'today,dayGridMonth,timeGridDay' : 'dayGridMonth,timeGridWeek,timeGridDay'
        }}
        views={{
          dayGridMonth: {
            buttonText: "월",
            eventContent: renderMonthEventContent,
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
        datesSet={handleDatesSet}
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
        dayMaxEventRows={true}
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
          onClose={(updated, newEventData, isRepeatEnabled) => {
            setSchedulePopupVisible(false);
            if (updated) {
              if (schedulePopupMode === 'create' && newEventData) {
                addNewEventToCalendar(newEventData, isRepeatEnabled);
              } else {
                // 수정이나 삭제의 경우는 전체 리프레시
                if (typeof refreshSchedules === "function") {
                  refreshSchedules();
                }
              }
            }
          }}
          selectedCalendarList={selectedCalendarList}
          currentUserId={currentUserId}
        />
      )}
    </div>
  );
});

export default CalendarComponent;