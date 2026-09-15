package com.example.data

import com.example.R

data class Episode(
  val id: String,
  val dramaId: String,
  val episodeNumber: Int,
  val title: String,
  val durationSeconds: Int = 106,
  val videoUrl: String,
  val thumbnailResId: Int? = null,
  val subtitleText: String = ""
)

data class Drama(
  val id: String,
  val title: String,
  val coverStorySubtitle: String = "",
  val genre: String,
  val coverResId: Int? = null,
  val coverUrl: String? = null,
  val description: String,
  val tags: List<String> = emptyList(),
  val episodesCount: Int = 56,
  val isTrending: Boolean = false,
  val isNewRelease: Boolean = false,
  val isForYou: Boolean = false,
  val isResume: Boolean = false,
  val resumeEpisode: Int = 1,
  val resumeProgress: Float = 0.25f,
  val episodes: List<Episode> = emptyList()
)

data class WatchProgress(
  val dramaId: String,
  val episodeNumber: Int,
  val positionMs: Long,
  val durationMs: Long,
  val lastWatchedTimestamp: Long = System.currentTimeMillis()
)

data class UserProfile(
  val id: String,
  val name: String,
  val email: String = "",
  val avatarColorHex: Long = 0xFFE50914,
  val isAdmin: Boolean = false,
  val savedDramaIds: Set<String> = emptySet(),
  val downloadedDramaIds: Set<String> = emptySet(),
  val watchHistory: Map<String, WatchProgress> = emptyMap()
)

data class AdConfig(
  val isAdSenseEnabled: Boolean = true,
  val adSensePublisherId: String = "ca-pub-9842183941092837",
  val bannerSlotId: String = "1928374650",
  val isPreRollEnabled: Boolean = true,
  val isMidRollEnabled: Boolean = true,
  val isBannerEnabled: Boolean = true,
  val preRollDurationSec: Int = 5,
  val preRollSkipSec: Int = 3,
  val preRollAdTitle: String = "StreamPass Premium — Ad-free Experience",
  val preRollAdDescription: String = "Get unlimited access to 10,000+ short drama episodes in 4K HDR.",
  val preRollAdMediaUrl: String = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
  val preRollClickUrl: String = "https://google.com/adsense",
  val midRollIntervalSec: Int = 45,
  val midRollAdTitle: String = "Discover Trendy Luxury Goods",
  val midRollAdMediaUrl: String = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
  val midRollClickUrl: String = "https://google.com/adsense",
  val bannerAdTitle: String = "Google AdSense • Sponsored Drama Pick",
  val bannerAdDescription: String = "Watch the top 10 werewolf & billionaire thrillers for free today.",
  val bannerAdActionUrl: String = "https://google.com/adsense"
)
