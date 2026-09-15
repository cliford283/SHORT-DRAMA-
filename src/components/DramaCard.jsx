import React from 'react';
import { Play, Heart, Star, Sparkles } from 'lucide-react';

export default function DramaCard({ 
  drama, 
  onPlay, 
  onSelect, 
  isSaved, 
  onToggleWatchlist 
}) {
  return (
    <div 
      className="group relative flex flex-col bg-[#141419] rounded-xl overflow-hidden border border-white/5 hover:border-red-600/50 transition-all duration-300 hover:shadow-xl hover:shadow-red-900/20 hover:-translate-y-1 cursor-pointer select-none"
      onClick={() => onSelect(drama)}
    >
      {/* Poster Image Container */}
      <div className="relative aspect-[3/4] w-full overflow-hidden bg-zinc-900">
        <img
          src={drama.coverUrl}
          alt={drama.title}
          loading="lazy"
          className="w-full h-full object-cover object-center group-hover:scale-105 transition-transform duration-500"
        />

        {/* Gradient Shadow */}
        <div className="absolute inset-0 bg-gradient-to-t from-[#141419] via-transparent to-black/30" />

        {/* Top Badges */}
        <div className="absolute top-2 left-2 right-2 flex items-center justify-between">
          <span className="bg-red-600/90 text-white text-[9px] font-black uppercase px-2 py-0.5 rounded tracking-wide shadow">
            {drama.genre?.split(' ')[0] || 'DRAMA'}
          </span>

          <button
            onClick={(e) => {
              e.stopPropagation();
              onToggleWatchlist(drama);
            }}
            className={`p-1.5 rounded-full backdrop-blur-md transition-all ${
              isSaved 
                ? 'bg-red-600 text-white' 
                : 'bg-black/50 text-white/80 hover:text-white hover:bg-black/80'
            }`}
            title={isSaved ? "Remove from List" : "Save to List"}
          >
            <Heart className={`w-3.5 h-3.5 ${isSaved ? 'fill-white' : ''}`} />
          </button>
        </div>

        {/* Play Overlay Icon */}
        <div 
          onClick={(e) => {
            e.stopPropagation();
            onPlay(drama);
          }}
          className="absolute inset-0 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity bg-black/40"
        >
          <div className="w-12 h-12 rounded-full bg-red-600 text-white flex items-center justify-center shadow-lg shadow-red-600/50 transform scale-75 group-hover:scale-100 transition-transform">
            <Play className="w-5 h-5 fill-white translate-x-0.5" />
          </div>
        </div>

        {/* Bottom Poster Bar */}
        <div className="absolute bottom-2 left-2 right-2 flex items-center justify-between text-[11px] font-semibold text-white/90 drop-shadow">
          <span className="bg-black/60 backdrop-blur-sm px-1.5 py-0.5 rounded text-[10px]">
            {drama.episodesCount || (drama.episodes ? drama.episodes.length : 0)} Eps
          </span>
          <span className="flex items-center gap-1 text-amber-400 bg-black/60 backdrop-blur-sm px-1.5 py-0.5 rounded text-[10px]">
            <Star className="w-2.5 h-2.5 fill-amber-400" />
            {drama.rating || 4.8}
          </span>
        </div>
      </div>

      {/* Meta Content */}
      <div className="p-3 flex flex-col justify-between flex-grow">
        <div>
          <h3 className="font-semibold text-xs sm:text-sm text-zinc-100 line-clamp-1 group-hover:text-red-400 transition-colors">
            {drama.title}
          </h3>
          <p className="text-[11px] text-zinc-400 line-clamp-2 mt-1 leading-snug">
            {drama.description}
          </p>
        </div>

        {/* Tags */}
        <div className="mt-2.5 flex items-center justify-between text-[10px] text-zinc-400 pt-2 border-t border-white/5">
          <span className="truncate max-w-[120px] text-zinc-400">
            {drama.genre}
          </span>
          <span className="text-red-400 font-medium hover:underline">
            Watch &gt;
          </span>
        </div>
      </div>
    </div>
  );
}
