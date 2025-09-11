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

self.addEventListener('install', (event) => self.skipWaiting());
self.addEventListener('activate', (event) => event.waitUntil(self.clients.claim()));

async function sendAck({ notifyId, receivedAt, displayedAt }) {
  if (!notifyId) return;
  try {
    await fetch('/api/notifications/ack', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ notifyId, receivedAt, displayedAt })
    });
  } catch (e) {
  }
}

// 백그라운드 메시지 처리
messaging.onBackgroundMessage(async (payload) => {
  const data = payload?.data || {};
  const { title, body, url, notifyId } = data;

  const receivedAt = Date.now();
  sendAck({ notifyId, receivedAt });

  await self.registration.showNotification(title || '알림', {
    body: body || '',
    icon: '/icon.png',
    data: { url: url || '/', notifyId, receivedAt },
  });
});

// 알림 클릭
self.addEventListener('notificationclick', (event) => {
  event.notification.close();

  const { url = '/', notifyId, receivedAt } = event.notification.data || {};
  const displayedAt = Date.now();

  event.waitUntil(sendAck({ notifyId, receivedAt, displayedAt }));

  event.waitUntil(
    clients.matchAll({ type: 'window', includeUncontrolled: true }).then((clientList) => {
      const matched = clientList.find(c => c.url === url && 'focus' in c);
      if (matched) return matched.focus();
      return clients.openWindow(url);
    })
  );
});