export function register() {
  if ('serviceWorker' in navigator) {
    // 페이지가 로드될 때 서비스 워커 등록을 시도
    window.addEventListener('load', () => {
      navigator.serviceWorker
        .getRegistration()  // 이미 등록된 서비스 워커가 있는지 확인
        .then((registration) => {
          if (!registration) {
            // 서비스 워커가 등록되지 않았다면 새로 등록
            navigator.serviceWorker
              .register('/service-worker.js')
              .then((registration) => {
                console.log('Service Worker registered with scope:', registration.scope);
              })
              .catch((error) => {
                console.log('Service Worker registration failed:', error);
              });
          }
        });
    });
  }
}

export function unregister() {
  if ('serviceWorker' in navigator) {
    navigator.serviceWorker.ready
      .then((registration) => {
        registration.unregister();
      })
      .catch((error) => {
        console.log('Service Worker unregistration failed:', error);
      });
  }
}
