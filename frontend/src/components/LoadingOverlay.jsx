import React from "react";
import styles from "styles/LoadingOverlay.module.css";

const LoadingOverlay = ({ fullScreen = false }) => {
  const overlayClass = fullScreen
    ? styles.fullScreenOverlay
    : styles.containerOverlay;

  return (
    <div className={overlayClass}>
      <div className={styles.spinner}></div>
    </div>
  );
};

export default LoadingOverlay;