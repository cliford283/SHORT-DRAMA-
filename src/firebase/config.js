import { initializeApp, getApps, getApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';
import { getFirestore } from 'firebase/firestore';
import { getStorage } from 'firebase/storage';

// Default Firebase Configuration with support for Vite environment variables
export const firebaseConfig = {
  apiKey: import.meta.env.VITE_FIREBASE_API_KEY || "AIzaSyDemoPlaceholderKeyForShortDramaApp01",
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN || "short-drama-production.firebaseapp.com",
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID || "short-drama-production",
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET || "short-drama-production.appspot.com",
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID || "103948291029",
  appId: import.meta.env.VITE_FIREBASE_APP_ID || "1:103948291029:web:a9d384729104bc8d"
};

// Singleton App Instance
const app = getApps().length > 0 ? getApp() : initializeApp(firebaseConfig);

export const auth = getAuth(app);
export const db = getFirestore(app);
export const storage = getStorage(app);

export default app;
