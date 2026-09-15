import React from 'react';
import { Play, Home, Compass, Bookmark, User, Shield, LogOut, Search } from 'lucide-react';

export default function Navbar({ 
  activeTab, 
  setActiveTab, 
  currentUser, 
  onLogout,
  onOpenSearch 
}) {
  return (
    <header className="sticky top-0 z-40 bg-[#0a0a0c]/90 backdrop-blur-md border-b border-white/10 transition-all">
      <div className="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between">
        
        {/* Brand Logo */}
        <div 
          onClick={() => setActiveTab('home')}
          className="flex items-center gap-2.5 cursor-pointer select-none group"
        >
          <div className="w-9 h-9 rounded-lg bg-gradient-to-tr from-red-700 to-red-500 flex items-center justify-center shadow-lg shadow-red-600/30 group-hover:scale-105 transition-transform">
            <Play className="w-5 h-5 text-white fill-white translate-x-0.5" />
          </div>
          <div>
            <div className="flex items-center gap-1.5">
              <span className="font-display font-black tracking-wider text-lg text-white">SHORT DRAMA</span>
              <span className="bg-red-600 text-white text-[9px] font-extrabold px-1.5 py-0.5 rounded tracking-widest">VIP</span>
            </div>
            <p className="text-[10px] text-zinc-400 font-medium tracking-wide">STREAM VIRAL REELS</p>
          </div>
        </div>

        {/* Desktop Navigation Links */}
        <nav className="hidden md:flex items-center gap-1 bg-white/5 p-1 rounded-full border border-white/10">
          <button
            onClick={() => setActiveTab('home')}
            className={`flex items-center gap-2 px-4 py-1.5 rounded-full text-xs font-semibold transition-all ${
              activeTab === 'home' 
                ? 'bg-red-600 text-white shadow-md shadow-red-600/30' 
                : 'text-zinc-300 hover:text-white hover:bg-white/10'
            }`}
          >
            <Home className="w-3.5 h-3.5" />
            <span>Home</span>
          </button>

          <button
            onClick={() => setActiveTab('browse')}
            className={`flex items-center gap-2 px-4 py-1.5 rounded-full text-xs font-semibold transition-all ${
              activeTab === 'browse' 
                ? 'bg-red-600 text-white shadow-md shadow-red-600/30' 
                : 'text-zinc-300 hover:text-white hover:bg-white/10'
            }`}
          >
            <Compass className="w-3.5 h-3.5" />
            <span>Browse</span>
          </button>

          <button
            onClick={() => setActiveTab('watchlist')}
            className={`flex items-center gap-2 px-4 py-1.5 rounded-full text-xs font-semibold transition-all ${
              activeTab === 'watchlist' 
                ? 'bg-red-600 text-white shadow-md shadow-red-600/30' 
                : 'text-zinc-300 hover:text-white hover:bg-white/10'
            }`}
          >
            <Bookmark className="w-3.5 h-3.5" />
            <span>My List</span>
          </button>

          {currentUser?.isAdmin && (
            <button
              onClick={() => setActiveTab('admin')}
              className={`flex items-center gap-2 px-4 py-1.5 rounded-full text-xs font-semibold transition-all ${
                activeTab === 'admin' 
                  ? 'bg-amber-500 text-black font-bold shadow-md shadow-amber-500/30' 
                  : 'text-amber-400 hover:text-amber-300 hover:bg-amber-400/10'
              }`}
            >
              <Shield className="w-3.5 h-3.5" />
              <span>Admin Panel</span>
            </button>
          )}
        </nav>

        {/* Right Actions: Search + Profile / Admin */}
        <div className="flex items-center gap-3">
          <button
            onClick={onOpenSearch}
            className="p-2 rounded-full text-zinc-400 hover:text-white hover:bg-white/10 transition-colors"
            title="Search Dramas"
          >
            <Search className="w-5 h-5" />
          </button>

          {currentUser?.isAdmin && (
            <button
              onClick={() => setActiveTab('admin')}
              className="md:hidden p-2 rounded-full text-amber-400 hover:bg-amber-400/10 transition-colors"
              title="Admin Dashboard"
            >
              <Shield className="w-5 h-5" />
            </button>
          )}

          {/* User Profile Pill */}
          <div 
            onClick={() => setActiveTab('profile')}
            className="flex items-center gap-2 pl-2 pr-3 py-1 bg-zinc-900/80 hover:bg-zinc-800 border border-zinc-700/60 rounded-full cursor-pointer transition-colors"
          >
            <div className="w-6 h-6 rounded-full bg-gradient-to-r from-red-600 to-red-400 flex items-center justify-center text-xs font-bold text-white uppercase">
              {currentUser?.displayName ? currentUser.displayName[0] : (currentUser?.email ? currentUser.email[0] : 'U')}
            </div>
            <span className="text-xs font-semibold text-zinc-200 max-w-[90px] truncate hidden sm:inline">
              {currentUser?.displayName || currentUser?.email?.split('@')[0]}
            </span>
            {currentUser?.isAdmin && (
              <span className="bg-amber-500/20 text-amber-400 border border-amber-500/30 text-[9px] font-bold px-1.5 py-0.2 rounded-full">
                ADMIN
              </span>
            )}
          </div>

          <button
            onClick={onLogout}
            className="p-2 rounded-full text-zinc-400 hover:text-red-400 hover:bg-red-500/10 transition-colors"
            title="Sign Out"
          >
            <LogOut className="w-4 h-4" />
          </button>
        </div>

      </div>

      {/* Mobile Bottom Navigation Bar (Phone First) */}
      <div className="md:hidden fixed bottom-0 left-0 right-0 z-50 bg-[#0a0a0c]/95 backdrop-blur-lg border-t border-white/10 px-4 py-2 flex items-center justify-around">
        <button
          onClick={() => setActiveTab('home')}
          className={`flex flex-col items-center gap-1 ${
            activeTab === 'home' ? 'text-red-500' : 'text-zinc-400'
          }`}
        >
          <Home className="w-5 h-5" />
          <span className="text-[10px] font-medium">Home</span>
        </button>

        <button
          onClick={() => setActiveTab('browse')}
          className={`flex flex-col items-center gap-1 ${
            activeTab === 'browse' ? 'text-red-500' : 'text-zinc-400'
          }`}
        >
          <Compass className="w-5 h-5" />
          <span className="text-[10px] font-medium">Browse</span>
        </button>

        <button
          onClick={() => setActiveTab('watchlist')}
          className={`flex flex-col items-center gap-1 ${
            activeTab === 'watchlist' ? 'text-red-500' : 'text-zinc-400'
          }`}
        >
          <Bookmark className="w-5 h-5" />
          <span className="text-[10px] font-medium">My List</span>
        </button>

        {currentUser?.isAdmin && (
          <button
            onClick={() => setActiveTab('admin')}
            className={`flex flex-col items-center gap-1 ${
              activeTab === 'admin' ? 'text-amber-400' : 'text-zinc-400'
            }`}
          >
            <Shield className="w-5 h-5" />
            <span className="text-[10px] font-medium">Admin</span>
          </button>
        )}

        <button
          onClick={() => setActiveTab('profile')}
          className={`flex flex-col items-center gap-1 ${
            activeTab === 'profile' ? 'text-red-500' : 'text-zinc-400'
          }`}
        >
          <User className="w-5 h-5" />
          <span className="text-[10px] font-medium">Profile</span>
        </button>
      </div>
    </header>
  );
}
