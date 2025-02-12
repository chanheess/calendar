import React, { useState, useEffect } from "react";
import styles from "styles/Popup.module.css";
import Button from "components/Button";
import Toggle from "components/Toggle";
import axios from "axios";
import RepeatPopup from "./RepeatPopup";

const SchedulePopup = ({ isOpen, mode, eventDetails, onClose, selectedCalendarList }) => {
  const [scheduleData, setScheduleData] = useState({
    id: "",
    title: "",
    description: "",
    startAt: "",
    endAt: "",
    repeatId: "",
    userId: "",
    calendarId: "",
    notifications: [],
    repeatDetails: {
      repeatInterval: 1,
      repeatType: "DAY",
      endAt: "",
    },
  });

  const [isNotificationEnabled, setIsNotificationEnabled] = useState(false);
  const [isRepeatEnabled, setIsRepeatEnabled] = useState(false);

  const [repeatPopupVisible, setRepeatPopupVisible] = useState(false);
  const [repeatPopupMode, setRepeatPopupMode] = useState("");

  useEffect(() => {
    const loadEventDetails = async () => {
      if (isOpen && mode === "edit" && eventDetails) {
        setScheduleData({
          id: eventDetails.id,
          title: eventDetails.title,
          description: eventDetails.description,
          startAt: eventDetails.startAt.substring(0, 16),
          endAt: eventDetails.endAt.substring(0, 16),
          repeatId: eventDetails.repeatId || "",
          userId: eventDetails.userId,
          calendarId: eventDetails.calendarId,
          notifications: [],
          repeatDetails: {
            repeatInterval: 1,
            repeatType: "DAY",
            endAt: "",
          },
        });

        const notificationList = await fetchScheduleNotifications(eventDetails.id);
        if(notificationList.length > 0) {
          setScheduleData((data) => ({
            ...data,
            notifications: convertDTOToNotifications(notificationList, eventDetails.startAt),
          }));
          setIsNotificationEnabled(true);
        }

        const repeatDetailList = await fetchRepeatDetails(eventDetails.repeatId);
        if (repeatDetailList) {
          setScheduleData((data) => ({
            ...data,
            repeatDetails: repeatDetailList,
          }));
          setIsRepeatEnabled(true);
        }

      } else if (isOpen && mode === "create") {
        setScheduleData({
          id: "",
          title: "",
          description: "",
          startAt: eventDetails.startAt.substring(0, 16),
          endAt: eventDetails.endAt.substring(0, 16),
          repeatId: "",
          userId: "",
          calendarId: Object.keys(selectedCalendarList)[0], // 첫 번째 캘린더 ID 기본값
          notifications: [{ time: 1, unit: "hours" }],
          repeatDetails: { repeatInterval: 1, repeatType: "DAY", endAt: "" },
        });

        setIsNotificationEnabled(false);
        setIsRepeatEnabled(false);
      }
    };

    loadEventDetails();
  }, [isOpen, mode, eventDetails, selectedCalendarList]);

  const getScheduleData = () => ({
    scheduleDto: {
      id: scheduleData.id,
      title: scheduleData.title,
      description: scheduleData.description,
      startAt: scheduleData.startAt,
      endAt: scheduleData.endAt,
      repeatId: scheduleData.repeatId,
      userId: scheduleData.userId,
      calendarId: scheduleData.calendarId,
    },
    notificationDto: isNotificationEnabled
      ? convertNotificationsToDTO(scheduleData.notifications, scheduleData.startAt)
      : [],
    repeatDto: formatRepeatDetails(scheduleData.repeatDetails),
  });

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

  const handleInputChange = (field, value) => {
    setScheduleData((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  // 알림 추가 (기본값: 1시간 전)
  const handleAddNotification = () => {
    setScheduleData((prevState) => ({
      ...prevState,
      notifications: [...prevState.notifications, { time: 1, unit: "hours" }],
    }));
  };

  // 특정 인덱스의 알림 업데이트
  const handleUpdateNotification = (index, field, value) => {
    setScheduleData((prevState) => ({
      ...prevState,
      notifications: prevState.notifications.map((notification, i) =>
        i === index ? { ...notification, [field]: value } : notification
      ),
    }));
  };

  // 특정 인덱스의 알림 삭제
  const handleRemoveNotification = (index) => {
    setScheduleData((prevState) => ({
      ...prevState,
      notifications: prevState.notifications.filter((_, i) => i !== index),
    }));
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

  const formatRepeatDetails = (repeatDetails) => {
    if (!isRepeatEnabled) return null;

    return {
      repeatInterval: repeatDetails.repeatInterval,
      repeatType: repeatDetails.repeatType,
      endAt: repeatDetails.endAt ? new Date(repeatDetails.endAt).toISOString() : null,
    };
  };

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

  const handleSave = async () => {
    try {
      if (mode === "create") {
        await axios.post("/schedules", getScheduleData(), {
            withCredentials: true,
            headers: { "Content-Type": "application/json" },
        });
        alert("Event created successfully!");
        onClose();
      } else if (mode === "edit") {
        if (scheduleData.repeatId) {
          openRepeatPopup("save");
        }
        else {
          await axios.patch(`/schedules/${scheduleData.id}?repeat=${isRepeatEnabled}`, getScheduleData(), {
              withCredentials: true,
              headers: { "Content-Type": "application/json" },
          });
          alert("Event updated successfully!");
          onClose();
        }
      }
    } catch (error) {
        console.error("Error saving schedule:", error.response?.data || error.message);
        alert("Failed to save schedule.");
    }
  };

  const repeatSave = async (url) => {
    try {
      if (mode === "edit" && scheduleData.repeatId) {
        await axios.patch(url, getScheduleData(), {
            withCredentials: true,
            headers: { "Content-Type": "application/json" },
        });
        alert("Event updated successfully!");
        onClose();
      }
    } catch (error) {
        console.error("Error saving schedule:", error.response?.data || error.message);
        alert("Failed to save schedule.");
    }
  };

  const handleDelete = async () => {
    try {
      if (mode === "edit") {
        if (scheduleData.repeatId) {
          openRepeatPopup("delete");
        }
        else {
          await axios.delete(`/schedules/${scheduleData.id}/calendars/${scheduleData.calendarId}`, {
              withCredentials: true,
          });
          alert("Event delete successfully!");
          onClose();
        }
      }
    } catch (error) {
      alert("Failed to delete schedule.");
    }
  };

  const repeatDelete = async (url) => {
    try {
      if (mode === "edit") {
        await axios.delete(url, {
            withCredentials: true,
        });
        alert("Event delete successfully!");
        onClose();
      }
    } catch (error) {
      alert("Failed to delete schedule.");
    }
  };

  const repeatConfirm = async (url) => {
    if (repeatPopupMode === "save") {
      repeatSave(url);
    } else if (repeatPopupMode === "delete") {
      repeatDelete(url);
    }
  }

  function openRepeatPopup(mode) {
    setRepeatPopupVisible(true);
    setRepeatPopupMode(mode);
  }

  function closeRepeatPopup() {
    setRepeatPopupVisible(false);
    setRepeatPopupMode("");
  }

  if (!isOpen) return null;

  return (
    <>
    {repeatPopupVisible && (
      <RepeatPopup
        isOpen={repeatPopupVisible}
        onClose={closeRepeatPopup}
        mode={repeatPopupMode}
        scheduleId={scheduleData.id}
        calendarId={scheduleData.calendarId}
        repeatCheck={isRepeatEnabled}
        onConfirm={repeatConfirm}
      />
    )}
    <div className={styles.popupOverlay}>
      <div className={styles.popup}>
        <div className={styles.popupHeader}>
          <h2>{mode === "edit" ? "Edit Schedule" : "Create Schedule"}</h2>
          <Button variant="close" size="" onClick={onClose}>
            ×
          </Button>
        </div>

        <div className={styles.popupContent}>
          <form>
            <div className={styles.infoRow}>
              <label>Title:</label>
              <input
                type="text"
                value={scheduleData.title}
                onChange={(e) => handleInputChange("title", e.target.value)}
              />
            </div>
            <div className={styles.infoRow}>
              <label>Description:</label>
              <textarea
                value={scheduleData.description}
                onChange={(e) => handleInputChange("description", e.target.value)}
              />
            </div>
            <div className={styles.infoRow}>
              <label>Start Time:</label>
              <input
                type="datetime-local"
                value={scheduleData.startAt}
                onChange={(e) => handleInputChange("startAt", e.target.value)}
              />
            </div>
            <div className={styles.infoRow}>
              <label>End Time:</label>
              <input
                type="datetime-local"
                value={scheduleData.endAt}
                onChange={(e) => handleInputChange("endAt", e.target.value)}
              />
            </div>
            <div className={styles.infoRow}>
              <label>Calendar:</label>
              <select value={scheduleData.calendarId} onChange={(e) => handleInputChange("calendarId", e.target.value)}>
                {Object.entries(selectedCalendarList).map(([key, value]) => (
                  <option key={key} value={key}>{value}</option>
                ))}
              </select>
            </div>

            <div className={styles.infoRow}>
              <label>Notifications:</label>
              <Toggle
                checked={isNotificationEnabled}
                onChange={() => setIsNotificationEnabled(!isNotificationEnabled)}
              />
            </div>

            {isNotificationEnabled && (
              <div>
                {scheduleData.notifications.map((notification, index) => (
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
                      size=""
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
                onChange={() => setIsRepeatEnabled(!isRepeatEnabled)}
              />
            </div>

            {isRepeatEnabled && (
              <div>
                <div className={styles.infoRow}>
                  <label>Repeat Interval:</label>
                  <input
                    type="number"
                    value={scheduleData.repeatDetails.repeatInterval}
                    onChange={(e) =>
                      setScheduleData((prevData) => ({
                        ...prevData,
                        repeatDetails: {
                          ...prevData.repeatDetails,
                          repeatInterval: e.target.value,
                        },
                      }))
                    }
                  />
                  <select
                    type="unit"
                    value={scheduleData.repeatDetails.repeatType}
                    onChange={(e) =>
                      setScheduleData((prevData) => ({
                        ...prevData,
                        repeatDetails: {
                          ...prevData.repeatDetails,
                          repeatType: e.target.value,
                        },
                      }))
                    }
                  >
                    <option value="DAY">Day(s)</option>
                    <option value="WEEK">Week(s)</option>
                    <option value="MONTH">Month(s)</option>
                    <option value="YEAR">Year(s)</option>
                  </select>
                </div>
                <div className={styles.infoRow}>
                  <label>End Time:</label>
                  <input
                    type="datetime-local"
                    value={scheduleData.repeatDetails.endAt}
                    onChange={(e) =>
                      setScheduleData((prevData) => ({
                        ...prevData,
                        repeatDetails: {
                          ...prevData.repeatDetails,
                          endAt: e.target.value,
                        },
                      }))
                    }
                  />
                </div>
              </div>
            )}
          </form>
        </div>  {/* <div className={styles.popupContent}> */}

        <div className={styles.popupFooter}>
          <Button variant="green" size="medium" onClick={handleSave} type="button">
            Save
          </Button>
          {mode === "edit" && (
            <Button variant="warning" size="medium" onClick={handleDelete} type="button">
              Delete
            </Button>
          )}
          <Button variant="logout" size="medium" onClick={onClose} type="button">
            Close
          </Button>
        </div>
      </div>  {/* <div className={styles.popup}> */}
    </div>  {/* <div className={styles.popupOverlay}> */}
    </>
  );
};

export default SchedulePopup;
