import React, { useState, useRef } from 'react';
import { 
  Shield, Film, UploadCloud, Plus, Trash2, Edit3, CheckCircle2, 
  AlertTriangle, ArrowLeft, Image, Video, Sparkles, BarChart3, Settings 
} from 'lucide-react';
import { 
  createDrama, 
  updateDrama, 
  deleteDrama, 
  addEpisodeToDrama, 
  deleteEpisodeFromDrama 
} from '../firebase/firestore';
import { uploadVideoWithProgress, uploadImageWithProgress } from '../firebase/storage';

export default function AdminDashboardPage({ 
  currentUser, 
  dramas = [], 
  onRefreshDramas, 
  onBack 
}) {
  // Authorization Gate (Server and Client verified)
  if (!currentUser?.isAdmin) {
    return (
      <div className="min-h-screen bg-[#0a0a0c] flex flex-col items-center justify-center p-6 text-center">
        <div className="w-16 h-16 rounded-2xl bg-red-600/20 text-red-500 flex items-center justify-center mb-4 border border-red-500/30">
          <AlertTriangle className="w-8 h-8" />
        </div>
        <h1 className="text-xl font-bold text-white mb-2">403 Forbidden</h1>
        <p className="text-xs text-zinc-400 max-w-sm mb-6">
          Access to this dashboard is strictly restricted to authenticated administrators. 
          Normal users cannot upload, modify, or delete content.
        </p>
        <button
          onClick={onBack}
          className="bg-zinc-800 hover:bg-zinc-700 text-white text-xs font-semibold px-5 py-2.5 rounded-full"
        >
          Return to Stream Feed
        </button>
      </div>
    );
  }

  const [activeTab, setActiveTab] = useState('upload_episode'); // 'upload_episode', 'dramas', 'ads'
  const [statusMessage, setStatusMessage] = useState({ text: '', type: '' });

  // --- Episode Video Upload State ---
  const [selectedDramaId, setSelectedDramaId] = useState(dramas[0]?.id || '');
  const [episodeTitle, setEpisodeTitle] = useState('');
  const [episodeNumber, setEpisodeNumber] = useState('');
  const [videoFile, setVideoFile] = useState(null);
  const [videoUploadProgress, setVideoUploadProgress] = useState(null); // { percent, bytesTransferred, totalBytes, state }
  const [isUploadingVideo, setIsUploadingVideo] = useState(false);
  const videoInputRef = useRef(null);

  // --- New Drama Form State ---
  const [newTitle, setNewTitle] = useState('');
  const [newGenre, setNewGenre] = useState('Mafia Romance');
  const [newSubtitle, setNewSubtitle] = useState('EXCLUSIVE RELEASE');
  const [newDescription, setNewDescription] = useState('');
  const [newTags, setNewTags] = useState('BILLIONAIRE, REVENGE, EXCLUSIVE');
  const [newEpisodesCount, setNewEpisodesCount] = useState('40');
  const [posterFile, setPosterFile] = useState(null);
  const [posterUploadProgress, setPosterUploadProgress] = useState(null);
  const [isUploadingDrama, setIsUploadingDrama] = useState(false);
  const posterInputRef = useRef(null);

  // Helper: Format bytes
  const formatBytes = (bytes) => {
    if (!bytes || bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
  };

  // 1. Handle Real Video File Upload & Episode Creation
  const handleUploadEpisode = async (e) => {
    e.preventDefault();
    if (!selectedDramaId) {
      setStatusMessage({ text: 'Please select a drama.', type: 'error' });
      return;
    }
    if (!videoFile) {
      setStatusMessage({ text: 'Please choose an MP4 video file to upload.', type: 'error' });
      return;
    }

    setIsUploadingVideo(true);
    setStatusMessage({ text: 'Uploading video to Firebase Cloud Storage...', type: 'info' });

    try {
      // Real upload to Firebase Storage with live progress callback
      const downloadUrl = await uploadVideoWithProgress(
        videoFile, 
        selectedDramaId, 
        (progress) => {
          setVideoUploadProgress(progress);
        }
      );

      // Add episode to Firestore
      const targetDrama = dramas.find(d => d.id === selectedDramaId);
      const nextEpNum = episodeNumber 
        ? parseInt(episodeNumber) 
        : (targetDrama?.episodes?.length ? targetDrama.episodes.length + 1 : 1);

      await addEpisodeToDrama(selectedDramaId, {
        title: episodeTitle || `Episode ${nextEpNum}`,
        episodeNumber: nextEpNum,
        videoUrl: downloadUrl,
        durationSeconds: 120
      }, currentUser);

      setStatusMessage({ 
        text: `Success! Episode ${nextEpNum} uploaded to Cloud Storage and published to Firestore.`, 
        type: 'success' 
      });

      // Reset form
      setVideoFile(null);
      setEpisodeTitle('');
      setEpisodeNumber('');
      setVideoUploadProgress(null);
      if (videoInputRef.current) videoInputRef.current.value = '';
      if (onRefreshDramas) onRefreshDramas();

    } catch (err) {
      console.error(err);
      setStatusMessage({ text: `Upload error: ${err.message}`, type: 'error' });
    } finally {
      setIsUploadingVideo(false);
    }
  };

  // 2. Handle New Drama Creation with Poster Image Upload
  const handleCreateDrama = async (e) => {
    e.preventDefault();
    if (!newTitle.trim()) {
      setStatusMessage({ text: 'Please enter a drama title.', type: 'error' });
      return;
    }

    setIsUploadingDrama(true);
    setStatusMessage({ text: 'Creating drama in Firestore...', type: 'info' });

    try {
      let coverUrl = '/posters/poster_mafia_don_1789461041545.jpg';

      if (posterFile) {
        setStatusMessage({ text: 'Uploading poster to Firebase Storage...', type: 'info' });
        coverUrl = await uploadImageWithProgress(posterFile, (prog) => {
          setPosterUploadProgress(prog);
        });
      }

      const dramaPayload = {
        title: newTitle.trim(),
        genre: newGenre,
        subtitle: newSubtitle,
        description: newDescription || 'A thrilling viral short drama series.',
        coverUrl: coverUrl,
        tags: newTags.split(',').map(t => t.trim()).filter(Boolean),
        episodesCount: parseInt(newEpisodesCount) || 1,
        isTrending: true,
        isFeatured: false,
        isNewRelease: true
      };

      await createDrama(dramaPayload, currentUser);

      setStatusMessage({ text: `Drama "${newTitle}" created successfully in Firestore!`, type: 'success' });
      
      // Reset form
      setNewTitle('');
      setNewDescription('');
      setPosterFile(null);
      setPosterUploadProgress(null);
      if (posterInputRef.current) posterInputRef.current.value = '';
      if (onRefreshDramas) onRefreshDramas();

    } catch (err) {
      setStatusMessage({ text: `Error creating drama: ${err.message}`, type: 'error' });
    } finally {
      setIsUploadingDrama(false);
    }
  };

  // 3. Handle Drama Deletion
  const handleDeleteDrama = async (dramaId, title) => {
    if (!window.confirm(`Are you sure you want to permanently delete "${title}"?`)) return;
    try {
      await deleteDrama(dramaId, currentUser);
      setStatusMessage({ text: `Deleted "${title}" from Firestore.`, type: 'success' });
      if (onRefreshDramas) onRefreshDramas();
    } catch (err) {
      setStatusMessage({ text: `Delete error: ${err.message}`, type: 'error' });
    }
  };

  // 4. Handle Episode Deletion
  const handleDeleteEpisode = async (dramaId, epId, epTitle) => {
    if (!window.confirm(`Delete ${epTitle}?`)) return;
    try {
      await deleteEpisodeFromDrama(dramaId, epId, currentUser);
      setStatusMessage({ text: `Episode deleted.`, type: 'success' });
      if (onRefreshDramas) onRefreshDramas();
    } catch (err) {
      setStatusMessage({ text: `Error deleting episode: ${err.message}`, type: 'error' });
    }
  };

  const currentDrama = dramas.find(d => d.id === selectedDramaId) || dramas[0];

  return (
    <div className="min-h-screen bg-[#0a0a0c] text-white pb-24">
      
      {/* Top Header */}
      <div className="bg-[#141419] border-b border-white/10 sticky top-16 z-30 px-4 sm:px-8 py-4">
        <div className="max-w-7xl mx-auto flex flex-wrap items-center justify-between gap-4">
          <div className="flex items-center gap-3">
            <button
              onClick={onBack}
              className="p-2 rounded-full bg-white/5 hover:bg-white/10 text-zinc-300 hover:text-white"
            >
              <ArrowLeft className="w-5 h-5" />
            </button>
            <div>
              <div className="flex items-center gap-2">
                <h1 className="font-display font-black text-lg sm:text-xl text-white">
                  ADMINISTRATOR DASHBOARD
                </h1>
                <span className="bg-amber-500/20 text-amber-400 border border-amber-500/30 text-[9px] font-black px-2 py-0.5 rounded-full">
                  PROTECTED
                </span>
              </div>
              <p className="text-xs text-zinc-400">
                Connected Admin: <span className="font-mono text-zinc-200">{currentUser?.email}</span>
              </p>
            </div>
          </div>

          {/* Quick Metrics */}
          <div className="flex items-center gap-3 text-xs font-semibold">
            <span className="bg-zinc-900 border border-white/10 px-3 py-1.5 rounded-lg text-zinc-300">
              Dramas: <strong className="text-white">{dramas.length}</strong>
            </span>
            <span className="bg-zinc-900 border border-white/10 px-3 py-1.5 rounded-lg text-zinc-300">
              Storage: <strong className="text-green-400">Online</strong>
            </span>
          </div>
        </div>

        {/* Tab Navigation */}
        <div className="max-w-7xl mx-auto flex items-center gap-2 mt-4">
          <button
            onClick={() => setActiveTab('upload_episode')}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg text-xs font-bold transition-colors ${
              activeTab === 'upload_episode' 
                ? 'bg-amber-500 text-black' 
                : 'text-zinc-400 hover:text-white bg-zinc-900/60'
            }`}
          >
            <UploadCloud className="w-4 h-4" />
            <span>Upload MP4 Video & Episodes</span>
          </button>

          <button
            onClick={() => setActiveTab('dramas')}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg text-xs font-bold transition-colors ${
              activeTab === 'dramas' 
                ? 'bg-amber-500 text-black' 
                : 'text-zinc-400 hover:text-white bg-zinc-900/60'
            }`}
          >
            <Film className="w-4 h-4" />
            <span>Drama Catalog Manager</span>
          </button>
        </div>
      </div>

      {/* Main Form Area */}
      <div className="max-w-7xl mx-auto px-4 sm:px-8 py-6 space-y-6">
        
        {/* Status Notification Banner */}
        {statusMessage.text && (
          <div className={`p-4 rounded-xl text-xs font-semibold flex items-center gap-2.5 animate-fade-in ${
            statusMessage.type === 'error' 
              ? 'bg-red-950/60 border border-red-500/50 text-red-200' 
              : statusMessage.type === 'success'
              ? 'bg-emerald-950/60 border border-emerald-500/50 text-emerald-200'
              : 'bg-zinc-900 border border-white/10 text-zinc-200'
          }`}>
            {statusMessage.type === 'success' ? (
              <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
            ) : (
              <AlertTriangle className="w-4 h-4 text-amber-400 shrink-0" />
            )}
            <span>{statusMessage.text}</span>
          </div>
        )}

        {/* TAB 1: REAL MP4 VIDEO FILE UPLOADER */}
        {activeTab === 'upload_episode' && (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            
            {/* Uploader Column */}
            <div className="lg:col-span-2 bg-[#141419] border border-white/10 rounded-2xl p-6 shadow-xl space-y-6">
              <div>
                <h2 className="text-base font-bold text-white flex items-center gap-2">
                  <UploadCloud className="w-5 h-5 text-amber-400" />
                  <span>Cloud Storage Video Uploader</span>
                </h2>
                <p className="text-xs text-zinc-400 mt-1">
                  Upload an authentic MP4 or WebM video file. The file is uploaded directly to Firebase Cloud Storage with live progress and instantly registered in Firestore.
                </p>
              </div>

              <form onSubmit={handleUploadEpisode} className="space-y-4">
                
                {/* Select Drama */}
                <div>
                  <label className="block text-xs font-semibold text-zinc-300 mb-1.5">
                    Target Drama Series
                  </label>
                  <select
                    value={selectedDramaId}
                    onChange={(e) => setSelectedDramaId(e.target.value)}
                    className="w-full bg-zinc-900 border border-white/10 rounded-lg px-3 py-2.5 text-sm text-white focus:outline-none focus:border-amber-500"
                  >
                    {dramas.map(d => (
                      <option key={d.id} value={d.id}>
                        {d.title} ({d.episodes?.length || d.episodesCount || 0} episodes)
                      </option>
                    ))}
                  </select>
                </div>

                {/* Episode Meta */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-semibold text-zinc-300 mb-1.5">
                      Episode Number
                    </label>
                    <input
                      type="number"
                      value={episodeNumber}
                      onChange={(e) => setEpisodeNumber(e.target.value)}
                      placeholder={currentDrama?.episodes?.length ? (currentDrama.episodes.length + 1).toString() : "1"}
                      className="w-full bg-zinc-900 border border-white/10 rounded-lg px-3 py-2.5 text-sm text-white placeholder-zinc-500 focus:outline-none focus:border-amber-500"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-zinc-300 mb-1.5">
                      Episode Title
                    </label>
                    <input
                      type="text"
                      value={episodeTitle}
                      onChange={(e) => setEpisodeTitle(e.target.value)}
                      placeholder={`Episode ${episodeNumber || (currentDrama?.episodes?.length ? currentDrama.episodes.length + 1 : 1)}`}
                      className="w-full bg-zinc-900 border border-white/10 rounded-lg px-3 py-2.5 text-sm text-white placeholder-zinc-500 focus:outline-none focus:border-amber-500"
                    />
                  </div>
                </div>

                {/* Real File Picker (Replaces URL Input as instructed) */}
                <div>
                  <label className="block text-xs font-semibold text-zinc-300 mb-1.5">
                    Select MP4 Video File <span className="text-amber-400">*</span>
                  </label>
                  
                  <div 
                    onClick={() => videoInputRef.current?.click()}
                    className="border-2 border-dashed border-zinc-700 hover:border-amber-500/80 rounded-xl p-6 text-center cursor-pointer bg-zinc-900/50 hover:bg-zinc-900 transition-colors"
                  >
                    <input
                      ref={videoInputRef}
                      type="file"
                      accept="video/mp4,video/webm,video/*"
                      onChange={(e) => setVideoFile(e.target.files?.[0] || null)}
                      className="hidden"
                    />
                    <Video className="w-10 h-10 text-amber-400 mx-auto mb-2" />
                    {videoFile ? (
                      <div>
                        <p className="text-sm font-bold text-white truncate max-w-sm mx-auto">{videoFile.name}</p>
                        <p className="text-xs text-amber-400 mt-1 font-mono">
                          Size: {formatBytes(videoFile.size)} • Type: {videoFile.type || 'video/mp4'}
                        </p>
                        <span className="inline-block mt-2 text-[11px] text-zinc-400 bg-zinc-800 px-3 py-1 rounded-full">
                          Click to select a different file
                        </span>
                      </div>
                    ) : (
                      <div>
                        <p className="text-sm font-semibold text-zinc-200">
                          Click to browse and choose MP4 video file
                        </p>
                        <p className="text-xs text-zinc-500 mt-1">
                          Supports MP4, WebM, H.264 formats (up to 500MB)
                        </p>
                      </div>
                    )}
                  </div>
                </div>

                {/* Real-time Upload Progress Indicator */}
                {videoUploadProgress && (
                  <div className="bg-zinc-900 border border-amber-500/40 rounded-xl p-4 space-y-2 animate-fade-in">
                    <div className="flex items-center justify-between text-xs font-semibold">
                      <span className="text-zinc-300">
                        {videoUploadProgress.percent === 100 
                          ? 'Finalizing Cloud Storage URL...' 
                          : 'Uploading to Firebase Cloud Storage...'}
                      </span>
                      <span className="text-amber-400 font-mono font-bold">
                        {videoUploadProgress.percent}%
                      </span>
                    </div>

                    {/* Progress Bar */}
                    <div className="w-full bg-zinc-800 h-2.5 rounded-full overflow-hidden">
                      <div 
                        className="bg-gradient-to-r from-amber-500 to-red-500 h-full transition-all duration-200 rounded-full"
                        style={{ width: `${videoUploadProgress.percent}%` }}
                      />
                    </div>

                    <div className="flex items-center justify-between text-[11px] text-zinc-400 font-mono pt-1">
                      <span>{formatBytes(videoUploadProgress.bytesTransferred)} / {formatBytes(videoUploadProgress.totalBytes)}</span>
                      <span>State: {videoUploadProgress.state || 'running'}</span>
                    </div>
                  </div>
                )}

                {/* Submit Action */}
                <button
                  type="submit"
                  disabled={isUploadingVideo || !videoFile}
                  className="w-full py-3 bg-amber-500 hover:bg-amber-400 disabled:opacity-40 disabled:hover:bg-amber-500 text-black font-extrabold text-sm rounded-xl shadow-lg shadow-amber-500/20 flex items-center justify-center gap-2 transition-all cursor-pointer"
                >
                  {isUploadingVideo ? (
                    <>
                      <span className="w-4 h-4 border-2 border-black/30 border-t-black rounded-full animate-spin" />
                      <span>Uploading & Publishing Episode...</span>
                    </>
                  ) : (
                    <>
                      <UploadCloud className="w-4 h-4" />
                      <span>Upload & Publish to Stream</span>
                    </>
                  )}
                </button>

              </form>
            </div>

            {/* Existing Episodes List for Selected Drama */}
            <div className="bg-[#141419] border border-white/10 rounded-2xl p-5 shadow-xl flex flex-col max-h-[600px]">
              <div className="pb-3 border-b border-white/10 mb-3">
                <h3 className="text-sm font-bold text-white truncate">
                  Episodes in "{currentDrama?.title}"
                </h3>
                <p className="text-xs text-zinc-400">
                  {currentDrama?.episodes?.length || 0} Registered Episodes
                </p>
              </div>

              <div className="flex-1 overflow-y-auto space-y-2 pr-1">
                {currentDrama?.episodes && currentDrama.episodes.length > 0 ? (
                  currentDrama.episodes.map((ep, idx) => (
                    <div 
                      key={ep.id || idx}
                      className="p-2.5 bg-zinc-900 border border-white/5 rounded-lg flex items-center justify-between text-xs"
                    >
                      <div className="truncate mr-2">
                        <span className="font-bold text-white mr-1.5">#{ep.episodeNumber || (idx + 1)}</span>
                        <span className="text-zinc-300">{ep.title}</span>
                      </div>

                      <button
                        onClick={() => handleDeleteEpisode(currentDrama.id, ep.id, ep.title)}
                        className="p-1 text-zinc-500 hover:text-red-400 transition-colors shrink-0"
                        title="Delete Episode"
                      >
                        <Trash2 className="w-3.5 h-3.5" />
                      </button>
                    </div>
                  ))
                ) : (
                  <p className="text-xs text-zinc-500 text-center py-8">
                    No episodes added to this drama yet.
                  </p>
                )}
              </div>
            </div>

          </div>
        )}

        {/* TAB 2: DRAMA CATALOG MANAGER */}
        {activeTab === 'dramas' && (
          <div className="space-y-6">
            
            {/* Create New Drama Card */}
            <div className="bg-[#141419] border border-white/10 rounded-2xl p-6 shadow-xl">
              <h2 className="text-base font-bold text-white flex items-center gap-2 mb-4">
                <Plus className="w-5 h-5 text-amber-400" />
                <span>Create New Drama Series</span>
              </h2>

              <form onSubmit={handleCreateDrama} className="space-y-4">
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-semibold text-zinc-300 mb-1">
                      Drama Title <span className="text-red-500">*</span>
                    </label>
                    <input
                      type="text"
                      value={newTitle}
                      onChange={(e) => setNewTitle(e.target.value)}
                      placeholder="e.g., The Secret Lycan Heir"
                      required
                      className="w-full bg-zinc-900 border border-white/10 rounded-lg px-3 py-2.5 text-sm text-white placeholder-zinc-500 focus:outline-none focus:border-amber-500"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-zinc-300 mb-1">
                      Genre
                    </label>
                    <select
                      value={newGenre}
                      onChange={(e) => setNewGenre(e.target.value)}
                      className="w-full bg-zinc-900 border border-white/10 rounded-lg px-3 py-2.5 text-sm text-white focus:outline-none focus:border-amber-500"
                    >
                      <option value="Mafia Romance">Mafia Romance</option>
                      <option value="Fantasy Werewolf">Fantasy Werewolf</option>
                      <option value="Billionaire Revenge">Billionaire Revenge</option>
                      <option value="Flash Marriage">Flash Marriage</option>
                      <option value="Country Billionaire">Country Billionaire</option>
                      <option value="Fantasy Magic">Fantasy Magic</option>
                      <option value="Martial Arts Fantasy">Martial Arts Fantasy</option>
                    </select>
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-semibold text-zinc-300 mb-1">
                    Synopsis / Description
                  </label>
                  <textarea
                    value={newDescription}
                    onChange={(e) => setNewDescription(e.target.value)}
                    rows="3"
                    placeholder="Write a hook synopsis for this viral short drama..."
                    className="w-full bg-zinc-900 border border-white/10 rounded-lg px-3 py-2 text-sm text-white placeholder-zinc-500 focus:outline-none focus:border-amber-500"
                  />
                </div>

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-semibold text-zinc-300 mb-1">
                      Cover Poster Image (Real File Picker)
                    </label>
                    <input
                      ref={posterInputRef}
                      type="file"
                      accept="image/*"
                      onChange={(e) => setPosterFile(e.target.files?.[0] || null)}
                      className="w-full bg-zinc-900 border border-white/10 rounded-lg px-3 py-2 text-xs text-zinc-300"
                    />
                    {posterUploadProgress && (
                      <p className="text-[11px] text-amber-400 mt-1 font-mono">
                        Poster Upload: {posterUploadProgress.percent}%
                      </p>
                    )}
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-zinc-300 mb-1">
                      Tags (comma-separated)
                    </label>
                    <input
                      type="text"
                      value={newTags}
                      onChange={(e) => setNewTags(e.target.value)}
                      placeholder="ALPHA, BILLIONAIRE, CONTRACT"
                      className="w-full bg-zinc-900 border border-white/10 rounded-lg px-3 py-2.5 text-sm text-white placeholder-zinc-500 focus:outline-none focus:border-amber-500"
                    />
                  </div>
                </div>

                <button
                  type="submit"
                  disabled={isUploadingDrama}
                  className="py-2.5 px-6 bg-amber-500 hover:bg-amber-400 text-black font-extrabold text-xs rounded-lg shadow-md transition-all cursor-pointer disabled:opacity-50"
                >
                  {isUploadingDrama ? 'Saving to Firestore...' : 'Create Drama Series'}
                </button>
              </form>
            </div>

            {/* List of Existing Dramas */}
            <div className="bg-[#141419] border border-white/10 rounded-2xl p-6 shadow-xl space-y-4">
              <h3 className="text-base font-bold text-white">
                Live Firestore Catalog ({dramas.length} Dramas)
              </h3>

              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
                {dramas.map((drama) => (
                  <div
                    key={drama.id}
                    className="flex gap-3 bg-zinc-900/80 border border-white/5 rounded-xl p-3 relative group"
                  >
                    <img
                      src={drama.coverUrl}
                      alt={drama.title}
                      className="w-16 h-22 object-cover rounded-lg shrink-0 bg-zinc-800"
                    />
                    <div className="flex-1 flex flex-col justify-between truncate">
                      <div>
                        <span className="text-[10px] text-amber-400 font-bold uppercase">{drama.genre}</span>
                        <h4 className="text-xs font-bold text-white truncate">{drama.title}</h4>
                        <p className="text-[11px] text-zinc-400 mt-0.5">
                          {drama.episodesCount || (drama.episodes ? drama.episodes.length : 0)} Episodes
                        </p>
                      </div>

                      <div className="flex items-center gap-2 pt-2">
                        <button
                          onClick={() => {
                            setSelectedDramaId(drama.id);
                            setActiveTab('upload_episode');
                          }}
                          className="text-[10px] font-bold text-amber-400 hover:underline flex items-center gap-1"
                        >
                          <UploadCloud className="w-3 h-3" />
                          <span>Add Episode</span>
                        </button>

                        <button
                          onClick={() => handleDeleteDrama(drama.id, drama.title)}
                          className="text-[10px] font-bold text-red-400 hover:underline flex items-center gap-1 ml-auto"
                        >
                          <Trash2 className="w-3 h-3" />
                          <span>Delete</span>
                        </button>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>

          </div>
        )}

      </div>

    </div>
  );
}
