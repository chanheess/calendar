import React, { useState } from "react";
import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import timeGridPlugin from "@fullcalendar/timegrid";
import interactionPlugin from "@fullcalendar/interaction";
import axios from "axios";
import styles from "../styles/Calendar.module.css";

const CalendarComponent = ({ selectedCalendarIds }) => {
  const fetchEvents = async (fetchInfo, successCallback, failureCallback) => {
    const params = new URLSearchParams({
      start: fetchInfo.start.toISOString().split("T")[0],
      end: fetchInfo.end.toISOString().split("T")[0],
    });

    try {
      const response = await axios.get(`/schedules/date?${params.toString()}`, {
        withCredentials: true,
      });

      const data = response.data;
      let events = [];

      if (Array.isArray(selectedCalendarIds) && selectedCalendarIds.length > 0) {
        selectedCalendarIds.forEach((calendarId) => {
          if (data[calendarId]) {
            events = events.concat(
              data[calendarId].map((event) => ({
                id: event.id,
                title: event.title,
                start: event.startAt,
                end: event.endAt,
              }))
            );
          }
        });
      }

      successCallback(events);
    } catch (error) {
      console.error("Error fetching events:", error);
      failureCallback(error);
    }
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
      />
    </div>
  );
};

export default CalendarComponent;
