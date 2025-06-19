import React from "react";
import styles from "styles/Button.module.css";

const Button = ({ children, variant = "primary", size = "medium", margin = "", padding = "", ...props }) => {
  return (
    <button
      className={
        `${styles.button} ${styles[variant]} ${styles[size]} ${styles[margin]} ${styles[padding]}${props.disabled ? ' ' + styles.disabled : ''}`
      }
      {...props}
    >
      {children}
    </button>
  );
};

export default Button;
