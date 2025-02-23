self.addEventListener('install', (event) => {
  console.log('Service Worker installed');
  // 서비스 워커 설치 시 필요한 파일을 캐시
  event.waitUntil(
    caches.open('my-cache-v1').then((cache) => {
      return cache.addAll([
        '/',
        '/index.html',
        '/app.js',
        '/style.css',
      ]);
    })
  );
});

self.addEventListener('activate', (event) => {
  console.log('Service Worker activated');
  // 기존 캐시를 정리하는 작업
  event.waitUntil(
    caches.keys().then((cacheNames) => {
      return Promise.all(
        cacheNames.map((cacheName) => {
          if (cacheName !== 'my-cache-v1') {
            return caches.delete(cacheName);
          }
        })
      );
    })
  );
});

// 푸시 알림 처리
self.addEventListener('push', (event) => {
  const options = {
    body: event.data.text(),
    icon: 'images/icon.png',
    badge: 'images/badge.png',
  };
  event.waitUntil(
    self.registration.showNotification('Push Notification', options)
  );
});
