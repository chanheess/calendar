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

let API_BASE = self.location.origin;

self.addEventListener('message', (e) => {
  if (e.data?.type === 'SET_API_BASE' && e.data.value) {
    try {
      API_BASE = new URL(e.data.value, self.location.origin).origin;
    } catch (_) {
      API_BASE = self.location.origin;
    }
  }
});

// 공통 ACK
async function sendAck({ notifyId, receivedAt, displayedAt }) {
  if (!notifyId) return;

  try {
    await fetch(`${API_BASE}/api/notifications/ack`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      credentials: 'include',
      body: JSON.stringify({ notifyId, receivedAt, displayedAt })
    });
  } catch (e) {
    // 무시 (네트워크 이슈 등)
  }
}

messaging.onBackgroundMessage(async (payload) => {
  try {
    const data = payload?.data || {};
    const { title, body, url, notifyId } = data;

    const receivedAt = Date.now();

    await self.registration.showNotification(title || '알림', {
      body: body || '',
      icon: '/icon.png',
      data: { url: url || '/', notifyId, receivedAt },
    });

    sendAck({ notifyId, receivedAt }).catch(() => {});
  } catch (err) {
  }
});

self.addEventListener('message', (event) => {
  const msg = event?.data;
  if (!msg || msg.type !== 'FCM_FOREGROUND_TO_SW') return;

  const data = msg.payload || {};
  const { title, body, url, notifyId } = data;
  const receivedAt = Date.now();

  event.waitUntil((async () => {
    await self.registration.showNotification(title || '알림', {
      body: body || '',
      icon: '/icon.png',
      data: { url: url || '/', notifyId, receivedAt },
    });
    sendAck({ notifyId, receivedAt }).catch(() => {});
  })());
});

// 알림 클릭
self.addEventListener('notificationclick', (event) => {
  event.notification.close();

  const { url = '/', notifyId, receivedAt } = event.notification.data || {};
  const displayedAt = Date.now();

  // 표시 ACK + 클릭 처리 모두 waitUntil로 보장
  event.waitUntil((async () => {
    const targetHref = new URL(url || '/', self.location.origin).href;
    const focusOrOpen = (async () => {
      const clientList = await clients.matchAll({ type: 'window', includeUncontrolled: true });
      const matched = clientList.find(c => c.url === targetHref && 'focus' in c);
      if (matched) return matched.focus();
      return clients.openWindow(targetHref);
    })();
    const ack = sendAck({ notifyId, receivedAt, displayedAt });
    await Promise.allSettled([focusOrOpen, ack]);
  })());
});