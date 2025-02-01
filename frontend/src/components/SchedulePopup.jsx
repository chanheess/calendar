import React, { useState, useEffect } from "react";
import styles from "../styles/SchedulePopup.module.css";
import Button from "./Button";
import Toggle from "./Toggle";
import axios from "axios";

const SchedulePopup = ({ isOpen, mode, eventDetails, onClose, onSave, selectedCalendarList }) => {
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [startAt, setStartAt] = useState("");
  const [endAt, setEndAt] = useState("");
  const [calendarId, setCalendarId] = useState("");
  const [notifications, setNotifications] = useState([]);
  const [isNotificationEnabled, setIsNotificationEnabled] = useState(false);
  const [repeatDetails, setRepeatDetails] = useState({
    repeatInterval: 1,
    repeatType: "d",
    endAt: "",
  });
  const [isRepeatEnabled, setIsRepeatEnabled] = useState(false);

  useEffect(() => {
    const loadEventDetails = async () => {
      if (isOpen && mode === "edit" && eventDetails) {
        setTitle(eventDetails.title);
        setDescription(eventDetails.description);
        setStartAt(eventDetails.startAt.substring(0, 16));
        setEndAt(eventDetails.endAt.substring(0, 16));
        setCalendarId(eventDetails.calendarId);

        const notificationList = await fetchScheduleNotifications(eventDetails.id);
        if(notificationList.length > 0) {
          setNotifications(convertDTOToNotifications(notificationList, eventDetails.startAt));
          setIsNotificationEnabled(true);
        }

        const repeatDetailList = await fetchRepeatDetails(eventDetails.repeatId);
        if (repeatDetailList) {
          setRepeatDetails(repeatDetailList);
          setIsRepeatEnabled(true);
        }

      } else if (isOpen && mode === "create") {
        // Create mode: reset fields
        setTitle("");
        setDescription("");
        setStartAt(eventDetails.startAt.substring(0, 16));
        setEndAt(eventDetails.endAt.substring(0, 16));

        const firstCalendarId = Object.keys(selectedCalendarList)[0]; // 첫 번째 캘린더 ID
        setCalendarId(firstCalendarId); // 기본값으로 첫 번째 캘린더 ID 설정

        setIsNotificationEnabled(false);
        setNotifications([{ time: 1, unit: "hours" }]);
        setIsRepeatEnabled(false);
        setRepeatDetails({ repeatInterval: 1, repeatType: "d", endAt: "" });
      }
    };

    loadEventDetails();
  }, [isOpen, mode, eventDetails]);

  const fetchScheduleNotifications = async (eventId) => {
    try {
      const response = await axios.get(`/schedules/${eventId}/notifications`, {
        withCredentials: true,
        headers: { "Content-Type": "application/json" },
      });

      return response.data;
    } catch (error) {
      console.error("Error fetching schedule notifications:", error);
    }
  };

  const handleAddNotification = (e) => {
      setNotifications([...notifications, { time: 1, unit: "hours" }]);
    };

  const handleUpdateNotification = (index, value) => {
    const updatedNotifications = notifications.map((notification, i) =>
      i === index ? { notificationAt: value } : notification
    );
    setNotifications(updatedNotifications);
  };

  const handleRemoveNotification = (index) => {
    const updatedNotifications = notifications.filter((_, i) => i !== index);
    setNotifications(updatedNotifications);
  };

  const convertNotificationsToDTO = (notifications, startAt) => {
    return notifications.map((notification) => {
      const { time, unit } = notification;

      let notificationAt = new Date(startAt);

      switch (unit) {
        case "minutes":
          notificationAt.setMinutes(notificationAt.getMinutes() - time);
          break;
        case "hours":
          notificationAt.setHours(notificationAt.getHours() - time);
          break;
        case "days":
          notificationAt.setDate(notificationAt.getDate() - time);
          break;
        case "weeks":
          notificationAt.setDate(notificationAt.getDate() - time * 7);
          break;
        case "months":
          notificationAt.setMonth(notificationAt.getMonth() - time);
          break;
        case "years":
          notificationAt.setFullYear(notificationAt.getFullYear() - time);
          break;
        default:
          console.warn("Unknown time unit:", unit);
      }

      return {
        notificationAt: formatDateTime(notificationAt),
      };
    });
  };

  function formatDateTime(date) {
    const year = date.getFullYear();
    const month = ('0' + (date.getMonth() + 1)).slice(-2);
    const day = ('0' + date.getDate()).slice(-2);
    const hours = ('0' + date.getHours()).slice(-2);
    const minutes = ('0' + date.getMinutes()).slice(-2);

    return `${year}-${month}-${day}T${hours}:${minutes}`;
  }

  const convertDTOToNotifications = (notifications, startAt) => {
    return notifications.map((notification) => {
      const startDate = new Date(startAt);
      const notificationDate = new Date(notification.notificationAt);

      const diffInMillis = startDate - notificationDate;

      const diffInMinutes = Math.floor(diffInMillis / (1000 * 60));
      const diffInHours = Math.floor(diffInMillis / (1000 * 60 * 60));
      const diffInDays = Math.floor(diffInMillis / (1000 * 60 * 60 * 24));

      if (diffInDays > 0) {
        return { unit: "days", time: diffInDays };
      } else if (diffInHours > 0) {
        return { unit: "hours", time: diffInHours };
      } else {
        return { unit: "minutes", time: diffInMinutes };
      }
    });
  };

  const fetchRepeatDetails = async (repeatId) => {
    if (!repeatId) return null; // repeatId가 없으면 null 반환

    try {
      const response = await axios.get(`/repeats/${repeatId}`, {
        withCredentials: true,
        headers: { "Content-Type": "application/json" },
      });

      return response.data;
    } catch (error) {
      console.error("Error fetching repeat details:", error);
      throw error;
    }
  };

  const formatRepeatDetails = (repeatDetails) => {
    if (!isRepeatEnabled) return null;

    const repeatTypeMap = {
      d: "DAY",
      w: "WEEK",
      m: "MONTH",
      y: "YEAR",
    };

    return {
      repeatInterval: repeatDetails.repeatInterval,
      repeatType: repeatTypeMap[repeatDetails.repeatType],
      endAt: repeatDetails.endAt ? new Date(repeatDetails.endAt).toISOString() : null,
    };
  };

  const handleSave = () => {
    const scheduleData = {
      scheduleDto: {
        title,
        description,
        startAt,
        endAt,
        calendarId,
      },
      notificationDto: isNotificationEnabled
        ? convertNotificationsToDTO(notifications, startAt)
        : [],
      repeatDto: formatRepeatDetails(repeatDetails),
    };

    onSave(scheduleData);
  };

  if (!isOpen) return null;

  return (
    <div className={styles.popupOverlay}>
      <div className={styles.popup}>
        <div className={styles.popupHeader}>
          <h2>{mode === "edit" ? "Edit Schedule" : "Create Schedule"}</h2>
          <Button variant="close" size="none" onClick={onClose}>
            ×
          </Button>
        </div>
        <form>
          <div className={styles.infoRow}>
            <label>Title:</label>
            <input
              type="text"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
            />
          </div>
          <div className={styles.infoRow}>
            <label>Description:</label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </div>
          <div className={styles.infoRow}>
            <label>Start Time:</label>
            <input
              type="datetime-local"
              value={startAt}
              onChange={(e) => setStartAt(e.target.value)}
            />
          </div>
          <div className={styles.infoRow}>
            <label>End Time:</label>
            <input
              type="datetime-local"
              value={endAt}
              onChange={(e) => setEndAt(e.target.value)}
            />
          </div>
          <div className={styles.infoRow}>
            <label>Calendar:</label>
            <select
              value={calendarId}
              onChange={(e) => {
                setCalendarId(e.target.value);
                console.log("Selected Calendar ID:", e.target.value); // 선택된 값 출력
              }}

            >
              {mode === "create" ? (
                selectedCalendarList && typeof selectedCalendarList === "object" ? (
                  Object.entries(selectedCalendarList).map(([key, value]) => (
                    <option key={key} value={key}>
                      {value}
                    </option>
                  ))
                ) : (
                  <option value="">No Calendars Available</option>
                )
              ) : (
                <option value={eventDetails?.calendarId || ""}>
                  {selectedCalendarList[eventDetails?.calendarId] || "Unknown Calendar"}
                </option>
              )}
            </select>
          </div>

          <div className={styles.infoRow}>
            <label>Notifications:</label>
            <Toggle
              checked={isNotificationEnabled}
              onChange={(e) => setIsNotificationEnabled(e.target.checked)}
            />
          </div>

          {isNotificationEnabled && (
            <div>
              {notifications.map((notification, index) => (
                <div key={index} className={styles.notificationRow}>
                  <input
                    type="number"
                    value={notification.time}
                    onChange={(e) =>
                      handleUpdateNotification(index, "time", e.target.value)
                    }
                    className={styles.notificationInput}
                    min="1"
                  />
                  <select
                    value={notification.unit}
                    onChange={(e) =>
                      handleUpdateNotification(index, "unit", e.target.value)
                    }
                    className={styles.notificationSelect}
                  >
                    <option value="minutes">minutes</option>
                    <option value="hours">hours</option>
                    <option value="days">days</option>
                  </select>
                  <Button
                    variant="close"
                    size="none"
                    onClick={() => handleRemoveNotification(index)}
                  >
                    ×
                  </Button>
                </div>
              ))}
              <div className={styles.infoRow} >
                <Button
                  variant="primary"
                  size="medium"
                  onClick={(e) => {
                    e.preventDefault();
                    handleAddNotification();
                  }}
                >
                  Add Notification
                </Button>
              </div>
            </div>
          )}

          <div className={styles.infoRow}>
            <label>Repeat:</label>
            <Toggle
              checked={isRepeatEnabled}
              onChange={(e) => setIsRepeatEnabled(e.target.checked)}
            />
          </div>

          {isRepeatEnabled && (
            <div>
              <div className={styles.infoRow}>
                <label>Repeat Interval:</label>
                <input
                  type="number"
                  value={repeatDetails.repeatInterval}
                  onChange={(e) =>
                    setRepeatDetails({
                      ...repeatDetails,
                      repeatInterval: e.target.value,
                    })
                  }
                />
                <select
                  value={repeatDetails.repeatType}
                  onChange={(e) =>
                    setRepeatDetails({
                      ...repeatDetails,
                      repeatType: e.target.value,
                    })
                  }
                >
                  <option value="d">Day(s)</option>
                  <option value="w">Week(s)</option>
                  <option value="m">Month(s)</option>
                  <option value="y">Year(s)</option>
                </select>
              </div>
              <div className={styles.infoRow}>
                <label>End Time:</label>
                <input
                  type="datetime-local"
                  value={repeatDetails.endAt}
                  onChange={(e) =>
                    setRepeatDetails({
                      ...repeatDetails,
                      endAt: e.target.value,
                    })
                  }
                />
              </div>
            </div>
          )}

          <div className={styles.popupFooter}>
            <Button variant="green" size="medium" onClick={handleSave} type="button">
              Save
            </Button>
            <Button variant="logout" size="medium" onClick={onClose} type="button">
              Close
            </Button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default SchedulePopup;
