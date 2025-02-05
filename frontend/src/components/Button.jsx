import React from "react";
import styles from "styles/Button.module.css";

const Button = ({ children, variant = "primary", size = "medium", margin = "", ...props }) => {
  return (
    <button
      className={`${styles.button} ${styles[variant]} ${styles[size]} ${styles[margin]}`}
      {...props}
    >
      {children}
    </button>
  );
};

export default Button;
