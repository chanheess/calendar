// src/PushNotification.js
import React, { useEffect, useState } from "react";
import { getToken, onMessage } from "firebase/messaging";
import { messaging } from "./firebase";
import axios from "axios";
import styles from "styles/PushNotification.module.css";

const PushNotification = () => {
  const [notifications, setNotifications] = useState([]);

  useEffect(() => {
    console.log("PushNotification mounted");

    // 토큰 요청 및 관리 함수
    const requestToken = async () => {
      try {
        const permission = await Notification.requestPermission();
        console.log("Notification permission:", permission);
        if (permission === "granted") {
          let token = localStorage.getItem("fcmToken");
          if (!token) {
            token = await getToken(messaging, {
              vapidKey: "BOOYYhMRpzdRL1n3Nnwm8jAhu1be-_tiMQKpCRPzBs4hXY85KB4yX9kR65__1hOB43Uj7ixfhHyPPSYA1NsNBSI"
            });
            if (token) {
              localStorage.setItem("fcmToken", token);
              console.log("New FCM Token:", token);
              await axios.post(`/notifications/token/${token}`);
            }
          } else {
            console.log("Existing FCM Token:", token);
          }
        } else {
          // 권한 거부 시 기존 토큰 삭제
          const token = localStorage.getItem("fcmToken");
          if (token) {
            await axios.delete(`/notifications/token/${token}`);
            localStorage.removeItem("fcmToken");
            console.log("FCM Token removed due to permission change");
          }
        }
      } catch (err) {
        console.error("Error getting FCM token:", err);
      }
    };

    requestToken();

    // 포그라운드 메시지 처리 (data-only 메시지여야 onMessage가 호출됩니다)
    const unsubscribe = onMessage(messaging, (payload) => {
      console.log("Message received (foreground):", payload);
      const { title, body } = payload.data || {};
      // 고유 id는 timestamp를 사용합니다.
      const id = Date.now();
      const newNotification = {
        id,
        title: title || "알림",
        body: body || "",
      };
      setNotifications((prev) => [...prev, newNotification]);
      // 자동 제거 setTimeout 제거 → 사용자가 클릭할 때만 제거
    });

    return () => {
      console.log("Unsubscribing from onMessage");
      unsubscribe();
    };
  }, []);

  const handleClick = (id) => {
    setNotifications((prev) => prev.filter((n) => n.id !== id));
  };

  return (
    <div className={styles.container}>
      {notifications.map((notification) => (
        <div
          key={notification.id}
          className={styles.toast}
          onClick={() => handleClick(notification.id)}
        >
          <strong>{notification.title}</strong>
          <p>{notification.body}</p>
        </div>
      ))}
    </div>
  );
};

export default PushNotification;