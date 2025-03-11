import React, { useEffect, useRef, useState } from "react";
import { onMessage } from "firebase/messaging";
import { messaging } from "./firebase";
import axios from "axios";
import styles from "styles/PushNotification.module.css";
import { getFirebaseToken } from "components/FirebaseToken";

const PushNotification = () => {
  const [notifications, setNotifications] = useState([]);
  const tokenRequested = useRef(false); // 토큰 요청 여부를 저장하는 ref

  useEffect(() => {
    const requestToken = async () => {
      // 이미 토큰 요청한 경우 중복 실행 방지
      if (tokenRequested.current) return;
      tokenRequested.current = true;

      await Notification.requestPermission();

      try {
        const token = await getFirebaseToken();
        if (token) {
          await axios.post(`/notifications/token/${token}`);
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