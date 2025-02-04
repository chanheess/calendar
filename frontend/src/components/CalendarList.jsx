import React from "react";
import styles from "styles/Sidebar.module.css";
import Button from "./Button";

const CalendarList = ({
  title,
  calendars,
  sectionId,
  onCalendarSelection,
  onManageClick,
  selectedIds = [],
}) => {
  const handleCheckboxChange = (id) => {
    const updatedSelectedIds = selectedIds.includes(id)
      ? selectedIds.filter((calendarId) => calendarId !== id)
      : [...selectedIds, id];
    onCalendarSelection(updatedSelectedIds);
  };

  return (
    <div className={styles.calendarSection}>
      <h3 className={styles.sectionTitle}>{title}</h3>
      <ul id={sectionId} className={styles.calendarList}>
        {calendars.map((calendar) => (
          <li key={calendar.id} className={styles.calendarItem}>
            <input
              type="checkbox"
              checked={selectedIds.includes(calendar.id)}
              onChange={() => handleCheckboxChange(calendar.id)}
              id={`calendar-${calendar.id}`}
            />
            <label htmlFor={`calendar-${calendar.id}`}>{calendar.title}</label>

            {sectionId === "GROUP" && (
              <Button
                variant="function"
                size=""
                title="그룹 관리"
                onClick={() => onManageClick(calendar)}
              >
                ⚙️
              </Button>
            )}
          </li>
        ))}
      </ul>
    </div>
  );
};

export default CalendarList;
