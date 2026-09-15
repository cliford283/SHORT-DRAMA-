import { 
  createUserWithEmailAndPassword, 
  signInWithEmailAndPassword, 
  signOut, 
  updateProfile,
  onAuthStateChanged 
} from 'firebase/auth';
import { doc, getDoc, setDoc, serverTimestamp } from 'firebase/firestore';
import { auth, db } from './config';

// Authorized admin emails designated server-side and client-side
export const AUTHORIZED_ADMIN_EMAILS = [
  'clifordmulumba@gmail.com',
  'admin@shortdrama.tv'
];

export async function registerWithEmail(displayName, email, password) {
  try {
    const userCredential = await createUserWithEmailAndPassword(auth, email.trim(), password);
    const user = userCredential.user;

    await updateProfile(user, { displayName });

    const isAdmin = AUTHORIZED_ADMIN_EMAILS.includes(email.trim().toLowerCase());

    // Record verified user record in Firestore
    try {
      const userRef = doc(db, 'users', user.uid);
      await setDoc(userRef, {
        uid: user.uid,
        displayName: displayName || 'Drama Lover',
        email: user.email,
        role: isAdmin ? 'admin' : 'user',
        createdAt: serverTimestamp(),
        savedDramaIds: ['drama_mafia_don', 'drama_bugatti'],
        watchProgress: {}
      }, { merge: true });
    } catch (fsErr) {
      console.warn('Firestore user doc sync warning:', fsErr);
    }

    return { user, error: null };
  } catch (error) {
    console.error('Registration failed:', error);
    return { user: null, error: error.message };
  }
}

export async function loginWithEmail(email, password) {
  try {
    const userCredential = await signInWithEmailAndPassword(auth, email.trim(), password);
    const user = userCredential.user;
    
    // Fetch profile to verify role
    let role = AUTHORIZED_ADMIN_EMAILS.includes(email.trim().toLowerCase()) ? 'admin' : 'user';
    try {
      const userDoc = await getDoc(doc(db, 'users', user.uid));
      if (userDoc.exists() && userDoc.data().role) {
        role = userDoc.data().role;
      }
    } catch (e) {
      console.warn('Could not read user role from Firestore:', e);
    }

    return { user: { ...user, role }, error: null };
  } catch (error) {
    console.error('Login failed:', error);
    return { user: null, error: error.message };
  }
}

export async function logoutUser() {
  try {
    await signOut(auth);
    return { success: true };
  } catch (error) {
    return { success: false, error: error.message };
  }
}

export async function getUserRole(user) {
  if (!user || !user.email) return 'user';
  if (AUTHORIZED_ADMIN_EMAILS.includes(user.email.toLowerCase())) {
    return 'admin';
  }
  try {
    const userDoc = await getDoc(doc(db, 'users', user.uid));
    if (userDoc.exists() && userDoc.data().role === 'admin') {
      return 'admin';
    }
  } catch (err) {
    console.warn('Role check error:', err);
  }
  return 'user';
}

export function subscribeToAuth(callback) {
  return onAuthStateChanged(auth, async (user) => {
    if (user) {
      const role = await getUserRole(user);
      callback({
        uid: user.uid,
        email: user.email,
        displayName: user.displayName || user.email.split('@')[0],
        role: role,
        isAdmin: role === 'admin'
      });
    } else {
      callback(null);
    }
  });
}
