import React from 'react';
import { User, Shield, Bookmark, Film, LogOut, CheckCircle2, Award } from 'lucide-react';

export default function ProfilePage({ 
  currentUser, 
  watchlist = [], 
  onNavigateAdmin, 
  onLogout 
}) {
  return (
    <div className="min-h-screen bg-[#0a0a0c] px-4 sm:px-6 py-8 max-w-3xl mx-auto pb-24 space-y-6">
      
      {/* Header Profile Card */}
      <div className="bg-[#141419] border border-white/10 rounded-2xl p-6 relative overflow-hidden shadow-xl">
        <div className="absolute top-0 left-0 right-0 h-2 bg-gradient-to-r from-red-600 via-red-500 to-amber-500" />
        
        <div className="flex flex-col sm:flex-row items-center gap-5">
          <div className="w-20 h-20 rounded-2xl bg-gradient-to-tr from-red-700 to-red-500 flex items-center justify-center text-3xl font-black text-white shadow-xl shadow-red-600/30 uppercase">
            {currentUser?.displayName ? currentUser.displayName[0] : (currentUser?.email ? currentUser.email[0] : 'U')}
          </div>

          <div className="text-center sm:text-left flex-1">
            <div className="flex flex-wrap items-center justify-center sm:justify-start gap-2">
              <h2 className="text-xl font-bold text-white">
                {currentUser?.displayName || 'VIP Viewer'}
              </h2>
              {currentUser?.isAdmin ? (
                <span className="flex items-center gap-1 bg-amber-500/20 text-amber-400 border border-amber-500/40 text-[10px] font-extrabold px-2.5 py-0.5 rounded-full">
                  <Shield className="w-3 h-3" />
                  AUTHORIZED ADMIN
                </span>
              ) : (
                <span className="flex items-center gap-1 bg-red-600/20 text-red-400 border border-red-500/30 text-[10px] font-extrabold px-2.5 py-0.5 rounded-full">
                  <Award className="w-3 h-3" />
                  VIP MEMBER
                </span>
              )}
            </div>

            <p className="text-xs text-zinc-400 mt-1 font-mono">{currentUser?.email}</p>
            <p className="text-[11px] text-zinc-500 mt-0.5">Account UID: {currentUser?.uid?.slice(0, 16)}...</p>
          </div>
        </div>
      </div>

      {/* Metrics Row */}
      <div className="grid grid-cols-2 gap-4">
        <div className="bg-[#141419] border border-white/5 rounded-xl p-4 flex items-center gap-3.5">
          <div className="w-10 h-10 rounded-lg bg-red-600/20 text-red-400 flex items-center justify-center">
            <Bookmark className="w-5 h-5" />
          </div>
          <div>
            <span className="text-xl font-black text-white">{watchlist.length}</span>
            <p className="text-[11px] text-zinc-400">Saved In My List</p>
          </div>
        </div>

        <div className="bg-[#141419] border border-white/5 rounded-xl p-4 flex items-center gap-3.5">
          <div className="w-10 h-10 rounded-lg bg-amber-500/20 text-amber-400 flex items-center justify-center">
            <Film className="w-5 h-5" />
          </div>
          <div>
            <span className="text-xl font-black text-white">400+</span>
            <p className="text-[11px] text-zinc-400">Free Viral Episodes</p>
          </div>
        </div>
      </div>

      {/* Admin Action Banner */}
      {currentUser?.isAdmin && (
        <div className="bg-gradient-to-r from-amber-950/40 via-zinc-900 to-zinc-900 border border-amber-500/40 rounded-xl p-5 flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-3.5">
            <div className="w-10 h-10 rounded-lg bg-amber-500/20 text-amber-400 flex items-center justify-center shrink-0">
              <Shield className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-sm font-bold text-white">Admin Privileges Active</h3>
              <p className="text-xs text-zinc-400">Upload MP4 video files, edit drama catalogs & manage Firestore rules.</p>
            </div>
          </div>

          <button
            onClick={onNavigateAdmin}
            className="w-full sm:w-auto bg-amber-500 hover:bg-amber-400 active:scale-95 text-black font-extrabold text-xs px-5 py-2.5 rounded-full shadow-lg shadow-amber-500/30 transition-all shrink-0"
          >
            Open Admin Dashboard
          </button>
        </div>
      )}

      {/* Account Settings List */}
      <div className="bg-[#141419] border border-white/5 rounded-xl divide-y divide-white/5">
        <div className="p-4 flex items-center justify-between">
          <div>
            <p className="text-xs font-semibold text-white">Authentication Service</p>
            <p className="text-[11px] text-zinc-400">Secured via Firebase Auth & Firestore rules</p>
          </div>
          <span className="flex items-center gap-1 text-[11px] text-green-400 font-bold">
            <CheckCircle2 className="w-3.5 h-3.5" />
            Verified
          </span>
        </div>

        <div className="p-4 flex items-center justify-between">
          <div>
            <p className="text-xs font-semibold text-white">Cloud Storage Bucket</p>
            <p className="text-[11px] text-zinc-400">Real video and poster image streaming</p>
          </div>
          <span className="text-[11px] text-zinc-400 font-mono">Firebase Storage</span>
        </div>

        <div className="p-4">
          <button
            onClick={onLogout}
            className="w-full flex items-center justify-center gap-2 py-2.5 bg-red-600/10 hover:bg-red-600/20 text-red-400 rounded-lg text-xs font-bold border border-red-500/20 transition-colors"
          >
            <LogOut className="w-4 h-4" />
            <span>Sign Out of Account</span>
          </button>
        </div>
      </div>

    </div>
  );
}
