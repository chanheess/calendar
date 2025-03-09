import React from "react";
import styles from "styles/Sidebar.module.css";
import Button from "./Button";

const CalendarList = ({
  title,
  calendars,
  sectionId,
  onCalendarSelection,
  onManageClick,
}) => {
  const handleCheckboxChange = (id, e) => {
    onCalendarSelection(String(id), e.target.checked);
  };

  return (
    <div className={styles.calendarSection}>
      <h3 className={styles.sectionTitle}>{title}</h3>
      <ul id={sectionId} className={styles.calendarList}>
        {calendars.map((calendar) => (
          <li key={calendar.id} className={styles.calendarItem}>
            <input
              type="checkbox"
              checked={calendar.isSelected}
              onChange={(e) => handleCheckboxChange(calendar.id, e)}
              id={`calendar-${calendar.id}`}
              style={{ accentColor: calendar.color }}
            />
            <label htmlFor={`calendar-${calendar.id}`}>{calendar.title}</label>
            <Button
              variant="function"
              size="medium"
              margin="marginNone"
              padding="paddingNone"
              title="설정"
              onClick={() => onManageClick(calendar, sectionId)}
            >
              ⋮
            </Button>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default CalendarList;