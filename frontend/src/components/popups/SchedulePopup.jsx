import React, { useState, useEffect, useCallback } from "react";
import styles from "styles/Popup.module.css";
import Button from "components/Button";
import Toggle from "components/Toggle";
import axios from "axios";
import RepeatPopup from "./RepeatPopup";

  async function fetchScheduleNotifications(eventId) {
    try {
      const response = await axios.get(`/schedules/${eventId}/notifications`, {
        withCredentials: true,
        headers: { "Content-Type": "application/json" },
      });
      return response.data;
    } catch (error) {
      console.error("Error fetching schedule notifications:", error);
      return null;
    }
  }

  async function fetchRepeatDetails(repeatId) {
    if (!repeatId) return null;
    try {
      const response = await axios.get(`/repeats/${repeatId}`, {
        withCredentials: true,
        headers: { "Content-Type": "application/json" },
      });
      return response.data;
    } catch (error) {
      console.error("Error fetching repeat details:", error);
      return null;
    }
  }

  function convertDTOToNotifications(notifications, startAt) {
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

  function convertNotificationsToDTO(notifications, startAt) {
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

  function formatRepeatDetails(repeatDetails) {
    // isRepeatEnabled는 외부에서 체크 후 호출
    if (!repeatDetails) return null;
    return {
      repeatInterval: repeatDetails.repeatInterval,
      repeatType: repeatDetails.repeatType,
      endAt: repeatDetails.endAt ? new Date(repeatDetails.endAt).toISOString() : null,
    };
  }

  function formatDateTime(date) {
    const year = date.getFullYear();
    const month = ("0" + (date.getMonth() + 1)).slice(-2);
    const day = ("0" + date.getDate()).slice(-2);
    const hours = ("0" + date.getHours()).slice(-2);
    const minutes = ("0" + date.getMinutes()).slice(-2);
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  }

  const SchedulePopup = ({
    isOpen,
    mode,              // "edit" or "create"
    eventDetails,
    onClose,
    selectedCalendarList,
    currentUserId,
  }) => {
    useEffect(() => {
      const handleEsc = (event) => {
        if (event.key === "Escape") {
          onClose();
        }
      };
      window.addEventListener("keydown", handleEsc);
      return () => window.removeEventListener("keydown", handleEsc);
    }, [onClose]);

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
  const [groupUserList, setGroupUserList] = useState([]);
  const [showGroupUsers, setShowGroupUsers] = useState(false);
  const [repeatPopupVisible, setRepeatPopupVisible] = useState(false);
  const [repeatPopupMode, setRepeatPopupMode] = useState("");
  const [participationStatus, setParticipationStatus] = useState(null);

  const currentUserPermission =
    groupUserList.find((user) => user.userId === currentUserId)?.permission || "READ";
  // 일정 편집모드에서 현재 사용자가 ADMIN이거나 일정 소유자이면 그룹 관리를 할 수 있다.
  const canManageGroup =
    mode === "edit" && (currentUserPermission === "ADMIN" || scheduleData.userId === currentUserId);
  // 편집모드에서 READ/WRITE 권한이면 readOnly 처리
  const isReadOnly =
    mode === "edit" && currentUserPermission !== "ADMIN" && scheduleData.userId !== currentUserId;

  // 일정 데이터 로드
  useEffect(() => {
    const loadEventDetails = async () => {
      if (!isOpen) return;

      if (mode === "edit" && eventDetails) {
        // 기존 일정 편집
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

        // 알림 정보
        const notificationList = await fetchScheduleNotifications(eventDetails.id);
        if (notificationList && notificationList.length > 0) {
          setScheduleData((data) => ({
            ...data,
            notifications: convertDTOToNotifications(notificationList, eventDetails.startAt),
          }));
          setIsNotificationEnabled(true);
        }

        // 반복 정보
        const repeatDetailList = await fetchRepeatDetails(eventDetails.repeatId);
        if (repeatDetailList) {
          setScheduleData((data) => ({
            ...data,
            repeatDetails: repeatDetailList,
          }));
          setIsRepeatEnabled(true);
        }
      } else if (mode === "create" && eventDetails) {
        // 새 일정 생성
        setScheduleData({
          id: "",
          title: "",
          description: "",
          startAt: eventDetails.startAt.substring(0, 16),
          endAt: eventDetails.endAt.substring(0, 16),
          repeatId: "",
          userId: "",
          calendarId: Object.keys(selectedCalendarList)[0],
          notifications: [{ time: 1, unit: "hours" }],
          repeatDetails: { repeatInterval: 1, repeatType: "DAY", endAt: "" },
        });
        setIsNotificationEnabled(false);
        setIsRepeatEnabled(false);
      }
    };

    loadEventDetails();
  }, [isOpen, mode, eventDetails, selectedCalendarList]);

  const loadGroupUsers = useCallback(async () => {
    if (
      !selectedCalendarList[scheduleData.calendarId] ||
      selectedCalendarList[scheduleData.calendarId].category !== "GROUP"
    ) {
      return;
    }
    try {
      // 그룹 내 전체 사용자 가져오기
      const allUsersResponse = await axios.get(
        `/groups/${scheduleData.calendarId}/users`,
        {
          withCredentials: true,
          headers: { "Content-Type": "application/json" },
        }
      );
      const allUsers = allUsersResponse.data;

      let invitedUsers = [];
      if (mode === "edit") {
        const scheduleGroupResponse = await axios.get(
          `/schedules/${scheduleData.id}/group`,
          {
            withCredentials: true,
            headers: { "Content-Type": "application/json" },
          }
        );
        invitedUsers = scheduleGroupResponse.data;
      }

      // create 모드인 경우
      if (mode === "create") {
        const mergedUsers = allUsers.map((user) => ({
          ...user,
          selected: false,
          permission: user.userId === currentUserId ? "ADMIN" : "READ",
          status: "Not selected",
          userNickname: user.userNickname || "",
        }));
        setGroupUserList(mergedUsers);
        return;
      }

      // edit 모드인데 invitedUsers가 없다면
      if (mode === "edit" && invitedUsers.length === 0) {
        const fallbackUsers = allUsers.map((user) => ({
          ...user,
          selected: false,
          permission: user.userId === currentUserId ? "ADMIN" : "READ",
          status: "Not selected",
          userNickname: user.userNickname || "",
        }));
        setGroupUserList(fallbackUsers);
        return;
      }

      // edit 모드이며 invitedUsers가 있는 경우
      const mergedUsers = allUsers.map((user) => {
        const invited = invitedUsers.find((u) => u.userId === user.userId);
        if (user.userId === currentUserId) {
          setParticipationStatus(invited?.status || "Not selected");
        }
        return {
          ...user,
          selected: !!invited,
          permission: invited ? (invited.authority || "READ") : "READ",
          status: invited?.status || "Not selected",
          userNickname: user.userNickname || "",
        };
      });

      setGroupUserList(mergedUsers);
      setShowGroupUsers(invitedUsers.length > 0 ? true : canManageGroup);
    } catch (error) {
      console.error("Error loading group users:", error);
      setShowGroupUsers(false);
    }
  }, [selectedCalendarList, scheduleData.calendarId, scheduleData.id, mode, currentUserId, canManageGroup]);

  // 그룹 사용자 로드
  useEffect(() => {
    if (
      isOpen &&
      selectedCalendarList[scheduleData.calendarId] &&
      selectedCalendarList[scheduleData.calendarId].category === "GROUP"
    ) {
      loadGroupUsers();
    }
  }, [isOpen, mode, scheduleData.id, selectedCalendarList, scheduleData.calendarId, loadGroupUsers]);

  // 일정/알림/반복 DTO 생성
  const getScheduleData = () => {
    const groupDto =
      selectedCalendarList[scheduleData.calendarId] &&
      selectedCalendarList[scheduleData.calendarId].category === "GROUP"
        ? groupUserList
            .filter((user) => user.selected)
            .map((user) => ({
              id: user.id,
              authority: user.permission,
              status: "PENDING",
              userId: user.userId,
              userNickname: user.userNickname || "",
            }))
        : [];
    return {
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
      repeatDto: isRepeatEnabled ? formatRepeatDetails(scheduleData.repeatDetails) : null,
      groupDto: groupDto,
    };
  };

  // 참여 여부 수정
  const handleParticipation = async (newStatus) => {
    try {
      const requestBody = {
        userId: currentUserId,
        status: newStatus, // "ACCEPTED", "DECLINED", "PENDING"
      };
      const response = await axios.patch(`/schedules/${scheduleData.id}/group`, requestBody, {
        withCredentials: true,
        headers: { "Content-Type": "application/json" },
      });
      const updatedGroup = response.data;
      setGroupUserList((prevList) =>
        prevList.map((user) =>
          user.userId === updatedGroup.userId
            ? { ...user, status: updatedGroup.status }
            : user
        )
      );
      setParticipationStatus(newStatus);
      alert("참여 여부가 정상적으로 반영되었습니다.");
    } catch (error) {
      console.error("Failed to update participation status:", error);
    }
  };

  // Save
  const handleSave = async () => {
    try {
      if (mode === "create") {
        await axios.post("/schedules", getScheduleData(), {
          withCredentials: true,
          headers: { "Content-Type": "application/json" },
        });
        alert("일정이 추가되었습니다.");
        onClose(true);
      } else if (mode === "edit") {
        if (scheduleData.repeatId) {
          openRepeatPopup("save");
        } else {
          await axios.patch(
            `/schedules/${scheduleData.id}?repeat=${isRepeatEnabled}`,
            getScheduleData(),
            {
              withCredentials: true,
              headers: { "Content-Type": "application/json" },
            }
          );
          alert("일정이 수정되었습니다.");
          onClose(true);
        }
      }
    } catch (error) {
      console.error("Error saving schedule:", error.response?.data || error.message);
      alert("Failed to save schedule.");
    }
  };

  // Delete
  const handleDelete = async () => {
    try {
      if (mode === "edit") {
        if (scheduleData.repeatId) {
          openRepeatPopup("delete");
        } else {
          await axios.delete(
            `/schedules/${scheduleData.id}/calendars/${scheduleData.calendarId}`,
            {
              withCredentials: true,
            }
          );
          alert("일정이 삭제되었습니다.");
          onClose(true);
        }
      }
    } catch (error) {
      alert("Failed to delete schedule.");
    }
  };

  // 반복 수정
  const repeatSave = async (url) => {
    try {
      if (mode === "edit" && scheduleData.repeatId) {
        await axios.patch(url, getScheduleData(), {
          withCredentials: true,
          headers: { "Content-Type": "application/json" },
        });
        alert("일정이 수정되었습니다.");
        onClose(true);
      }
    } catch (error) {
      console.error("Error saving schedule:", error.response?.data || error.message);
      alert("Failed to save schedule.");
    }
  };

  // 반복 삭제
  const repeatDelete = async (url) => {
    try {
      if (mode === "edit") {
        await axios.delete(url, {
          withCredentials: true,
        });
        alert("일정이 삭제되었습니다.");
        onClose(true);
      }
    } catch (error) {
      alert("Failed to delete schedule.");
    }
  };

  // 반복 팝업 확인
  const repeatConfirm = async (url) => {
    if (repeatPopupMode === "save") {
      repeatSave(url);
    } else if (repeatPopupMode === "delete") {
      repeatDelete(url);
    }
  };

  function openRepeatPopup(mode) {
    setRepeatPopupVisible(true);
    setRepeatPopupMode(mode);
  }
  function closeRepeatPopup() {
    setRepeatPopupVisible(false);
    setRepeatPopupMode("");
  }

  function closePopup() {
    onClose(false);
  }

  // 알림 추가/수정/삭제
  const handleAddNotification = () => {
    setScheduleData((prevState) => ({
      ...prevState,
      notifications: [...prevState.notifications, { time: 1, unit: "hours" }],
    }));
  };
  const handleUpdateNotification = (index, field, value) => {
    setScheduleData((prevState) => ({
      ...prevState,
      notifications: prevState.notifications.map((notification, i) =>
        i === index ? { ...notification, [field]: value } : notification
      ),
    }));
  };
  const handleRemoveNotification = (index) => {
    setScheduleData((prevState) => ({
      ...prevState,
      notifications: prevState.notifications.filter((_, i) => i !== index),
    }));
  };

  // 일반 입력 변경
  const handleInputChange = (field, value) => {
    setScheduleData((prev) => ({ ...prev, [field]: value }));
  };

  const handleIndividualSelect = (index, isSelected) => {
    setGroupUserList((prevList) => {
      const newList = [...prevList];
      newList[index] = { ...newList[index], selected: isSelected };
      return newList;
    });
  };
  const handlePermissionChange = (index, permission) => {
    setGroupUserList((prevList) => {
      const newList = [...prevList];
      newList[index] = { ...newList[index], permission };
      return newList;
    });
  };
  const handleRemoveGroupUser = (index) => {
    setGroupUserList((prevList) => {
      const newList = [...prevList];
      newList[index] = { ...newList[index], selected: false };
      return newList;
    });
  };

  const selectedUsers = groupUserList.filter((user) => user.selected);

  const getUserStatus = (status) => {
    switch (status) {
      case "ACCEPTED":
        return "참여";
      case "DECLINED":
        return "불참";
      case "PENDING":
      default:
        return "미정";
    }
  };

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

      <div className={styles.popupOverlay} onClick={closePopup}>
        <div className={styles.popup} onClick={(e) => e.stopPropagation()}>
          <div className={styles.popupHeader}>
            <h2>{mode === "edit" ? "일정 수정" : "일정 생성"}</h2>
            <Button variant="close" size="" onClick={closePopup}>
              ×
            </Button>
          </div>

          <fieldset
            disabled={isReadOnly && mode === "edit"}
            style={{ border: "none", padding: 0, margin: 0 }}
          >
            <div className={styles.popupContent}>
              <form>
                {/* 기본 일정 정보 */}
                <div className={styles.infoRow}>
                  <label>제목:</label>
                  <input
                    type="text"
                    value={scheduleData.title}
                    onChange={(e) => handleInputChange("title", e.target.value)}
                  />
                </div>
                <div className={styles.infoRow}>
                  <label>설명:</label>
                  <textarea
                    value={scheduleData.description}
                    onChange={(e) => handleInputChange("description", e.target.value)}
                  />
                </div>
                <div className={styles.infoRow}>
                  <label>시작 시간:</label>
                  <input
                    type="datetime-local"
                    value={scheduleData.startAt}
                    onChange={(e) => handleInputChange("startAt", e.target.value)}
                  />
                </div>
                <div className={styles.infoRow}>
                  <label>종료 시간:</label>
                  <input
                    type="datetime-local"
                    value={scheduleData.endAt}
                    onChange={(e) => handleInputChange("endAt", e.target.value)}
                  />
                </div>
                <div className={styles.infoRow}>
                  <label>캘린더:</label>
                  <select
                    value={scheduleData.calendarId}
                    onChange={(e) => handleInputChange("calendarId", e.target.value)}
                  >
                    {Object.entries(selectedCalendarList).map(([calendarId, calInfo]) => (
                      <option key={calendarId} value={calendarId}>
                        {calInfo.title}
                      </option>
                    ))}
                  </select>
                </div>

                {/* 알림 설정 */}
                <div className={styles.infoRow}>
                  <label>알림:</label>
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
                          <option value="minutes">분</option>
                          <option value="hours">시간</option>
                          <option value="days">일</option>
                        </select>
                        <Button variant="close" size="" onClick={() => handleRemoveNotification(index)}>×</Button>
                      </div>
                    ))}
                    <div className={styles.infoRow}>
                      <Button
                        variant="primary"
                        size="medium"
                        onClick={(e) => {
                          e.preventDefault();
                          handleAddNotification();
                        }}
                      >
                        알림 추가
                      </Button>
                    </div>
                  </div>
                )}

                {/* 반복 설정 */}
                <div className={styles.infoRow}>
                  <label>반복 설정:</label>
                  <Toggle
                    checked={isRepeatEnabled}
                    onChange={() => setIsRepeatEnabled(!isRepeatEnabled)}
                  />
                </div>
                {isRepeatEnabled && (
                  <div>
                    <div className={styles.infoRow}>
                      <label>반복 간격:</label>
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
                        <option value="DAY">일</option>
                        <option value="WEEK">주</option>
                        <option value="MONTH">월</option>
                        <option value="YEAR">년</option>
                      </select>
                    </div>
                    <div className={styles.infoRow}>
                      <label>반복 종료 일자:</label>
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

                {/* 그룹 캘린더인 경우 */}
                {selectedCalendarList[scheduleData.calendarId] &&
                  selectedCalendarList[scheduleData.calendarId].category === "GROUP" && (
                  <>
                    <div className={styles.infoRow}>
                      <label>일정 참석자 추가:</label>
                      <Toggle
                        checked={showGroupUsers}
                        onChange={() => {
                          const newShowValue = !showGroupUsers;
                          setShowGroupUsers(newShowValue);
                          if (!newShowValue) {
                            // 토글 끄면 모든 사용자의 선택 해제
                            setGroupUserList((prevList) =>
                              prevList.map((user) => ({ ...user, selected: false }))
                            );
                          }
                        }}
                        disabled={isReadOnly}
                      />
                    </div>
                    {showGroupUsers && (
                      <div style={{ marginTop: "10px" }}>
                        {(mode === "create" || canManageGroup) ? (
                          <>
                            {/* 관리 가능(ADMIN/소유자) 또는 create 모드 → Available + Selected */}
                            <div style={{ marginBottom: "5px" }}>
                              <div style={{ marginBottom: "5px" }}>참석 가능 인원:</div>
                              <div className={styles.availableUsersContainer}>
                                <table className={styles.userTable}>
                                  <thead><tr><th>선택</th><th>닉네임</th></tr></thead>
                                  <tbody>
                                    {groupUserList.filter((user) => !user.selected).length > 0 ? (
                                      groupUserList
                                        .filter((user) => !user.selected)
                                        .map((user, index) => (
                                          <tr key={user.id || index}>
                                            <td>
                                              <input
                                                type="checkbox"
                                                checked={user.selected || false}
                                                onChange={(e) =>
                                                  handleIndividualSelect(
                                                    groupUserList.findIndex(
                                                      (u) => u.userId === user.userId
                                                    ),
                                                    e.target.checked
                                                  )
                                                }
                                                disabled={isReadOnly}
                                              />
                                            </td>
                                            <td>{user.userNickname}</td>
                                          </tr>
                                        ))
                                    ) : (
                                      <tr><td colSpan="2">추가 가능한 사용자가 없습니다</td></tr>
                                    )}
                                  </tbody>
                                </table>
                              </div>
                            </div>
                            <div style={{ marginTop: "10px" }}>
                              <div style={{ marginBottom: "5px" }}>일정 참석자:</div>
                              <div className={styles.selectedUsersContainer}>
                                <table className={styles.userTable}>
                                  <thead><tr><th>닉네임</th><th>권한</th><th>상태</th><th style={{ textAlign: "center" }}>x</th></tr></thead>
                                  <tbody>
                                    {groupUserList.filter((user) => user.selected).map((user, index) => (
                                      <tr key={user.id || index}>
                                        <td>{user.userNickname}</td>
                                        <td>
                                          <select
                                            value={user.permission || "READ"}
                                            onChange={(e) =>
                                              handlePermissionChange(
                                                groupUserList.findIndex((u) => u.userId === user.userId),
                                                e.target.value
                                              )
                                            }
                                            disabled={isReadOnly || currentUserId === user.userId}
                                          >
                                            <option value="READ">읽기</option>
                                            <option value="WRITE">일정 수정</option>
                                            <option value="ADMIN">관리자</option>
                                          </select>
                                        </td>
                                        <td>{getUserStatus(user.status)}</td>
                                        <td>
                                          <Button
                                            variant="close"
                                            size="verySmall"
                                            type="button"
                                            onClick={() =>
                                              handleRemoveGroupUser(
                                                groupUserList.findIndex((u) => u.userId === user.userId)
                                              )
                                            }
                                            disabled={isReadOnly}
                                          >
                                            ×
                                          </Button>
                                        </td>
                                      </tr>
                                    ))}
                                  </tbody>
                                </table>
                              </div>
                            </div>
                          </>
                        ) : (
                          <>
                            {/* READ/WRITE인 경우 Selected만 */}
                            <div style={{ marginTop: "10px" }}>
                              <div style={{ marginBottom: "5px" }}>일정 참석자:</div>
                              <div className={styles.selectedUsersContainer}>
                                <table className={styles.userTable}>
                                  <thead><tr><th>닉네임</th><th>상태</th></tr></thead>
                                  <tbody>
                                    {groupUserList.filter((user) => user.selected).map((user, index) => (
                                      <tr key={user.id || index}>
                                        <td>{user.userNickname}</td>
                                        <td>{getUserStatus(user.status)}</td>
                                      </tr>
                                    ))}
                                  </tbody>
                                </table>
                              </div>
                            </div>
                          </>
                        )}
                      </div>
                    )}
                  </>
                )}
              </form>
            </div>
          </fieldset>

          {/* 현재 사용자가 Selected Users에 포함되면 참여 여부 버튼 노출 */}
          {selectedUsers.some(
            (user) =>
              user.userId === currentUserId &&
              user.status !== "Not selected"
          ) && (
            <div className={styles.googleFooter}>
              <span style={{ marginRight: "8px" }}>참여 여부:</span>
              <button
                className={`${styles.googleButton} ${
                  participationStatus === "ACCEPTED" ? styles.activeGoogleButton : ""
                }`}
                onClick={() => handleParticipation("ACCEPTED")}
              >
                예
              </button>
              <button
                className={`${styles.googleButton} ${
                  participationStatus === "DECLINED" ? styles.activeGoogleButton : ""
                }`}
                onClick={() => handleParticipation("DECLINED")}
              >
                아니오
              </button>
              <button
                className={`${styles.googleButton} ${
                  participationStatus === "PENDING" ? styles.activeGoogleButton : ""
                }`}
                onClick={() => handleParticipation("PENDING")}
              >
                미정
              </button>
            </div>
          )}

          {/* readOnly가 아니면 Save/Delete 버튼 노출 */}
          {!isReadOnly && (
            <div className={styles.popupFooter}>
              <Button variant="green" size="medium" onClick={handleSave} type="button">
                저장
              </Button>
              {mode === "edit" && (
                <Button variant="logout" size="medium" onClick={handleDelete} type="button">
                  삭제
                </Button>
              )}
            </div>
          )}
        </div>
      </div>
    </>
  );
};

export default SchedulePopup;