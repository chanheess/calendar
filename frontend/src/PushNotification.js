import { useEffect, useRef } from "react";
import { onMessage } from "firebase/messaging";
import { messaging } from "./firebase";
import axios from "utils/axiosInstance";
import { getFirebaseToken } from "components/FirebaseToken";
import platform from "platform";

const PushNotification = () => {
  const tokenRequestPromise = useRef(null);

  useEffect(() => {
    const ensurePermission = async () => {
      if (Notification.permission !== "granted") {
        await Notification.requestPermission();
      }
    };

    const registerTokenOnce = async () => {
      // 중복 요청 방지
      if (tokenRequestPromise.current) return tokenRequestPromise.current;

      tokenRequestPromise.current = (async () => {
        await ensurePermission();

        try {
          // 토큰 발급
          const token = await getFirebaseToken();
          const platformId =
            (platform.os?.family || "Unknown OS") +
            " " +
            (platform.name || "Unknown Browser");

          if (token) {
            await axios.post("/notifications/token", {
              firebaseToken: token,
              platformId,
            });
          }
        } catch (err) {
          console.error("[Push] get token failed:", err);
        } finally {
          tokenRequestPromise.current = null;
        }
      })();

      return tokenRequestPromise.current;
    };

    // 권한/토큰 등록
    registerTokenOnce();

    // 포그라운드 수신 → 서비스워커에 그대로 포워딩
    let unsubscribe = () => {};
    (async () => {
      try {
        const reg = await navigator.serviceWorker.ready; // 등록 보장
        unsubscribe = onMessage(messaging, (payload) => {
          const data = payload?.data || {};
          const target = reg.active || navigator.serviceWorker.controller;

          if (target) {
            // SW로 전달 → SW가 showNotification + ACK 처리
            target.postMessage({
              type: "FCM_FOREGROUND_TO_SW",
              payload: data,
            });
          } else {
            console.warn("[Push] No active service worker; using fallback");
            const title = data.title || "알림";
            const body = data.body || "";
            const url = data.url || "/";
            const notifyId = data.notifyId;

            // 브라우저 알림 (fallback)
            if (Notification.permission === "granted") {
              new Notification(title, { body, data: { url } });
            }

            // 간단 ACK (fallback)
            if (notifyId) {
              const receivedAt = Date.now();
              fetch("/api/notifications/ack", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "include",
                body: JSON.stringify({ notifyId, receivedAt }),
              }).catch(() => {});
            }
          }
        });
      } catch (e) {
        console.error("[Push] onMessage setup failed:", e);
      }
    })();

    return () => {
      unsubscribe && unsubscribe();
    };
  }, []);

  // OS 알림만 쓰므로 별도 UI 렌더링 없음
  return null;
};

export default PushNotification;