import { getToken } from "firebase/messaging";
import { messaging } from "../firebase";

export async function getFirebaseToken() {
  try {
    const permission = Notification.permission;
    if (permission === "granted") {
      const token = await getToken(messaging, {
        vapidKey: "BOOYYhMRpzdRL1n3Nnwm8jAhu1be-_tiMQKpCRPzBs4hXY85KB4yX9kR65__1hOB43Uj7ixfhHyPPSYA1NsNBSI"
      });
      return token;
    } else {
      return null;
    }
  } catch (err) {
    return null;
  }
}