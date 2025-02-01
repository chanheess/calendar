import React from "react";
import styles from "../styles/Popup.module.css";
import Button from "./Button"

const Popup = ({
  mode = "scheduleList", // Default mode
  title = "",
  size = "medium",
  children,
  onClose,
  actions = [],
  ...props
}) => {
  return (
    <div className={`${styles.popupOverlay} ${styles[size]}`} {...props}>
      <div className={styles.popup}>
        <div className={styles.popupHeader}>
          <span>{title}</span>
          <Button variant="close" size="none" onClick={onClose}>
            ×
          </Button>
        </div>
        <div className={styles.popupContent}>{children}</div>
        <div className={styles.popupFooter}>
          {actions.map((action, index) => (
            <button
              key={index}
              className={`${styles.popupButton} ${styles[action.variant || "default"]}`}
              onClick={action.onClick}
            >
              {action.label}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
};

export default Popup;
