import React, { useState, useEffect } from "react";
import styles from "styles/Popup.module.css";
import Button from "../Button";
import axios from "axios";

const ManageCalendarPopup = ({
  isOpen,
  onClose,
  calendarInfo = {},
  selectedCalendarList,
  onCalendarChange,
}) => {
  const [inviteUserName, setInviteUserName] = useState("");
  const [userList, setUserList] = useState([]);

  const [color, setColor] = useState(calendarInfo.color || "#3788d8");
  useEffect(() => {
    setColor(calendarInfo.color || "#3788d8");
  }, [calendarInfo.color]);

  useEffect(() => {
    if (isOpen && calendarInfo.category === "GROUP" && calendarInfo.id) {
      loadUserList(calendarInfo.id);
    }
  }, [isOpen, calendarInfo.category, calendarInfo.id]);

  const loadUserList = async (groupId) => {
    try {
      const response = await axios.get(`/groups/${groupId}/users`, {
        headers: { "Content-Type": "application/json" },
      });
      setUserList(response.data);
    } catch (error) {
      console.error("Error fetching user list:", error);
    }
  };

  const inviteUserToCalendar = async () => {
    if (!inviteUserName.trim()) {
      alert("사용자 이름을 입력해주세요.");
      return;
    }
    try {
      await axios.post(
        `/notifications/groups/${calendarInfo.id}/invite`,
        null,
        {
          params: { nickname: inviteUserName },
          withCredentials: true,
          headers: { "Content-Type": "application/json" },
        }
      );
      alert(`${inviteUserName} 초대 성공`);
      setInviteUserName("");
      loadUserList(calendarInfo.id);
    } catch (error) {
      console.error("Error inviting user:", error);
      alert(`${inviteUserName} 초대 실패`);
    }
  };

  const updateCalendarColor = async () => {
    if (!calendarInfo.id) return;

    const payload = {
      color,
      category: calendarInfo.category || "USER",
    };

    try {
      const response = await axios.patch(
        `/calendars/${calendarInfo.id}/color`,
        payload,
        {
          withCredentials: true,
          headers: { "Content-Type": "application/json" },
        }
      );

      if (selectedCalendarList && selectedCalendarList[response.data.calendarId]) {
        const updatedList = {
          ...selectedCalendarList,
          [response.data.calendarId]: {
            ...selectedCalendarList[response.data.calendarId],
            color: response.data.color,
          },
        };
        onCalendarChange(updatedList); // 업데이트된 상태 전파
      }

      alert("색상 변경 성공");
      onClose();
    } catch (error) {
      console.error("Error updating color:", error);
      alert("색상 변경 실패");
    }
  };

  if (!isOpen) return null;

  return (
    <div className={styles.popupOverlay}>
      <div className={styles.popup}>
        <div className={styles.popupHeader}>
          <h2>
            {calendarInfo.category === "GROUP"
              ? `${calendarInfo.title} 그룹 관리`
              : `${calendarInfo.title} 설정`}
          </h2>
          <Button variant="close" size="" onClick={onClose}>
            &times;
          </Button>
        </div>

        <div className={styles.popupContent}>
          <div className={styles.formSection}>
            <h4>캘린더 색상</h4>
            <div className={styles.infoRow}>
              <input
                type="color"
                value={color}
                onChange={(e) => setColor(e.target.value)}
                className={styles.colorPicker}
              />
              <Button variant="primary" size="input" onClick={updateCalendarColor}>
                색상 변경
              </Button>
            </div>
          </div>

          {calendarInfo.category === "GROUP" && (
            <>
              <h4>캘린더 초대</h4>
              <div className={styles.infoRow}>
                <input
                  type="text"
                  value={inviteUserName}
                  onChange={(e) => setInviteUserName(e.target.value)}
                  placeholder="사용자 이름 입력"
                />
                <Button variant="primary" size="input" onClick={inviteUserToCalendar}>
                  초대
                </Button>
              </div>

              <div className={styles.formSection}>
                <h4>캘린더 유저</h4>
                <div className={styles.userListContainer}>
                  {userList.length > 0 ? (
                    userList.map((user, index) => (
                      <div key={user.id || index} className={styles.userItem}>
                        {user.userNickname}
                      </div>
                    ))
                  ) : (
                    <p>등록된 유저가 없습니다.</p>
                  )}
                </div>
              </div>
              <hr />
            </>
          )}
        </div>
      </div>
    </div>
  );
};

export default ManageCalendarPopup;