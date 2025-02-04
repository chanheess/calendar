import React from "react";
import styles from "styles/Toggle.module.css";

const Toggle = ({ label, checked = false, onChange }) => {
  return (
    <label className={styles.customCheckbox}>
      <input
        type="checkbox"
        checked={checked}
        onChange={onChange}
        className={styles.hiddenCheckbox}
      />
      <span className={styles.customCheckmark}></span>
      <span className={styles.labelText}>{label}</span>
    </label>
  );
};

export default Toggle;
