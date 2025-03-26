import React, { useEffect } from "react";
import styles from "styles/Popup.module.css";
import Button from "../Button"

const Popup = ({
  mode = "scheduleList", // Default mode
  title = "",
  size = "medium",
  children,
  onClose,
  actions = [],
  ...props
}) => {
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

  return (
    <div className={`${styles.popupOverlay} ${styles[size]}`} {...props}>
      <div className={styles.popup}>
        <div className={styles.popupHeader}>
          <h2>{title}</h2>
          <Button variant="close" size="" onClick={onClose}>×</Button>
        </div>
        <div className={styles.popupContent}>
          {children}
        </div>
        <div className={styles.popupFooter}>
          {actions.map((action, index) => (
            <Button
              key={index}
              variant={action.variant}
              size={action.size}
              onClick={action.onClick}
            >
              {action.label}
            </Button>
          ))}
        </div>
      </div>
    </div>
  );
};

export default Popup;
