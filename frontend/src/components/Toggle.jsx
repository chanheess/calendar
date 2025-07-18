import React from "react";
import styles from "styles/Toggle.module.css";

const Toggle = ({ label, checked = false, onChange, disabled = false }) => {
  return (
    <label
      className={styles.customCheckbox}
      style={disabled ? { cursor: 'not-allowed', opacity: 0.5, pointerEvents: 'none' } : {}}
    >
      <input
        type="checkbox"
        checked={checked}
        onChange={onChange}
        className={styles.hiddenCheckbox}
        disabled={disabled}
      />
      <span className={styles.customCheckmark}></span>
      <span className={styles.labelText}>{label}</span>
    </label>
  );
};

export default Toggle;
