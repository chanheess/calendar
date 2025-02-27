importScripts('https://www.gstatic.com/firebasejs/9.10.0/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/9.10.0/firebase-messaging-compat.js');

firebase.initializeApp({
  apiKey: "AIzaSyAKkDD2camVUqotmabXw-IQ3LlgkV-pIY4",
  authDomain: "chcalendar-61799.firebaseapp.com",
  projectId: "chcalendar-61799",
  storageBucket: "chcalendar-61799.firebasestorage.app",
  messagingSenderId: "529635326637",
  appId: "1:529635326637:web:2e251392ea9b7d26ace424",
});

const messaging = firebase.messaging();

self.addEventListener('install', (event) => {
  self.skipWaiting();
});

self.addEventListener('activate', (event) => {
  event.waitUntil(self.clients.claim());
});
// 백그라운드 메시지 처리
messaging.onBackgroundMessage((payload) => {
  console.log('Message received (background): ', payload);
  // payload.data 안에 title, body, url이 들어옴
  const { title, body, url } = payload.data || {};

  const notificationTitle = title || "알림";
  const notificationOptions = {
    body: body || "",
    icon: "/icon.png", // 필요 시 아이콘 경로
    data: { url }      // 클릭 시 열 URL
  };

  self.registration.showNotification(notificationTitle, notificationOptions);
});

// 알림 클릭 시 동작
self.addEventListener('notificationclick', (event) => {
  console.log('Notification click received:', event);
  event.notification.close();

  const clickAction = event.notification.data?.url;
  event.waitUntil(
    clients.matchAll({ type: 'window', includeUncontrolled: true }).then((clientList) => {
      for (const client of clientList) {
        if (client.url === clickAction && 'focus' in client) {
          return client.focus();
        }
      }
      if (clients.openWindow) {
        return clients.openWindow(clickAction);
      }
    })
  );
});