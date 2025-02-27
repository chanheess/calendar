import React, { useEffect, useState } from "react";
import { getToken, onMessage } from "firebase/messaging";
import { messaging } from "./firebase";
import axios from "axios";
import styles from "styles/PushNotification.module.css";

const PushNotification = () => {
  const [notifications, setNotifications] = useState([]);

  useEffect(() => {
    const requestToken = async () => {
      try {
        const permission = await Notification.requestPermission();
        if (permission === "granted") {
          let token = localStorage.getItem("fcmToken");
          if (!token) {
            token = await getToken(messaging, {
              vapidKey: "BOOYYhMRpzdRL1n3Nnwm8jAhu1be-_tiMQKpCRPzBs4hXY85KB4yX9kR65__1hOB43Uj7ixfhHyPPSYA1NsNBSI"
            });
            if (token) {
              localStorage.setItem("fcmToken", token);
              await axios.post(`/notifications/token/${token}`);
            }
          }
        } else {
          // 권한 거부 시 기존 토큰 삭제
          const token = localStorage.getItem("fcmToken");
          if (token) {
            await axios.delete(`/notifications/token/${token}`);
            localStorage.removeItem("fcmToken");
          }
        }
      } catch (err) {
        console.error("Error getting FCM token:", err);
      }
    };

    requestToken();

    const unsubscribe = onMessage(messaging, (payload) => {
      const { title, body } = payload.data || {};
      const id = Date.now();
      const newNotification = {
        id,
        title: title || "알림",
        body: body || "",
      };
      setNotifications((prev) => [...prev, newNotification]);
    });

    return () => {
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