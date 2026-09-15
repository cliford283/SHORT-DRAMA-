import React, { useState, useRef, useEffect } from 'react';
import { 
  Play, Pause, X, RotateCcw, RotateCw, Volume2, VolumeX, 
  Maximize, Minimize, SkipForward, SkipBack, List, Settings, Check 
} from 'lucide-react';
import { saveWatchProgress } from '../firebase/firestore';

export default function VideoPlayerModal({ 
  drama, 
  initialEpisode, 
  currentUser, 
  onClose 
}) {
  const videoRef = useRef(null);
  const containerRef = useRef(null);
  
  const episodes = drama.episodes || [];
  const [currentEpisodeIndex, setCurrentEpisodeIndex] = useState(() => {
    if (!initialEpisode) return 0;
    const idx = episodes.findIndex(e => e.id === initialEpisode.id || e.episodeNumber === initialEpisode.episodeNumber);
    return idx >= 0 ? idx : 0;
  });

  const currentEpisode = episodes[currentEpisodeIndex] || {
    title: "Episode 1",
    episodeNumber: 1,
    videoUrl: drama.episodes?.[0]?.videoUrl || "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
  };

  const [isPlaying, setIsPlaying] = useState(true);
  const [currentTime, setCurrentTime] = useState(0);
  const [duration, setDuration] = useState(0);
  const [volume, setVolume] = useState(1);
  const [isMuted, setIsMuted] = useState(false);
  const [isFullscreen, setIsFullscreen] = useState(false);
  const [playbackRate, setPlaybackRate] = useState(1);
  const [showControls, setShowControls] = useState(true);
  const [showEpisodeDrawer, setShowEpisodeDrawer] = useState(false);
  const [showSpeedMenu, setShowSpeedMenu] = useState(false);

  const controlsTimeoutRef = useRef(null);

  // Auto-hide controls during playback
  const handleMouseMove = () => {
    setShowControls(true);
    if (controlsTimeoutRef.current) clearTimeout(controlsTimeoutRef.current);
    controlsTimeoutRef.current = setTimeout(() => {
      if (isPlaying) setShowControls(false);
    }, 3500);
  };

  const togglePlay = () => {
    if (!videoRef.current) return;
    if (isPlaying) {
      videoRef.current.pause();
    } else {
      videoRef.current.play();
    }
  };

  const toggleMute = () => {
    if (!videoRef.current) return;
    const nextMuted = !isMuted;
    setIsMuted(nextMuted);
    videoRef.current.muted = nextMuted;
  };

  const handleVolumeChange = (e) => {
    const val = parseFloat(e.target.value);
    setVolume(val);
    if (videoRef.current) {
      videoRef.current.volume = val;
      videoRef.current.muted = val === 0;
      setIsMuted(val === 0);
    }
  };

  const toggleFullscreen = () => {
    if (!containerRef.current) return;
    if (!document.fullscreenElement) {
      containerRef.current.requestFullscreen().catch(err => console.error(err));
      setIsFullscreen(true);
    } else {
      document.exitFullscreen();
      setIsFullscreen(false);
    }
  };

  const seek = (seconds) => {
    if (!videoRef.current) return;
    videoRef.current.currentTime = Math.max(0, Math.min(seconds, duration));
  };

  const handleSpeedChange = (speed) => {
    setPlaybackRate(speed);
    if (videoRef.current) videoRef.current.playbackRate = speed;
    setShowSpeedMenu(false);
  };

  const playNextEpisode = () => {
    if (currentEpisodeIndex < episodes.length - 1) {
      setCurrentEpisodeIndex(prev => prev + 1);
    }
  };

  const playPrevEpisode = () => {
    if (currentEpisodeIndex > 0) {
      setCurrentEpisodeIndex(prev => prev - 1);
    }
  };

  // Video ended -> auto play next episode
  const handleVideoEnded = () => {
    if (currentUser?.uid) {
      saveWatchProgress(currentUser.uid, drama.id, currentEpisode.id, duration, true);
    }
    if (currentEpisodeIndex < episodes.length - 1) {
      setCurrentEpisodeIndex(prev => prev + 1);
    }
  };

  // Time update -> record progress
  const handleTimeUpdate = () => {
    if (!videoRef.current) return;
    setCurrentTime(videoRef.current.currentTime);
    // Periodically save progress to Firestore
    if (currentUser?.uid && Math.round(videoRef.current.currentTime) % 10 === 0) {
      saveWatchProgress(
        currentUser.uid, 
        drama.id, 
        currentEpisode.id, 
        videoRef.current.currentTime, 
        false
      );
    }
  };

  const formatTime = (secs) => {
    if (isNaN(secs)) return "0:00";
    const m = Math.floor(secs / 60);
    const s = Math.floor(secs % 60);
    return `${m}:${s < 10 ? '0' : ''}${s}`;
  };

  return (
    <div 
      ref={containerRef}
      onMouseMove={handleMouseMove}
      className="fixed inset-0 z-50 bg-black flex flex-col items-center justify-center select-none"
    >
      {/* Top Header Bar */}
      <div className={`absolute top-0 left-0 right-0 z-30 p-4 bg-gradient-to-b from-black/80 to-transparent flex items-center justify-between transition-opacity duration-300 ${
        showControls ? 'opacity-100' : 'opacity-0 pointer-events-none'
      }`}>
        <div className="flex items-center gap-3">
          <button 
            onClick={onClose}
            className="p-2 rounded-full bg-white/10 hover:bg-white/20 text-white transition-colors"
          >
            <X className="w-5 h-5" />
          </button>
          <div>
            <h2 className="text-sm font-bold text-white tracking-wide truncate max-w-[240px] sm:max-w-md">
              {drama.title}
            </h2>
            <p className="text-xs text-red-400 font-semibold">
              {currentEpisode.title} (Ep {currentEpisode.episodeNumber || (currentEpisodeIndex + 1)})
            </p>
          </div>
        </div>

        <div className="flex items-center gap-2">
          <button
            onClick={() => setShowEpisodeDrawer(!showEpisodeDrawer)}
            className="flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-white/10 hover:bg-white/20 text-xs font-semibold text-white transition-colors"
          >
            <List className="w-4 h-4" />
            <span className="hidden sm:inline">Episodes</span>
          </button>
        </div>
      </div>

      {/* Main HTML5 Video Player */}
      <div className="relative w-full h-full flex items-center justify-center bg-black">
        <video
          ref={videoRef}
          src={currentEpisode.videoUrl}
          autoPlay
          playsInline
          onPlay={() => setIsPlaying(true)}
          onPause={() => setIsPlaying(false)}
          onTimeUpdate={handleTimeUpdate}
          onLoadedMetadata={() => {
            if (videoRef.current) {
              setDuration(videoRef.current.duration);
              videoRef.current.playbackRate = playbackRate;
            }
          }}
          onEnded={handleVideoEnded}
          onClick={togglePlay}
          className="max-h-full max-w-full object-contain cursor-pointer"
        />

        {/* Center Click Play/Pause Icon Pulse */}
        {!isPlaying && (
          <div 
            onClick={togglePlay}
            className="absolute inset-0 flex items-center justify-center bg-black/30 cursor-pointer"
          >
            <div className="w-16 h-16 rounded-full bg-red-600/90 text-white flex items-center justify-center shadow-2xl shadow-red-600/50 hover:scale-110 transition-transform">
              <Play className="w-8 h-8 fill-white translate-x-1" />
            </div>
          </div>
        )}
      </div>

      {/* Bottom Controls Overlay */}
      <div className={`absolute bottom-0 left-0 right-0 z-30 px-4 py-3 sm:px-6 bg-gradient-to-t from-black/90 via-black/60 to-transparent transition-opacity duration-300 ${
        showControls ? 'opacity-100' : 'opacity-0 pointer-events-none'
      }`}>
        
        {/* Scrubber / Progress Bar */}
        <div className="flex items-center gap-2 mb-2">
          <input
            type="range"
            min="0"
            max={duration || 100}
            step="0.1"
            value={currentTime}
            onChange={(e) => seek(parseFloat(e.target.value))}
            className="w-full h-1.5 bg-zinc-700 accent-red-600 rounded-lg cursor-pointer transition-all hover:h-2"
          />
        </div>

        <div className="flex items-center justify-between">
          
          {/* Left Controls: Play, Skips, Timers */}
          <div className="flex items-center gap-3">
            <button 
              onClick={togglePlay}
              className="text-white hover:text-red-400 transition-colors"
            >
              {isPlaying ? <Pause className="w-5 h-5 fill-white" /> : <Play className="w-5 h-5 fill-white" />}
            </button>

            <button 
              onClick={() => seek(currentTime - 10)}
              className="text-zinc-300 hover:text-white transition-colors"
              title="Rewind 10s"
            >
              <RotateCcw className="w-4 h-4" />
            </button>

            <button 
              onClick={() => seek(currentTime + 10)}
              className="text-zinc-300 hover:text-white transition-colors"
              title="Fast Forward 10s"
            >
              <RotateCw className="w-4 h-4" />
            </button>

            <button 
              onClick={playPrevEpisode}
              disabled={currentEpisodeIndex === 0}
              className="text-zinc-400 hover:text-white disabled:opacity-30 disabled:hover:text-zinc-400 transition-colors"
              title="Previous Episode"
            >
              <SkipBack className="w-4 h-4" />
            </button>

            <button 
              onClick={playNextEpisode}
              disabled={currentEpisodeIndex >= episodes.length - 1}
              className="text-zinc-400 hover:text-white disabled:opacity-30 disabled:hover:text-zinc-400 transition-colors"
              title="Next Episode"
            >
              <SkipForward className="w-4 h-4" />
            </button>

            <span className="text-xs text-zinc-300 font-mono">
              {formatTime(currentTime)} / {formatTime(duration)}
            </span>
          </div>

          {/* Right Controls: Volume, Speed, Fullscreen */}
          <div className="flex items-center gap-3">
            
            {/* Volume Control */}
            <div className="hidden sm:flex items-center gap-1.5 group">
              <button onClick={toggleMute} className="text-zinc-300 hover:text-white">
                {isMuted || volume === 0 ? <VolumeX className="w-4 h-4" /> : <Volume2 className="w-4 h-4" />}
              </button>
              <input
                type="range"
                min="0"
                max="1"
                step="0.05"
                value={isMuted ? 0 : volume}
                onChange={handleVolumeChange}
                className="w-16 h-1 bg-zinc-700 accent-red-600 rounded cursor-pointer"
              />
            </div>

            {/* Playback Speed Menu */}
            <div className="relative">
              <button
                onClick={() => setShowSpeedMenu(!showSpeedMenu)}
                className="text-xs font-bold text-zinc-300 hover:text-white bg-white/10 px-2 py-1 rounded"
              >
                {playbackRate}x
              </button>
              {showSpeedMenu && (
                <div className="absolute bottom-8 right-0 bg-zinc-900 border border-zinc-700 rounded-lg py-1 w-20 shadow-xl z-50">
                  {[0.75, 1, 1.25, 1.5, 2].map((speed) => (
                    <button
                      key={speed}
                      onClick={() => handleSpeedChange(speed)}
                      className="w-full px-3 py-1 text-xs text-left flex items-center justify-between text-zinc-200 hover:bg-zinc-800"
                    >
                      <span>{speed}x</span>
                      {playbackRate === speed && <Check className="w-3 h-3 text-red-500" />}
                    </button>
                  ))}
                </div>
              )}
            </div>

            {/* Fullscreen Button */}
            <button
              onClick={toggleFullscreen}
              className="text-zinc-300 hover:text-white transition-colors"
            >
              {isFullscreen ? <Minimize className="w-4 h-4" /> : <Maximize className="w-4 h-4" />}
            </button>
          </div>

        </div>
      </div>

      {/* Side Episode Selection Drawer */}
      {showEpisodeDrawer && (
        <div className="absolute top-0 right-0 bottom-0 w-80 max-w-[85%] bg-zinc-950/95 backdrop-blur-xl border-l border-zinc-800 z-40 flex flex-col p-4 animate-fade-in shadow-2xl">
          <div className="flex items-center justify-between pb-3 border-b border-zinc-800 mb-3">
            <div>
              <h3 className="text-sm font-bold text-white">Episodes</h3>
              <p className="text-xs text-zinc-400">{episodes.length} Episodes Total</p>
            </div>
            <button 
              onClick={() => setShowEpisodeDrawer(false)}
              className="p-1 rounded text-zinc-400 hover:text-white"
            >
              <X className="w-5 h-5" />
            </button>
          </div>

          <div className="flex-1 overflow-y-auto space-y-2 pr-1">
            {episodes.map((ep, idx) => {
              const isCurrent = idx === currentEpisodeIndex;
              return (
                <div
                  key={ep.id || idx}
                  onClick={() => {
                    setCurrentEpisodeIndex(idx);
                    setShowEpisodeDrawer(false);
                  }}
                  className={`flex items-center justify-between p-2.5 rounded-lg text-xs font-semibold cursor-pointer transition-colors ${
                    isCurrent 
                      ? 'bg-red-600/20 text-red-400 border border-red-500/40' 
                      : 'bg-zinc-900/60 text-zinc-300 hover:bg-zinc-800'
                  }`}
                >
                  <div className="flex items-center gap-2">
                    <span className={`w-5 h-5 rounded-full flex items-center justify-center text-[10px] ${
                      isCurrent ? 'bg-red-600 text-white' : 'bg-zinc-800 text-zinc-400'
                    }`}>
                      {ep.episodeNumber || (idx + 1)}
                    </span>
                    <span className="truncate max-w-[170px]">{ep.title}</span>
                  </div>
                  <span className="text-[10px] text-zinc-500 font-mono">
                    {formatTime(ep.durationSeconds || 95)}
                  </span>
                </div>
              );
            })}
          </div>
        </div>
      )}

    </div>
  );
}
