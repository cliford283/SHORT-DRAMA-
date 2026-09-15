import React, { useState, useEffect } from 'react';
import { Play, Plus, Check, Info, ChevronLeft, ChevronRight, Star, Flame } from 'lucide-react';

export default function HeroCarousel({ 
  dramas = [], 
  onPlayDrama, 
  onSelectDrama, 
  watchlist = [], 
  onToggleWatchlist 
}) {
  const featured = dramas.filter(d => d.isFeatured || d.isTrending).slice(0, 5);
  const [currentIndex, setCurrentIndex] = useState(0);

  useEffect(() => {
    if (featured.length <= 1) return;
    const timer = setInterval(() => {
      setCurrentIndex(prev => (prev + 1) % featured.length);
    }, 6000);
    return () => clearInterval(timer);
  }, [featured.length]);

  if (featured.length === 0) return null;
  const current = featured[currentIndex];
  const isSaved = watchlist.includes(current.id);

  const prevSlide = () => {
    setCurrentIndex(prev => (prev - 1 + featured.length) % featured.length);
  };

  const nextSlide = () => {
    setCurrentIndex(prev => (prev + 1) % featured.length);
  };

  return (
    <div className="relative w-full h-[520px] md:h-[580px] overflow-hidden bg-black select-none">
      
      {/* Background Poster Image with Cinematic Multi-Directional Gradient */}
      <div className="absolute inset-0">
        <img
          src={current.coverUrl}
          alt={current.title}
          className="w-full h-full object-cover object-center md:object-top transition-all duration-700 scale-105"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-[#0a0a0c] via-[#0a0a0c]/60 to-transparent" />
        <div className="absolute inset-0 bg-gradient-to-r from-[#0a0a0c] via-[#0a0a0c]/80 to-transparent" />
      </div>

      {/* Content Container */}
      <div className="relative z-10 max-w-7xl mx-auto h-full px-4 md:px-8 flex flex-col justify-end pb-12">
        <div className="max-w-2xl animate-fade-in">
          
          {/* Badges */}
          <div className="flex flex-wrap items-center gap-2 mb-3">
            <span className="flex items-center gap-1 bg-red-600 text-white text-[11px] font-extrabold px-2.5 py-0.5 rounded-full tracking-wider uppercase shadow-md shadow-red-600/40">
              <Flame className="w-3.5 h-3.5 fill-white" />
              {current.subtitle || "FEATURED DRAMA"}
            </span>

            <span className="bg-zinc-800/90 text-zinc-300 border border-zinc-700/80 text-[11px] font-medium px-2.5 py-0.5 rounded-full">
              {current.genre}
            </span>

            <span className="flex items-center gap-1 text-amber-400 text-xs font-bold bg-amber-500/10 px-2 py-0.5 rounded-full border border-amber-500/20">
              <Star className="w-3 h-3 fill-amber-400" />
              {current.rating || 4.9}
            </span>

            <span className="text-zinc-400 text-xs font-medium">
              {current.episodesCount || (current.episodes ? current.episodes.length : 0)} Episodes
            </span>
          </div>

          {/* Title */}
          <h1 className="font-display font-black text-2xl sm:text-4xl md:text-5xl text-white tracking-wide leading-tight mb-3 drop-shadow-lg">
            {current.title}
          </h1>

          {/* Synopsis */}
          <p className="text-zinc-300 text-xs sm:text-sm line-clamp-3 mb-6 leading-relaxed text-balance max-w-xl drop-shadow">
            {current.description}
          </p>

          {/* Tags */}
          {current.tags && current.tags.length > 0 && (
            <div className="flex flex-wrap gap-1.5 mb-6">
              {current.tags.slice(0, 4).map((tag, idx) => (
                <span 
                  key={idx} 
                  className="text-[10px] uppercase font-bold tracking-wider text-zinc-400 bg-white/5 border border-white/10 px-2 py-0.5 rounded"
                >
                  #{tag}
                </span>
              ))}
            </div>
          )}

          {/* Action Buttons */}
          <div className="flex items-center gap-3">
            <button
              onClick={() => onPlayDrama(current, current.episodes?.[0])}
              className="flex items-center gap-2 bg-red-600 hover:bg-red-700 active:scale-95 text-white font-bold text-sm px-6 py-3 rounded-full shadow-lg shadow-red-600/40 transition-all"
            >
              <Play className="w-4 h-4 fill-white" />
              <span>Watch Ep 1 Free</span>
            </button>

            <button
              onClick={() => onToggleWatchlist(current)}
              className={`flex items-center gap-2 font-semibold text-sm px-5 py-3 rounded-full border transition-all ${
                isSaved 
                  ? 'bg-zinc-800 text-white border-zinc-600' 
                  : 'bg-white/10 hover:bg-white/20 text-white border-white/20'
              }`}
            >
              {isSaved ? <Check className="w-4 h-4 text-green-400" /> : <Plus className="w-4 h-4" />}
              <span>{isSaved ? 'In My List' : 'Add to List'}</span>
            </button>

            <button
              onClick={() => onSelectDrama(current)}
              className="p-3 rounded-full bg-white/10 hover:bg-white/20 text-white transition-colors"
              title="Episodes & Details"
            >
              <Info className="w-4 h-4" />
            </button>
          </div>

        </div>
      </div>

      {/* Nav Arrows */}
      <button 
        onClick={prevSlide}
        className="hidden md:flex absolute left-4 top-1/2 -translate-y-1/2 z-20 w-10 h-10 rounded-full bg-black/40 hover:bg-black/70 border border-white/10 items-center justify-center text-white transition-colors"
      >
        <ChevronLeft className="w-6 h-6" />
      </button>

      <button 
        onClick={nextSlide}
        className="hidden md:flex absolute right-4 top-1/2 -translate-y-1/2 z-20 w-10 h-10 rounded-full bg-black/40 hover:bg-black/70 border border-white/10 items-center justify-center text-white transition-colors"
      >
        <ChevronRight className="w-6 h-6" />
      </button>

      {/* Slide Indicators */}
      <div className="absolute bottom-4 right-6 z-20 flex items-center gap-1.5">
        {featured.map((_, idx) => (
          <button
            key={idx}
            onClick={() => setCurrentIndex(idx)}
            className={`h-1.5 rounded-full transition-all ${
              idx === currentIndex ? 'w-6 bg-red-600' : 'w-2 bg-white/30'
            }`}
          />
        ))}
      </div>

    </div>
  );
}
