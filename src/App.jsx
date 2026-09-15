import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import HomePage from './pages/HomePage';
import BrowsePage from './pages/BrowsePage';
import WatchlistPage from './pages/WatchlistPage';
import ProfilePage from './pages/ProfilePage';
import AdminDashboardPage from './pages/AdminDashboardPage';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import VideoPlayerModal from './components/VideoPlayerModal';
import DramaDetailModal from './components/DramaDetailModal';
import { subscribeToAuth, logoutUser } from './firebase/auth';
import { 
  fetchAllDramas, 
  seedInitialDramasIfEmpty, 
  getUserWatchlist, 
  toggleUserWatchlist 
} from './firebase/firestore';
import { INITIAL_DRAMAS } from './data/initialDramas';

export default function App() {
  // Auth state
  const [currentUser, setCurrentUser] = useState(null);
  const [authChecking, setAuthChecking] = useState(true);
  const [authView, setAuthView] = useState('login'); // 'login' or 'register'

  // App Navigation & Data
  const [activeTab, setActiveTab] = useState('home'); // 'home', 'browse', 'watchlist', 'profile', 'admin'
  const [dramas, setDramas] = useState(INITIAL_DRAMAS);
  const [watchlist, setWatchlist] = useState(['drama_mafia_don', 'drama_bugatti']);
  const [searchQuery, setSearchQuery] = useState('');

  // Active Modals
  const [activeVideoDrama, setActiveVideoDrama] = useState(null);
  const [activeVideoEpisode, setActiveVideoEpisode] = useState(null);
  const [activeDetailDrama, setActiveDetailDrama] = useState(null);

  // Subscribe to Firebase Auth
  useEffect(() => {
    const unsubscribe = subscribeToAuth((user) => {
      setCurrentUser(user);
      setAuthChecking(false);
      if (user) {
        // Load watchlist for authenticated user
        getUserWatchlist(user.uid).then(list => {
          if (list) setWatchlist(list);
        });
      }
    });
    return () => unsubscribe();
  }, []);

  // Fetch dramas from Firestore on load
  const loadDramas = async () => {
    try {
      const data = await fetchAllDramas();
      if (data && data.length > 0) {
        setDramas(data);
      }
    } catch (e) {
      console.warn('Error loading dramas:', e);
    }
  };

  useEffect(() => {
    loadDramas();
    seedInitialDramasIfEmpty();
  }, []);

  // Watchlist Toggle
  const handleToggleWatchlist = async (drama) => {
    if (!currentUser) return;
    const isSaved = watchlist.includes(drama.id);
    const updated = await toggleUserWatchlist(currentUser.uid, drama.id, isSaved);
    setWatchlist(updated);
  };

  // Play Episode
  const handlePlayDrama = (drama, episode = null) => {
    setActiveVideoDrama(drama);
    setActiveVideoEpisode(episode || drama.episodes?.[0] || null);
  };

  // Logout
  const handleLogout = async () => {
    await logoutUser();
    setCurrentUser(null);
    setActiveTab('home');
    setActiveVideoDrama(null);
    setActiveDetailDrama(null);
  };

  // 1. Loading screen while resolving Firebase session
  if (authChecking) {
    return (
      <div className="min-h-screen bg-[#0a0a0c] flex flex-col items-center justify-center">
        <div className="w-12 h-12 border-4 border-red-600 border-t-transparent rounded-full animate-spin mb-4" />
        <p className="text-xs font-semibold text-zinc-400 tracking-wider">
          LOADING SHORT DRAMA VIP...
        </p>
      </div>
    );
  }

  // 2. Auth Wall / Gate: Every visitor must register or sign in before accessing the main site
  if (!currentUser) {
    if (authView === 'register') {
      return (
        <RegisterPage
          onSwitchToLogin={() => setAuthView('login')}
          onRegisterSuccess={(user) => setCurrentUser(user)}
        />
      );
    }
    return (
      <LoginPage
        onSwitchToRegister={() => setAuthView('register')}
        onLoginSuccess={(user) => setCurrentUser(user)}
      />
    );
  }

  // 3. Authenticated App UI
  return (
    <div className="min-h-screen bg-[#0a0a0c] text-white flex flex-col selection:bg-red-600 selection:text-white">
      
      {/* Top Navbar */}
      <Navbar
        activeTab={activeTab}
        setActiveTab={setActiveTab}
        currentUser={currentUser}
        onLogout={handleLogout}
        onOpenSearch={() => {
          setActiveTab('home');
          window.scrollTo({ top: 380, behavior: 'smooth' });
        }}
      />

      {/* Main Screen Content */}
      <main className="flex-1">
        {activeTab === 'home' && (
          <HomePage
            dramas={dramas}
            watchlist={watchlist}
            onPlayDrama={handlePlayDrama}
            onSelectDrama={(drama) => setActiveDetailDrama(drama)}
            onToggleWatchlist={handleToggleWatchlist}
            searchQuery={searchQuery}
            setSearchQuery={setSearchQuery}
          />
        )}

        {activeTab === 'browse' && (
          <BrowsePage
            dramas={dramas}
            watchlist={watchlist}
            onPlayDrama={handlePlayDrama}
            onSelectDrama={(drama) => setActiveDetailDrama(drama)}
            onToggleWatchlist={handleToggleWatchlist}
          />
        )}

        {activeTab === 'watchlist' && (
          <WatchlistPage
            dramas={dramas}
            watchlist={watchlist}
            onPlayDrama={handlePlayDrama}
            onSelectDrama={(drama) => setActiveDetailDrama(drama)}
            onToggleWatchlist={handleToggleWatchlist}
            onExplore={() => setActiveTab('home')}
          />
        )}

        {activeTab === 'profile' && (
          <ProfilePage
            currentUser={currentUser}
            watchlist={watchlist}
            onNavigateAdmin={() => setActiveTab('admin')}
            onLogout={handleLogout}
          />
        )}

        {activeTab === 'admin' && (
          <AdminDashboardPage
            currentUser={currentUser}
            dramas={dramas}
            onRefreshDramas={loadDramas}
            onBack={() => setActiveTab('home')}
          />
        )}
      </main>

      {/* Video Player Modal */}
      {activeVideoDrama && (
        <VideoPlayerModal
          drama={activeVideoDrama}
          initialEpisode={activeVideoEpisode}
          currentUser={currentUser}
          onClose={() => {
            setActiveVideoDrama(null);
            setActiveVideoEpisode(null);
          }}
        />
      )}

      {/* Drama Detail & Episodes Modal */}
      {activeDetailDrama && (
        <DramaDetailModal
          drama={activeDetailDrama}
          isSaved={watchlist.includes(activeDetailDrama.id)}
          onClose={() => setActiveDetailDrama(null)}
          onPlayEpisode={(drama, episode) => {
            setActiveDetailDrama(null);
            handlePlayDrama(drama, episode);
          }}
          onToggleWatchlist={handleToggleWatchlist}
        />
      )}

    </div>
  );
}
