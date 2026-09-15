import React, { useState, useMemo } from 'react';
import DramaCard from '../components/DramaCard';
import { Compass, Search, SlidersHorizontal } from 'lucide-react';

const CATEGORIES = [
  'All',
  'Mafia Romance',
  'Fantasy Werewolf',
  'Billionaire Revenge',
  'Flash Marriage',
  'Country Billionaire',
  'Fantasy Magic',
  'Martial Arts Fantasy'
];

export default function BrowsePage({ 
  dramas = [], 
  watchlist = [], 
  onPlayDrama, 
  onSelectDrama, 
  onToggleWatchlist 
}) {
  const [selectedCat, setSelectedCat] = useState('All');
  const [query, setQuery] = useState('');
  const [sortBy, setSortBy] = useState('rating'); // 'rating', 'episodes', 'title'

  const filtered = useMemo(() => {
    return dramas
      .filter(d => {
        const matchesCat = selectedCat === 'All' || d.genre?.toLowerCase().includes(selectedCat.toLowerCase());
        const q = query.trim().toLowerCase();
        const matchesQuery = !q || 
          d.title.toLowerCase().includes(q) || 
          d.genre.toLowerCase().includes(q) || 
          d.description?.toLowerCase().includes(q);
        return matchesCat && matchesQuery;
      })
      .sort((a, b) => {
        if (sortBy === 'rating') return (b.rating || 4.5) - (a.rating || 4.5);
        if (sortBy === 'episodes') return (b.episodesCount || 0) - (a.episodesCount || 0);
        return a.title.localeCompare(b.title);
      });
  }, [dramas, selectedCat, query, sortBy]);

  return (
    <div className="min-h-screen bg-[#0a0a0c] px-4 sm:px-6 py-6 max-w-7xl mx-auto pb-24 space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b border-white/10 pb-4">
        <div>
          <h1 className="font-display font-black text-2xl sm:text-3xl text-white flex items-center gap-2">
            <Compass className="w-6 h-6 text-red-500" />
            <span>Browse Catalog</span>
          </h1>
          <p className="text-xs text-zinc-400 mt-1">Explore all viral mini-dramas and binge-worthy short reels</p>
        </div>

        {/* Sort & Search Controls */}
        <div className="flex items-center gap-3">
          <div className="relative">
            <Search className="w-4 h-4 text-zinc-400 absolute left-3 top-1/2 -translate-y-1/2" />
            <input
              type="text"
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              placeholder="Search..."
              className="pl-9 pr-3 py-1.5 bg-zinc-900 border border-white/10 rounded-full text-xs text-white placeholder-zinc-500 focus:outline-none focus:border-red-500"
            />
          </div>

          <select
            value={sortBy}
            onChange={(e) => setSortBy(e.target.value)}
            className="bg-zinc-900 text-xs text-zinc-200 border border-white/10 rounded-full px-3 py-1.5 focus:outline-none"
          >
            <option value="rating">Top Rated</option>
            <option value="episodes">Most Episodes</option>
            <option value="title">Title (A-Z)</option>
          </select>
        </div>
      </div>

      {/* Category Pills */}
      <div className="flex items-center gap-2 overflow-x-auto pb-2 scrollbar-none">
        {CATEGORIES.map((c) => (
          <button
            key={c}
            onClick={() => setSelectedCat(c)}
            className={`shrink-0 text-xs font-semibold px-4 py-1.5 rounded-full border transition-all ${
              selectedCat === c
                ? 'bg-red-600 text-white border-red-500'
                : 'bg-zinc-900 text-zinc-400 border-white/5 hover:text-white'
            }`}
          >
            {c}
          </button>
        ))}
      </div>

      {/* Grid */}
      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-4 gap-4">
        {filtered.map((drama) => (
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
    </div>
  );
}
