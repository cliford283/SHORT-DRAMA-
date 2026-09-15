import React from 'react';
import { X, Play, Heart, Star, Share2, Sparkles, Film } from 'lucide-react';

export default function DramaDetailModal({ 
  drama, 
  onClose, 
  onPlayEpisode, 
  isSaved, 
  onToggleWatchlist 
}) {
  if (!drama) return null;
  const episodes = drama.episodes || [];

  return (
    <div className="fixed inset-0 z-50 bg-black/80 backdrop-blur-md flex items-center justify-center p-3 sm:p-6 overflow-y-auto animate-fade-in">
      <div className="relative w-full max-w-3xl bg-[#141419] border border-white/10 rounded-2xl overflow-hidden shadow-2xl my-auto">
        
        {/* Close Button */}
        <button
          onClick={onClose}
          className="absolute top-4 right-4 z-20 p-2 rounded-full bg-black/60 hover:bg-black/90 text-white transition-colors border border-white/10"
        >
          <X className="w-5 h-5" />
        </button>

        {/* Hero Banner Header */}
        <div className="relative h-64 sm:h-80 w-full overflow-hidden bg-zinc-900">
          <img
            src={drama.coverUrl}
            alt={drama.title}
            className="w-full h-full object-cover object-top"
          />
          <div className="absolute inset-0 bg-gradient-to-t from-[#141419] via-[#141419]/70 to-transparent" />
          
          <div className="absolute bottom-4 left-4 sm:left-6 right-6 flex flex-col justify-end">
            <div className="flex flex-wrap items-center gap-2 mb-2">
              <span className="bg-red-600 text-white text-[10px] font-extrabold uppercase px-2 py-0.5 rounded tracking-wider shadow">
                {drama.genre}
              </span>
              <span className="flex items-center gap-1 text-amber-400 text-xs font-bold bg-black/60 px-2 py-0.5 rounded-full border border-amber-400/30">
                <Star className="w-3 h-3 fill-amber-400" />
                {drama.rating || 4.8}
              </span>
              <span className="text-xs text-zinc-300 bg-black/60 px-2 py-0.5 rounded-full">
                {drama.episodesCount || episodes.length} Episodes
              </span>
            </div>

            <h2 className="font-display font-black text-xl sm:text-3xl text-white drop-shadow">
              {drama.title}
            </h2>
          </div>
        </div>

        {/* Content Body */}
        <div className="p-4 sm:p-6 space-y-6 max-h-[55vh] overflow-y-auto">
          
          {/* Action Row */}
          <div className="flex flex-wrap items-center gap-3">
            <button
              onClick={() => onPlayEpisode(drama, episodes[0])}
              className="flex items-center gap-2 bg-red-600 hover:bg-red-700 text-white font-bold text-sm px-6 py-2.5 rounded-full shadow-lg shadow-red-600/30 transition-all"
            >
              <Play className="w-4 h-4 fill-white" />
              <span>Watch Episode 1</span>
            </button>

            <button
              onClick={() => onToggleWatchlist(drama)}
              className={`flex items-center gap-2 text-sm font-semibold px-5 py-2.5 rounded-full border transition-all ${
                isSaved 
                  ? 'bg-zinc-800 text-white border-zinc-600' 
                  : 'bg-white/5 hover:bg-white/10 text-zinc-300 border-white/10'
              }`}
            >
              <Heart className={`w-4 h-4 ${isSaved ? 'fill-red-500 text-red-500' : ''}`} />
              <span>{isSaved ? 'Saved in My List' : 'Add to My List'}</span>
            </button>
          </div>

          {/* Synopsis */}
          <div>
            <h4 className="text-xs uppercase font-extrabold tracking-wider text-zinc-400 mb-1.5">
              Synopsis
            </h4>
            <p className="text-zinc-300 text-xs sm:text-sm leading-relaxed">
              {drama.description}
            </p>
          </div>

          {/* Tags */}
          {drama.tags && drama.tags.length > 0 && (
            <div>
              <h4 className="text-xs uppercase font-extrabold tracking-wider text-zinc-400 mb-2">
                Themes & Tags
              </h4>
              <div className="flex flex-wrap gap-1.5">
                {drama.tags.map((tag, i) => (
                  <span 
                    key={i} 
                    className="text-[10px] uppercase font-bold tracking-wider text-zinc-400 bg-white/5 border border-white/10 px-2.5 py-1 rounded-full"
                  >
                    #{tag}
                  </span>
                ))}
              </div>
            </div>
          )}

          {/* Episode List Grid */}
          <div>
            <div className="flex items-center justify-between mb-3">
              <h4 className="text-xs uppercase font-extrabold tracking-wider text-zinc-400 flex items-center gap-1.5">
                <Film className="w-3.5 h-3.5 text-red-500" />
                <span>Episodes ({episodes.length})</span>
              </h4>
              <span className="text-[11px] text-zinc-500">Tap episode to stream</span>
            </div>

            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-2">
              {episodes.map((ep, idx) => (
                <button
                  key={ep.id || idx}
                  onClick={() => onPlayEpisode(drama, ep)}
                  className="flex items-center justify-between p-2.5 bg-zinc-900/90 hover:bg-zinc-800 border border-white/5 hover:border-red-500/40 rounded-xl text-left transition-colors group"
                >
                  <div className="flex items-center gap-2 truncate">
                    <span className="w-6 h-6 rounded bg-zinc-800 group-hover:bg-red-600 group-hover:text-white text-zinc-400 flex items-center justify-center text-xs font-bold shrink-0 transition-colors">
                      {ep.episodeNumber || (idx + 1)}
                    </span>
                    <span className="text-xs text-zinc-200 font-semibold truncate">
                      {ep.title}
                    </span>
                  </div>
                  <Play className="w-3 h-3 text-zinc-500 group-hover:text-red-400 shrink-0" />
                </button>
              ))}
            </div>
          </div>

        </div>
      </div>
    </div>
  );
}
