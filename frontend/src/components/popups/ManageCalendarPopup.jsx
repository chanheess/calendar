import React, { useState, useEffect } from "react";
import styles from "styles/Popup.module.css";
import Button from "../Button";
import axios from "axios";

const ManageCalendarPopup = ({ isOpen, onClose, calendarTitle, calendarId }) => {
  const [inviteUserName, setInviteUserName] = useState("");
  const [userList, setUserList] = useState([]);

  useEffect(() => {
    if (isOpen && calendarId) {
      loadUserList(calendarId);
    }
  }, [isOpen, calendarId]);


  const loadUserList = async (calendarId) => {
    try {
      const response = await axios.get(`/groups/${calendarId}/users`, {
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
        `/notifications/groups/${calendarId}/invite`,
        null, // 요청 바디가 필요 없으므로 null 전달
        {
          params: { nickname: inviteUserName }, // query parameters 방식
          withCredentials: true,
          headers: { "Content-Type": "application/json" },
        }
      );

      alert(`${inviteUserName} 초대 성공`);
      setInviteUserName("");
      loadUserList(calendarId);
    } catch (error) {
      console.error("Error inviting user:", error);
      alert(`${inviteUserName} 초대 실패`);
    }
  };


  if (!isOpen) return null;

  return (
    <div className={styles.popupOverlay}>
      <div className={styles.popup}>
        <div className={styles.popupHeader}>
          <h2>{calendarTitle} 관리</h2>
          <Button variant="close" size="" onClick={onClose}>
            &times;
          </Button>
        </div>

        <div className={styles.popupContent}>
          <h4>캘린더 초대</h4>
          <div className={styles.infoRow}>

            <input
              type="text"
              value={inviteUserName}
              onChange={(e) => setInviteUserName(e.target.value)}
              placeholder="사용자 이름 입력"
            />
            <Button
              variant="primary"
              size="input"
              onClick={inviteUserToCalendar}
            >
              초대
            </Button>
          </div>

          {/* 유저 리스트 */}
          <div className={styles.formSection}>
            <h4>캘린더 유저</h4>
            <div className={styles.userListContainer}>
              {userList.length > 0 ? (
                userList.map((user) => (
                  <div key={user.id} className={styles.userItem}>
                    {user.userNickname}
                  </div>
                ))
              ) : (
                <p>등록된 유저가 없습니다.</p>
              )}
            </div>
          </div>
        </div>

        <div className={styles.popupFooter}>
          <Button variant="logout" size="medium" onClick={onClose}>
            닫기
          </Button>
        </div>
      </div>
    </div>
  );
};

export default ManageCalendarPopup;
