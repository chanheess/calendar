import { initializeApp } from "firebase/app";
import { getAnalytics } from "firebase/analytics";
import { getMessaging, getToken, onMessage } from "firebase/messaging";

const firebaseConfig = {
  apiKey: "AIzaSyAKkDD2camVUqotmabXw-IQ3LlgkV-pIY4",
  authDomain: "chcalendar-61799.firebaseapp.com",
  projectId: "chcalendar-61799",
  storageBucket: "chcalendar-61799.firebasestorage.app",
  messagingSenderId: "529635326637",
  appId: "1:529635326637:web:2e251392ea9b7d26ace424",
  measurementId: "G-M9MMZ2JKM2"
};

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const analytics = getAnalytics(app);
const messaging = getMessaging(app);

export const requestFirebaseNotificationPermission = async () => {
  try {
    const token = await getToken(messaging, { vapidKey: "BOOYYhMRpzdRL1n3Nnwm8jAhu1be-_tiMQKpCRPzBs4hXY85KB4yX9kR65__1hOB43Uj7ixfhHyPPSYA1NsNBSI" });
    return token;
  } catch (error) {
    console.error('FCM 토큰 가져오기 실패', error);
  }
};

export { app, analytics, messaging };
