import React from 'react';
import DramaCard from '../components/DramaCard';
import { Bookmark, Heart, Film } from 'lucide-react';

export default function WatchlistPage({ 
  dramas = [], 
  watchlist = [], 
  onPlayDrama, 
  onSelectDrama, 
  onToggleWatchlist,
  onExplore 
}) {
  const savedDramas = dramas.filter(d => watchlist.includes(d.id));

  return (
    <div className="min-h-screen bg-[#0a0a0c] px-4 sm:px-6 py-6 max-w-7xl mx-auto pb-24 space-y-6">
      <div className="border-b border-white/10 pb-4">
        <h1 className="font-display font-black text-2xl sm:text-3xl text-white flex items-center gap-2">
          <Bookmark className="w-6 h-6 text-red-500" />
          <span>My Saved List</span>
        </h1>
        <p className="text-xs text-zinc-400 mt-1">
          {savedDramas.length} {savedDramas.length === 1 ? 'Drama' : 'Dramas'} saved to your personal library (Synced via Firestore)
        </p>
      </div>

      {savedDramas.length === 0 ? (
        <div className="py-24 text-center max-w-md mx-auto space-y-4">
          <div className="w-16 h-16 rounded-full bg-zinc-900 flex items-center justify-center mx-auto text-zinc-600 border border-white/5">
            <Heart className="w-8 h-8" />
          </div>
          <h2 className="text-lg font-bold text-white">Your list is empty</h2>
          <p className="text-xs text-zinc-400 leading-relaxed">
            Tap the heart icon on any drama poster to save it here for fast access and synchronized episode streaming.
          </p>
          <button
            onClick={onExplore}
            className="bg-red-600 hover:bg-red-700 text-white text-xs font-bold px-6 py-2.5 rounded-full shadow-lg shadow-red-600/30 transition-all"
          >
            Explore Viral Dramas
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-4 gap-4">
          {savedDramas.map((drama) => (
            <DramaCard
              key={drama.id}
              drama={drama}
              isSaved={true}
              onPlay={() => onPlayDrama(drama)}
              onSelect={() => onSelectDrama(drama)}
              onToggleWatchlist={onToggleWatchlist}
            />
          ))}
        </div>
      )}
    </div>
  );
}
