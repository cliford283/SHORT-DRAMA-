import {
  collection,
  doc,
  getDocs,
  getDoc,
  setDoc,
  updateDoc,
  deleteDoc,
  query,
  orderBy,
  serverTimestamp,
  arrayUnion,
  arrayRemove
} from 'firebase/firestore';
import { db } from './config';
import { INITIAL_DRAMAS } from '../data/initialDramas';

const DRAMAS_COLLECTION = 'dramas';
const USERS_COLLECTION = 'users';
const CONFIG_COLLECTION = 'app_data';

// Local storage key for fallback offline persistence if Firestore is temporarily offline
const LOCAL_DRAMAS_KEY = 'short_drama_local_cache';
const LOCAL_WATCHLIST_KEY = 'short_drama_watchlist';
const LOCAL_PROGRESS_KEY = 'short_drama_progress';

function getLocalDramas() {
  try {
    const raw = localStorage.getItem(LOCAL_DRAMAS_KEY);
    if (raw) return JSON.parse(raw);
  } catch (_) {}
  return null;
}

function setLocalDramas(dramas) {
  try {
    localStorage.setItem(LOCAL_DRAMAS_KEY, JSON.stringify(dramas));
  } catch (_) {}
}

export async function fetchAllDramas() {
  try {
    const colRef = collection(db, DRAMAS_COLLECTION);
    const snapshot = await getDocs(colRef);
    
    if (!snapshot.empty) {
      const list = snapshot.docs.map(docSnap => ({
        id: docSnap.id,
        ...docSnap.data()
      }));
      setLocalDramas(list);
      return list;
    }
  } catch (err) {
    console.warn('Firestore fetch failed, checking local cache or seeding:', err);
  }

  // Check cached or fallback to initial dataset
  const cached = getLocalDramas();
  if (cached && cached.length > 0) {
    return cached;
  }

  setLocalDramas(INITIAL_DRAMAS);
  return INITIAL_DRAMAS;
}

export async function seedInitialDramasIfEmpty() {
  try {
    const colRef = collection(db, DRAMAS_COLLECTION);
    const snapshot = await getDocs(colRef);
    if (snapshot.empty) {
      for (const drama of INITIAL_DRAMAS) {
        await setDoc(doc(db, DRAMAS_COLLECTION, drama.id), {
          ...drama,
          createdAt: serverTimestamp()
        });
      }
      console.log('Seeded initial dramas to Firestore');
    }
  } catch (e) {
    console.warn('Auto-seed check error (offline mode active):', e);
  }
}

// ADMIN ONLY: Create Drama
export async function createDrama(dramaData, currentUser) {
  if (!currentUser?.isAdmin) {
    throw new Error('Unauthorized: Only administrators can create dramas.');
  }

  const dramaId = dramaData.id || `drama_${Date.now()}`;
  const newDrama = {
    ...dramaData,
    id: dramaId,
    episodes: dramaData.episodes || [],
    episodesCount: dramaData.episodes ? dramaData.episodes.length : (dramaData.episodesCount || 0),
    rating: dramaData.rating || 4.9,
    views: dramaData.views || '10K',
    createdAt: new Date().toISOString()
  };

  try {
    const dramaRef = doc(db, DRAMAS_COLLECTION, dramaId);
    await setDoc(dramaRef, {
      ...newDrama,
      updatedAt: serverTimestamp()
    });
  } catch (err) {
    console.warn('Firestore write warning, writing to client state:', err);
  }

  // Update local cache
  const existing = getLocalDramas() || INITIAL_DRAMAS;
  const updated = [newDrama, ...existing.filter(d => d.id !== dramaId)];
  setLocalDramas(updated);

  return newDrama;
}

// ADMIN ONLY: Update Drama
export async function updateDrama(dramaId, dramaData, currentUser) {
  if (!currentUser?.isAdmin) {
    throw new Error('Unauthorized: Only administrators can update dramas.');
  }

  try {
    const dramaRef = doc(db, DRAMAS_COLLECTION, dramaId);
    await updateDoc(dramaRef, {
      ...dramaData,
      updatedAt: serverTimestamp()
    });
  } catch (err) {
    console.warn('Firestore update warning:', err);
  }

  const existing = getLocalDramas() || INITIAL_DRAMAS;
  const updated = existing.map(d => d.id === dramaId ? { ...d, ...dramaData } : d);
  setLocalDramas(updated);

  return { id: dramaId, ...dramaData };
}

// ADMIN ONLY: Delete Drama
export async function deleteDrama(dramaId, currentUser) {
  if (!currentUser?.isAdmin) {
    throw new Error('Unauthorized: Only administrators can delete dramas.');
  }

  try {
    const dramaRef = doc(db, DRAMAS_COLLECTION, dramaId);
    await deleteDoc(dramaRef);
  } catch (err) {
    console.warn('Firestore delete warning:', err);
  }

  const existing = getLocalDramas() || INITIAL_DRAMAS;
  const updated = existing.filter(d => d.id !== dramaId);
  setLocalDramas(updated);

  return true;
}

// ADMIN ONLY: Add Episode
export async function addEpisodeToDrama(dramaId, episodeData, currentUser) {
  if (!currentUser?.isAdmin) {
    throw new Error('Unauthorized: Only administrators can add episodes.');
  }

  const existing = getLocalDramas() || INITIAL_DRAMAS;
  const drama = existing.find(d => d.id === dramaId);
  if (!drama) throw new Error('Drama not found.');

  const epNum = episodeData.episodeNumber || (drama.episodes ? drama.episodes.length + 1 : 1);
  const newEpisode = {
    id: `${dramaId}_ep_${epNum}_${Date.now()}`,
    dramaId,
    episodeNumber: epNum,
    title: episodeData.title || `Episode ${epNum}`,
    videoUrl: episodeData.videoUrl,
    durationSeconds: episodeData.durationSeconds || 95,
    isUploaded: true,
    createdAt: new Date().toISOString()
  };

  const updatedEpisodes = [...(drama.episodes || []), newEpisode];
  const updatedDrama = {
    ...drama,
    episodes: updatedEpisodes,
    episodesCount: updatedEpisodes.length
  };

  try {
    const dramaRef = doc(db, DRAMAS_COLLECTION, dramaId);
    await updateDoc(dramaRef, {
      episodes: updatedEpisodes,
      episodesCount: updatedEpisodes.length,
      updatedAt: serverTimestamp()
    });
  } catch (err) {
    console.warn('Firestore episode addition error:', err);
  }

  const updatedList = existing.map(d => d.id === dramaId ? updatedDrama : d);
  setLocalDramas(updatedList);

  return newEpisode;
}

// ADMIN ONLY: Delete Episode
export async function deleteEpisodeFromDrama(dramaId, episodeId, currentUser) {
  if (!currentUser?.isAdmin) {
    throw new Error('Unauthorized: Only administrators can delete episodes.');
  }

  const existing = getLocalDramas() || INITIAL_DRAMAS;
  const drama = existing.find(d => d.id === dramaId);
  if (!drama) return;

  const updatedEpisodes = (drama.episodes || []).filter(e => e.id !== episodeId);
  const updatedDrama = {
    ...drama,
    episodes: updatedEpisodes,
    episodesCount: updatedEpisodes.length
  };

  try {
    const dramaRef = doc(db, DRAMAS_COLLECTION, dramaId);
    await updateDoc(dramaRef, {
      episodes: updatedEpisodes,
      episodesCount: updatedEpisodes.length,
      updatedAt: serverTimestamp()
    });
  } catch (err) {
    console.warn('Firestore episode delete error:', err);
  }

  const updatedList = existing.map(d => d.id === dramaId ? updatedDrama : d);
  setLocalDramas(updatedList);
  return true;
}

// USER & FIRESTORE WATCHLIST / FAVORITES
export async function getUserWatchlist(userId) {
  try {
    const userRef = doc(db, USERS_COLLECTION, userId);
    const snap = await getDoc(userRef);
    if (snap.exists() && snap.data().savedDramaIds) {
      return snap.data().savedDramaIds;
    }
  } catch (e) {
    console.warn('Could not fetch watchlist from Firestore, reading local:', e);
  }
  try {
    const local = localStorage.getItem(`${LOCAL_WATCHLIST_KEY}_${userId}`);
    if (local) return JSON.parse(local);
  } catch (_) {}
  return ['drama_mafia_don', 'drama_bugatti'];
}

export async function toggleUserWatchlist(userId, dramaId, isCurrentlySaved) {
  try {
    const userRef = doc(db, USERS_COLLECTION, userId);
    await updateDoc(userRef, {
      savedDramaIds: isCurrentlySaved ? arrayRemove(dramaId) : arrayUnion(dramaId)
    });
  } catch (e) {
    console.warn('Could not update watchlist in Firestore:', e);
  }

  try {
    const key = `${LOCAL_WATCHLIST_KEY}_${userId}`;
    const local = localStorage.getItem(key);
    let list = local ? JSON.parse(local) : ['drama_mafia_don', 'drama_bugatti'];
    if (isCurrentlySaved) {
      list = list.filter(id => id !== dramaId);
    } else {
      if (!list.includes(dramaId)) list.push(dramaId);
    }
    localStorage.setItem(key, JSON.stringify(list));
    return list;
  } catch (_) {}
  return [];
}

// USER WATCH PROGRESS
export async function saveWatchProgress(userId, dramaId, episodeId, seconds, completed = false) {
  const payload = {
    dramaId,
    episodeId,
    seconds: Math.round(seconds),
    completed,
    updatedAt: new Date().toISOString()
  };

  try {
    const userRef = doc(db, USERS_COLLECTION, userId);
    await updateDoc(userRef, {
      [`watchProgress.${dramaId}`]: payload
    });
  } catch (e) {
    console.warn('Could not update watch progress in Firestore:', e);
  }

  try {
    const key = `${LOCAL_PROGRESS_KEY}_${userId}`;
    const raw = localStorage.getItem(key);
    const map = raw ? JSON.parse(raw) : {};
    map[dramaId] = payload;
    localStorage.setItem(key, JSON.stringify(map));
  } catch (_) {}
}

export function getLocalWatchProgress(userId) {
  try {
    const raw = localStorage.getItem(`${LOCAL_PROGRESS_KEY}_${userId}`);
    return raw ? JSON.parse(raw) : {};
  } catch (_) {
    return {};
  }
}
