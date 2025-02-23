import React, { useEffect, useRef } from 'react';
import axios from "axios";

function PushNotification() {
  const didInitialize = useRef(false);

  useEffect(() => {
    // 첫 마운트 시점에만 실행
    if (didInitialize.current) return;
    didInitialize.current = true;

    (async () => {
      if ('Notification' in window && 'serviceWorker' in navigator) {
        const registration = await navigator.serviceWorker.ready;
        const permission = Notification.permission;
        const subscription = await registration.pushManager.getSubscription();

        if (permission === 'denied') {
          if (subscription) {
            await unsubscribeAndNotifyServer(subscription);
          }
          return;
        }

        if (permission === 'default') {
          const newPermission = await Notification.requestPermission();
          if (newPermission === 'granted') {
            const sub = await registration.pushManager.getSubscription();
            if (!sub) {
              await createSubscription(registration);
            }
          } else {
            const sub = await registration.pushManager.getSubscription();
            if (sub) {
              await unsubscribeAndNotifyServer(sub);
            }
          }
          return;
        }

        if (permission === 'granted') {
          if (!subscription) {
            await createSubscription(registration);
          }
        }
      }
    })();
  }, []);

  // 구독 생성 함수
  const createSubscription = async (registration) => {
    const pushSubscription = await registration.pushManager.subscribe({
      userVisibleOnly: true,
      applicationServerKey: process.env.REACT_APP_VAPID_PUBLIC_KEY,
    });

    // ArrayBuffer → Base64 변환
    const p256dhArrayBuffer = pushSubscription.getKey('p256dh');
    const authArrayBuffer   = pushSubscription.getKey('auth');
    const p256dh = arrayBufferToBase64Url(p256dhArrayBuffer);
    const auth   = arrayBufferToBase64Url(authArrayBuffer);

    const pushSubscriptionDto = {
      endpoint: pushSubscription.endpoint,
      p256dhKey: p256dh,
      authKey: auth
    };

    // 서버에 구독 정보 전송
    await axios.post('/web-push/subscribe', pushSubscriptionDto, {
      headers: {'Content-Type': 'application/json'}
    });
  };

  const unsubscribeAndNotifyServer = async (subscription) => {
    try {
      console.log("Unsubscribe:", subscription.endpoint);
      await axios.delete('/web-push/subscribe', {
        data: { endpoint: subscription.endpoint }
      });
      await subscription.unsubscribe();
    } catch (err) {
      console.error("Error unsubscribing:", err);
    }
  };

  // ArrayBuffer → Base64 변환
  function arrayBufferToBase64Url(buffer) {
    const bytes = new Uint8Array(buffer);
    let binary = '';
    bytes.forEach((b) => (binary += String.fromCharCode(b)));
    let base64 = window.btoa(binary);
    // URL-safe 변환
    base64 = base64.replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
    return base64;
  }

  return null; // 컴포넌트는 UI를 렌더링하지 않음
}

export default PushNotification;
