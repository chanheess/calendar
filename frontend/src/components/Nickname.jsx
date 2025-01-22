import React, { useState, useEffect } from "react";
import styles from "../styles/Nickname.module.css";

const Nickname = ({ variant = "primary", size = "medium", ...props }) => {
  const [nickname, setNickname] = useState(null);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchNickname = async () => {
          try {
            const response = await fetch("/user/nickname", {
              method: "GET",
              credentials: "include",
            });

            if (!response.ok) {
              throw new Error("Failed to fetch nickname");
            }

            const userNickname = await response.text();
            setNickname(userNickname);
          } catch (err) {
            setError("닉네임을 가져오지 못했습니다.");
          }
        };

        fetchNickname();
  }, []);

  // 렌더링 내용 결정
  const renderContent = () => {
    if (error) return error;
    if (nickname === null) return "Loading...";
    return nickname;
  };

  return (
    <p
      className={`${styles.nickname} ${styles[variant]} ${styles[size]} ${
        error ? styles.error : ""
      } ${nickname === null ? styles.loading : ""}`}
      {...props}
    >
      {renderContent()}
    </p>
  );
};

export default Nickname;
