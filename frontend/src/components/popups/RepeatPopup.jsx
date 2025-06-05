import React, { useState, useEffect } from "react";
import styles from "styles/Popup.module.css";
import Button from "components/Button";

const RepeatPopup = ({ isOpen, onClose, mode, scheduleId, calendarId, repeatCheck, onConfirm }) => {
  const [selectedOption, setSelectedOption] = useState("single");

  useEffect(() => {
    const handleEsc = (event) => {
      if (event.key === "Escape") {
        onClose();
      }
    };
    window.addEventListener("keydown", handleEsc);
    return () => window.removeEventListener("keydown", handleEsc);
  }, [onClose]);

  if (!isOpen) return null;

  const handleConfirm = () => {
    let url = "";

    if (mode === "delete") {
      if (selectedOption === "single") {
        url = `/schedules/${scheduleId}/current-only/calendars/${calendarId}`;
      } else if (selectedOption === "future") {
        url = `/schedules/${scheduleId}/current-and-future/calendars/${calendarId}`;
      }
    } else if (mode === "save") {
      if (selectedOption === "single") {
        url = `/schedules/${scheduleId}/current-only?repeat=${repeatCheck}`;
      } else if (selectedOption === "future") {
        url = `/schedules/${scheduleId}/current-and-future?repeat=${repeatCheck}`;
      }
    }

    onConfirm(url); // URL을 부모 컴포넌트로 전달
    onClose(); // 팝업 닫기
  };

  return (
    <div className={styles.popupOverlay} style={{ zIndex: 2001 }} onClick={onClose}>
      <div className={styles.popup} onClick={(e) => e.stopPropagation()} style={{ zIndex: 2002 }}>
        <div className={styles.popupHeader}>
          <h2>반복 일정 수정</h2>
          <Button variant="close" size="" onClick={onClose}>×</Button>
        </div>
        <div className={styles.popupContent}>
          <p>이 이벤트는 반복 일정의 일부입니다. 어떤 작업을 수행하시겠습니까?</p>

          <div className={styles.radioGroup}>
            <label className={styles.radioLabel}>
              <input
                type="radio"
                value="single"
                checked={selectedOption === "single"}
                onChange={() => setSelectedOption("single")}
              />
              이 일정
            </label>

            <label className={styles.radioLabel}>
              <input
                type="radio"
                value="future"
                checked={selectedOption === "future"}
                onChange={() => setSelectedOption("future")}
              />
              이 일정 및 향후 일정
            </label>
          </div>
        </div>

        <div className={styles.popupFooter}>
          <Button variant="green" size="medium" onClick={handleConfirm} type="button">
            저장
          </Button>
        </div>
      </div>
    </div>
  );
};

export default RepeatPopup;
