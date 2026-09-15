import React, { useState, useMemo } from 'react';
import HeroCarousel from '../components/HeroCarousel';
import DramaCard from '../components/DramaCard';
import { Search, Flame, Sparkles, Clock, Compass, Filter, Film } from 'lucide-react';

const GENRES = [
  'All',
  'Mafia Romance',
  'Fantasy Werewolf',
  'Billionaire Revenge',
  'Flash Marriage',
  'Country Billionaire',
  'Fantasy Magic',
  'Martial Arts Fantasy'
];

export default function HomePage({ 
  dramas = [], 
  watchlist = [], 
  onPlayDrama, 
  onSelectDrama, 
  onToggleWatchlist,
  searchQuery,
  setSearchQuery
}) {
  const [selectedGenre, setSelectedGenre] = useState('All');

  const filteredDramas = useMemo(() => {
    return dramas.filter(d => {
      const matchesGenre = selectedGenre === 'All' || d.genre?.toLowerCase().includes(selectedGenre.toLowerCase());
      const query = searchQuery.trim().toLowerCase();
      const matchesSearch = !query || 
        d.title.toLowerCase().includes(query) ||
        d.genre.toLowerCase().includes(query) ||
        d.description?.toLowerCase().includes(query) ||
        d.tags?.some(t => t.toLowerCase().includes(query));
      return matchesGenre && matchesSearch;
    });
  }, [dramas, selectedGenre, searchQuery]);

  const trendingDramas = useMemo(() => {
    return filteredDramas.filter(d => d.isTrending);
  }, [filteredDramas]);

  const newReleases = useMemo(() => {
    return filteredDramas.filter(d => d.isNewRelease || d.isFeatured);
  }, [filteredDramas]);

  return (
    <div className="min-h-screen bg-[#0a0a0c] pb-24">
      
      {/* Hero Carousel (Shown when not searching) */}
      {!searchQuery && (
        <HeroCarousel
          dramas={dramas}
          watchlist={watchlist}
          onPlayDrama={onPlayDrama}
          onSelectDrama={onSelectDrama}
          onToggleWatchlist={onToggleWatchlist}
        />
      )}

      {/* Main Content Container */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 pt-6 space-y-8">
        
        {/* Search Bar */}
        <div className="relative max-w-xl mx-auto">
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-zinc-400" />
          <input
            type="text"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            placeholder="Search viral dramas, billionaire, werewolf, mafia..."
            className="w-full pl-12 pr-4 py-3 bg-zinc-900/90 border border-white/10 rounded-full text-sm text-white placeholder-zinc-500 focus:outline-none focus:border-red-500 focus:ring-1 focus:ring-red-500 transition-all shadow-inner"
          />
          {searchQuery && (
            <button
              onClick={() => setSearchQuery('')}
              className="absolute right-4 top-1/2 -translate-y-1/2 text-xs text-zinc-400 hover:text-white bg-zinc-800 px-2 py-0.5 rounded-full"
            >
              Clear
            </button>
          )}
        </div>

        {/* Genre Filter Pills */}
        <div className="flex items-center gap-2 overflow-x-auto pb-2 scrollbar-none">
          {GENRES.map((g) => (
            <button
              key={g}
              onClick={() => setSelectedGenre(g)}
              className={`shrink-0 text-xs font-semibold px-4 py-2 rounded-full border transition-all ${
                selectedGenre === g
                  ? 'bg-red-600 text-white border-red-500 shadow-md shadow-red-600/30'
                  : 'bg-zinc-900/80 text-zinc-400 border-white/5 hover:text-white hover:bg-zinc-800'
              }`}
            >
              {g}
            </button>
          ))}
        </div>

        {/* If Search is Active */}
        {searchQuery ? (
          <div>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-base font-bold text-white flex items-center gap-2">
                <Search className="w-4 h-4 text-red-500" />
                <span>Results for "{searchQuery}" ({filteredDramas.length})</span>
              </h2>
            </div>
            {filteredDramas.length === 0 ? (
              <div className="py-16 text-center text-zinc-500">
                <Film className="w-12 h-12 mx-auto mb-2 opacity-40 text-zinc-400" />
                <p className="text-sm font-semibold text-zinc-400">No dramas match your search.</p>
                <p className="text-xs text-zinc-600 mt-1">Try another keyword like "Werewolf", "Mafia", or "Billionaire".</p>
              </div>
            ) : (
              <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-4 gap-4">
                {filteredDramas.map((drama) => (
                  <DramaCard
                    key={drama.id}
                    drama={drama}
                    isSaved={watchlist.includes(drama.id)}
                    onPlay={() => onPlayDrama(drama)}
                    onSelect={() => onSelectDrama(drama)}
                    onToggleWatchlist={onToggleWatchlist}
                  />
                ))}
              </div>
            )}
          </div>
        ) : (
          <>
            {/* Section 1: Trending Now */}
            <section>
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-lg sm:text-xl font-bold text-white flex items-center gap-2">
                  <Flame className="w-5 h-5 text-red-500 fill-red-500" />
                  <span>Trending Viral Series</span>
                </h2>
                <span className="text-xs text-zinc-400 font-semibold hover:text-red-400 cursor-pointer">
                  {selectedGenre}
                </span>
              </div>

              <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-4 gap-3 sm:gap-5">
                {(trendingDramas.length > 0 ? trendingDramas : filteredDramas).slice(0, 8).map((drama) => (
                  <DramaCard
                    key={drama.id}
                    drama={drama}
                    isSaved={watchlist.includes(drama.id)}
                    onPlay={() => onPlayDrama(drama)}
                    onSelect={() => onSelectDrama(drama)}
                    onToggleWatchlist={onToggleWatchlist}
                  />
                ))}
              </div>
            </section>

            {/* Section 2: New Releases */}
            <section className="pt-4">
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-lg sm:text-xl font-bold text-white flex items-center gap-2">
                  <Sparkles className="w-5 h-5 text-amber-400 fill-amber-400" />
                  <span>Fresh Releases & Exclusives</span>
                </h2>
              </div>

              <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-4 gap-3 sm:gap-5">
                {(newReleases.length > 0 ? newReleases : filteredDramas).slice(0, 8).map((drama) => (
                  <DramaCard
                    key={`new_${drama.id}`}
                    drama={drama}
                    isSaved={watchlist.includes(drama.id)}
                    onPlay={() => onPlayDrama(drama)}
                    onSelect={() => onSelectDrama(drama)}
                    onToggleWatchlist={onToggleWatchlist}
                  />
                ))}
              </div>
            </section>
          </>
        )}

      </div>
    </div>
  );
}
