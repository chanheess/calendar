// ScheduleUtility.jsx
import axios from "axios";

export async function fetchScheduleNotifications(eventId) {
  try {
    const response = await axios.get(`/schedules/${eventId}/notifications`, {
      withCredentials: true,
      headers: { "Content-Type": "application/json" },
    });
    return response.data ? response.data : null;
  } catch (error) {
    console.error("Error fetching schedule notifications:", error);
    return null;
  }
}

export async function fetchRepeatDetails(repeatId) {
  if (!repeatId) return null;
  try {
    const response = await axios.get(`/repeats/${repeatId}`, {
      withCredentials: true,
      headers: { "Content-Type": "application/json" },
    });
    return response.data ? response.data : null;
  } catch (error) {
    console.error("Error fetching repeat details:", error);
    return null;
  }
}

export async function getScheduleGroupList(scheduleId) {
  try {
    const scheduleGroupResponse = await axios.get(
      `/schedules/${scheduleId}/group`,
      {
        withCredentials: true,
        headers: { "Content-Type": "application/json" },
      }
    );
    return scheduleGroupResponse.data ? scheduleGroupResponse.data : null;;
  } catch (error) {
    console.error("Error fetching getScheduleGroupList:", error);
    return null;
  }
}

export function convertDTOToNotifications(notifications, startAt) {
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
}

export function convertNotificationsToDTO(notifications, startAt) {
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
    return { notificationAt: formatDateTime(notificationAt) };
  });
}

export function formatRepeatDetails(repeatDetails) {
  if (!repeatDetails) return null;

  return {
    repeatInterval: repeatDetails.repeatInterval,
    repeatType: repeatDetails.repeatType,
    endAt: repeatDetails.endAt ? formatDateTime(new Date(repeatDetails.endAt)) : null,
  };
}

export function formatDateTime(date) {
  const year = date.getFullYear();
  const month = ("0" + (date.getMonth() + 1)).slice(-2);
  const day = ("0" + date.getDate()).slice(-2);
  const hours = ("0" + date.getHours()).slice(-2);
  const minutes = ("0" + date.getMinutes()).slice(-2);
  return `${year}-${month}-${day}T${hours}:${minutes}`;
}

export function applyDeltaDate(dateTime, delta) {
  if (!dateTime) {
    return null;
  }

  const date = new Date(dateTime);

  date.setFullYear(date.getFullYear() + (delta.years || 0));
  date.setMonth(date.getMonth() + (delta.months || 0));
  date.setDate(date.getDate() + (delta.days || 0));
  date.setMilliseconds(date.getMilliseconds() + (delta.milliseconds || 0));

  return formatDateTime(date);
}