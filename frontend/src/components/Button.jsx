import React from "react";
import styles from "../styles/Button.module.css";

const Button = ({ children, variant = "primary", size = "medium", ...props }) => {
  return (
    <button
      className={`${styles.button} ${styles[variant]} ${styles[size]}`}
      {...props}
    >
      {children}
    </button>
  );
};

export default Button;
