import React, { useState, useEffect } from "react";
import styles from "styles/Popup.module.css";
import Button from "../Button";
import axios from 'utils/axiosInstance';

const ManageCalendarPopup = ({
  isOpen,
  onClose,
  calendarInfo = {},
  selectedCalendarList,
  onCalendarChange,
}) => {
  const [inviteUserName, setInviteUserName] = useState("");
  const [userList, setUserList] = useState([]);
  const [confirmVisible, setConfirmVisible] = useState(false);
  const [pendingDelete, setPendingDelete] = useState(false);

  const [color, setColor] = useState(calendarInfo.color || "#3788d8");
  const [title, setTitle] = useState(calendarInfo.title || "");
  useEffect(() => {
    setColor(calendarInfo.color || "#3788d8");
    setTitle(calendarInfo.title || "");
  }, [calendarInfo.color, calendarInfo.title]);

  // Esc 키 눌렀을 때 팝업 닫기
  useEffect(() => {
    const handleEsc = (event) => {
      if (event.key === "Escape") {
        onClose();
      }
    };
    window.addEventListener("keydown", handleEsc);
    return () => window.removeEventListener("keydown", handleEsc);
  }, [onClose]);

  useEffect(() => {
    if (isOpen && calendarInfo.category === "GROUP" && calendarInfo.id) {
      loadUserList(calendarInfo.id);
    }
  }, [isOpen, calendarInfo.category, calendarInfo.id]);

  const loadUserList = async (groupId) => {
    try {
      const response = await axios.get(`/calendars/${groupId}/members`, {
        headers: { "Content-Type": "application/json" },
      });
      setUserList(response.data);
    } catch (error) {
      console.error("Error fetching user list:", error);
    }
  };

  // 안내문구 렌더 함수
  const renderDeleteGuide = () => {
    if (calendarInfo.category === 'GOOGLE') {
      return (
        <div style={{ color: '#868e96', fontSize: '12px', marginBottom: '4px' }}>
          1. 구글 캘린더에서 탈퇴해도 구글 계정 내 실제 캘린더와 일정은 삭제되지 않습니다.<br/>
          2. 탈퇴 시 더 이상 이 캘린더 및 일정에 접근할 수 없습니다.
        </div>
      );
    }
    if (calendarInfo.category === 'GROUP' && userList.length >= 2) {
      return (
        <div style={{ color: '#868e96', fontSize: '12px', marginBottom: '4px' }}>
          1. 그룹 캘린더에서 탈퇴하면 더 이상 이 캘린더 및 일정에 액세스할 수 없게 됩니다.<br/>
          2. 캘린더에 액세스할 수 있는 다른 사용자는 계속 사용할 수 있습니다.<br/>
          3. 관리자인 경우 권한이 다른 최고 권한자에게 이관됩니다.<br/>
          4. 그룹 내 인원이 1명일 경우 캘린더는 삭제됩니다.
        </div>
      );
    }
    return (
      <div style={{ color: '#868e96', fontSize: '12px', marginBottom: '4px' }}>
        탈퇴 시 더 이상 이 캘린더 및 일정에 접근할 수 없습니다.
      </div>
    );
  };

  const deleteCalendar = async () => {
    if (!calendarInfo.id) return;
    setPendingDelete(true);
    try {
      await axios.delete(`/calendars/${calendarInfo.id}`, { withCredentials: true });
      alert('캘린더에서 탈퇴되었습니다.');
      window.location.reload();
    } catch (error) {
      alert(error.response?.data?.message || '탈퇴에 실패했습니다.');
    } finally {
      setPendingDelete(false);
      setConfirmVisible(false);
    }
  };

  const inviteUserToCalendar = async () => {
    if (!inviteUserName.trim()) {
      alert("사용자 이름을 입력해주세요.");
      return;
    }
    try {
      await axios.post(
        `/notifications/calendars/${calendarInfo.id}/invite`,
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

  const updateCalendarInfo = async (type) => {
    if (!calendarInfo.id) return;
    let payload = { category: calendarInfo.category || "USER" };
    if (type === 'color') {
      payload.color = color;
    } else if (type === 'title') {
      payload.title = title;
    }
    try {
      const response = await axios.patch(
        `/calendars/${calendarInfo.id}`,
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
            title: response.data.title,
          },
        };
        onCalendarChange(updatedList); // 업데이트된 상태 전파
      }
      if (type === 'color') {
        alert("색상 변경 성공");
      } else if (type === 'title') {
        alert("이름 변경 성공");
      }
      onClose();
    } catch (error) {
      console.error("Error updating calendar info:", error);
      alert(error.response?.data.message || "변경 실패");
    }
  };

  if (!isOpen) return null;

  return (
    <div className={styles.popupOverlay} onClick={onClose}>
      <div className={styles.popup} onClick={(e) => e.stopPropagation()}>
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
            <h4>캘린더 이름</h4>
            <div className={styles.infoRow}>
              <input
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
              />
              <Button
                variant="primary"
                size="input"
                onClick={() => updateCalendarInfo('title')}
                disabled={title === (calendarInfo.title || "")}
              >
                이름 변경
              </Button>
            </div>
          </div>
          <div className={styles.formSection}>
            <h4>캘린더 색상</h4>
            <div className={styles.infoRow}>
              <input
                type="color"
                value={color}
                onChange={(e) => setColor(e.target.value)}
                className={styles.colorPicker}
              />
              <Button
                variant="primary"
                size="input"
                onClick={() => updateCalendarInfo('color')}
                disabled={color === (calendarInfo.color || "#3788d8")}
              >
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
            </>
          )}
        </div>
        <div style={{ textAlign: 'left', marginTop: '8px' }}>
          <button
            type="button"
            onClick={() => setConfirmVisible(true)}
            style={{ color: '#868e96', textDecoration: 'underline', fontSize: '13px', cursor: 'pointer', background: 'none', border: 'none', paddingLeft: 10 }}
          >
            캘린더 탈퇴
          </button>
        </div>
        {/* 커스텀 confirm 모달 */}
        {confirmVisible && (
          <div className={styles.confirmOverlay}>
            <div className={styles.confirmPopup}>
              <div className={styles.confirmTitle}>정말로 이 캘린더에서 탈퇴하시겠습니까?</div>
              <div className={styles.confirmGuide}>{renderDeleteGuide()}</div>
              <div className={styles.confirmFooter}>
                <Button variant="secondary" size="small" onClick={() => setConfirmVisible(false)} disabled={pendingDelete}>취소</Button>
                <Button variant="logout" size="small" onClick={deleteCalendar} disabled={pendingDelete}>탈퇴</Button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default ManageCalendarPopup;