import React, { useState, useEffect } from "react";
import styles from "styles/Popup.module.css";
import Button from "../Button";
import axios from "axios";

const AddCalendarPopup = ({ isOpen, onClose, onCalendarAdded }) => {
  const [title, setTitle] = useState("");
  const [type, setType] = useState("USER");

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

  const handleCreateCalendar = async () => {
    if (!title.trim()) {
      alert("캘린더 제목을 입력해주세요.");
      return;
    }

    try {
      const response = await axios.post(
        "/calendars",
        { category: type, title: title },
        {
          withCredentials: true,
          headers: { "Content-Type": "application/json" },
        }
      );

      const calendarInfo = response.data;
      alert("캘린더가 추가되었습니다.");
      setTitle("");
      onClose();

      if (onCalendarAdded) onCalendarAdded(type, calendarInfo);
    } catch (error) {
      console.error("Error: ", error);
      const errorMessage = error.response?.data?.message || `오류 발생: ${error.response?.status}`;
      alert(errorMessage);
    }
  };

  if (!isOpen) return null;

  return (
    // 오버레이 클릭 시 onClose 호출
    <div className={styles.popupOverlay} onClick={onClose}>
      {/* 내부 팝업 클릭 시 전파 막기 */}
      <div className={styles.popup} onClick={(e) => e.stopPropagation()}>
        <div className={styles.popupHeader}>
          <h2>캘린더 생성</h2>
          <Button variant="close" size="" onClick={onClose}>
            ×
          </Button>
        </div>
        <div className={styles.popupContent}>
          <div className={styles.infoRow}>
            <label htmlFor="calendarTitle">제목:</label>
            <input
              type="text"
              id="calendarTitle"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              placeholder="캘린더 제목"
            />
          </div>
          <div className={styles.infoRow}>
            <label htmlFor="calendarType">유형:</label>
            <select
              id="calendarType"
              value={type}
              onChange={(e) => setType(e.target.value)}
            >
              <option value="USER">개인</option>
              <option value="GROUP">그룹</option>
            </select>
          </div>
        </div>
        <div className={styles.popupFooter}>
          <Button variant="primary" size="medium" onClick={handleCreateCalendar}>
            생성
          </Button>
        </div>
      </div>
    </div>
  );
};

export default AddCalendarPopup;