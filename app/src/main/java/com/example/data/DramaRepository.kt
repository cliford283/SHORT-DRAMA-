package com.example.data

import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

object DramaRepository {

  private val defaultProfiles = listOf(
    UserProfile(
      id = "user_guest",
      name = "Guest Viewer",
      email = "viewer@shortdrama.tv",
      avatarColorHex = 0xFFE50914,
      isAdmin = false,
      savedDramaIds = setOf("drama_mafia_don", "drama_lycan_queen"),
      downloadedDramaIds = setOf("drama_weakest_bastard")
    ),
    UserProfile(
      id = "user_sarah",
      name = "Sarah Jenkins",
      email = "sarah.j@gmail.com",
      avatarColorHex = 0xFF7C4DFF,
      isAdmin = false,
      savedDramaIds = setOf("drama_poisoned_love", "drama_bugatti"),
      downloadedDramaIds = setOf("drama_mafia_don")
    ),
    UserProfile(
      id = "user_admin",
      name = "Admin Master",
      email = "admin@shortdrama.tv",
      avatarColorHex = 0xFF00E676,
      isAdmin = true,
      savedDramaIds = setOf("drama_mafia_don", "drama_poisoned_love", "drama_archmage"),
      downloadedDramaIds = setOf("drama_weakest_bastard", "drama_bugatti")
    )
  )

  private val sampleVideoUrls = listOf(
    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyBlazes.mp4",
    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4",
    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4",
    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/Sintel.mp4",
    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
  )

  private fun generateEpisodes(dramaId: String, count: Int, baseTitle: String): List<Episode> {
    return (1..count).map { epNum ->
      val videoUrl = sampleVideoUrls[(epNum - 1) % sampleVideoUrls.size]
      Episode(
        id = "${dramaId}_ep_$epNum",
        dramaId = dramaId,
        episodeNumber = epNum,
        title = "Episode $epNum",
        durationSeconds = 90 + (epNum * 7) % 50,
        videoUrl = videoUrl,
        subtitleText = when (epNum % 4) {
          0 -> "Three million dollars. Leave tonight and never look back."
          1 -> "Limited edition Bugatti. You think money buys everything?"
          2 -> "I made a promise to protect the family, no matter what."
          else -> "You have no idea who you're dealing with."
        }
      )
    }
  }

  private val initialDramas = listOf(
    Drama(
      id = "drama_mafia_don",
      title = "The Mafia Don Hired Me as His Wife",
      coverStorySubtitle = "COVER STORY",
      genre = "Mafia Romance",
      coverResId = R.drawable.poster_mafia_don_1789461041545,
      description = "Contracted into the lion's den! Down-on-her-luck baker Clara signs a 1-year marriage deed to protect her sick brother, only to discover her billionaire client is the deadliest syndicate boss in New York. Underneath his cold exterior lies an obsessive heart that will burn down empires to keep her safe.",
      tags = listOf("CONTRACT MARRIAGE", "MAFIA BOSS", "BILLIONAIRE", "OBSESSIVE LOVE"),
      episodesCount = 139,
      isTrending = true,
      isResume = true,
      resumeEpisode = 1,
      resumeProgress = 0.45f,
      episodes = generateEpisodes("drama_mafia_don", 139, "The Mafia Don Hired Me as His Wife")
    ),
    Drama(
      id = "drama_bugatti",
      title = "Get Out of My Bugatti",
      coverStorySubtitle = "BILINGUAL EDITION",
      genre = "Billionaire Revenge",
      coverResId = R.drawable.poster_bugatti_1789461062516,
      description = "Disguised as a broke valet driver, hidden heir Logan Vance tests his arrogant fiancee's loyalty on her birthday. When she dumps him on the roadside and climbs into another man's car, Logan calls his butler and deploys 20 gold Bugattis to intercept them.",
      tags = listOf("HIDDEN IDENTITY", "SWEET REVENGE", "BILLIONAIRE HEIR", "SLAP IN THE FACE"),
      episodesCount = 40,
      isTrending = true,
      isForYou = true,
      isResume = true,
      resumeEpisode = 1,
      resumeProgress = 0.70f,
      episodes = generateEpisodes("drama_bugatti", 40, "Get Out of My Bugatti")
    ),
    Drama(
      id = "drama_poisoned_love",
      title = "Poisoned by His Love",
      coverStorySubtitle = "EXCLUSIVE DRAMA",
      genre = "Flash Marriage",
      coverResId = R.drawable.poster_poisoned_love_1789461081803,
      description = "On her half-sister Victoria's wedding day, 25-year-old nurse Lily Watson is forced to marry Dominic Castellano, a ruthless mafia king rumored to have murdered his last wife. When Victoria flees, Dominic's hitmen threaten Lily's family. To save them, Lily takes Victoria's place at the altar—only to become the bride of a man she fears may be a killer.",
      tags = listOf("HIDDEN IDENTITY", "FLASH MARRIAGE", "LOVE AFTER MARRIAGE", "REVENGE"),
      episodesCount = 56,
      isTrending = true,
      isNewRelease = true,
      isResume = true,
      resumeEpisode = 3,
      resumeProgress = 0.35f,
      episodes = generateEpisodes("drama_poisoned_love", 56, "Poisoned by His Love")
    ),
    Drama(
      id = "drama_alpha_queen",
      title = "Oops, You Bullied The Alpha Queen",
      coverStorySubtitle = "HOT WEREWOLF",
      genre = "Fantasy Werewolf",
      coverResId = R.drawable.poster_alpha_queen_1789461100658,
      description = "Transferred to St. Jude's Academy as a seemingly quiet scholarship student, Elena endured months of torment from the elite cheer captain. But the blood moon triggers her awakening as the supreme Royal Blood Lycan Queen whose roar forces packs to kneel.",
      tags = listOf("ALPHA QUEEN", "WEREWOLF", "CAMPUS REVENGE", "SHIFTER ROMANCE"),
      episodesCount = 45,
      isTrending = true,
      isNewRelease = false,
      isForYou = true,
      isResume = true,
      resumeEpisode = 1,
      resumeProgress = 0.15f,
      episodes = generateEpisodes("drama_alpha_queen", 45, "Oops, You Bullied The Alpha Queen")
    ),
    Drama(
      id = "drama_cowgirl",
      title = "This Cowgirl Defended My Autistic Stepson",
      coverStorySubtitle = "HEARTWARMING ROMANCE",
      genre = "Country Billionaire",
      coverResId = R.drawable.poster_cowgirl_1789461149055,
      description = "Hired as a ranch hand, sharpshooter cowgirl Sadie shields tech tycoon Liam's non-verbal son from greedy relatives. When the young boy speaks his very first sentence to Sadie, the cold billionaire realizes this fiery country girl is the missing piece of his shattered home.",
      tags = listOf("RANCH ROMANCE", "PROTECTOR", "BILLIONAIRE DAD", "FAMILY BOND"),
      episodesCount = 32,
      isTrending = true,
      isNewRelease = false,
      isResume = true,
      resumeEpisode = 1,
      resumeProgress = 0.55f,
      episodes = generateEpisodes("drama_cowgirl", 32, "This Cowgirl Defended My Autistic Stepson")
    ),
    Drama(
      id = "drama_archmage",
      title = "Bow To 10-Year-Old Archmage Aldric",
      coverStorySubtitle = "MAGIC REBIRTH",
      genre = "Fantasy Magic",
      coverResId = R.drawable.poster_archmage_1789461165800,
      description = "Betrayed by the Imperial Council after saving humanity from the void dragon, Archmage Aldric awakes reincarnated inside the body of a 10-year-old exiled noble orphan. With 500 years of forbidden spell knowledge intact, he dismantles aristocratic bullies before lunch.",
      tags = listOf("REINCARNATION", "MAGIC PRODIGY", "OVERPOWERED HERO", "FANTASY EMPIRE"),
      episodesCount = 130,
      isTrending = true,
      isNewRelease = true,
      isForYou = true,
      episodes = generateEpisodes("drama_archmage", 130, "Bow To 10-Year-Old Archmage Aldric")
    ),
    Drama(
      id = "drama_lycan_queen",
      title = "The Rise of the Lycan Queen",
      coverStorySubtitle = "SUPREME MONARCH",
      genre = "Fantasy Werewolf",
      coverResId = R.drawable.poster_lycan_queen_1789461119081,
      description = "Banished into the cursed silver woods by her wicked pack elders, scarred hybrid Astrid unlocks the legendary primal fire of the First Moon. As three rival alpha kings march to conquer her territory, she crowns herself Empress.",
      tags = listOf("LYCAN QUEEN", "TRUE ALPHA", "ENEMIES TO LOVERS", "FANTASY WAR"),
      episodesCount = 51,
      isTrending = false,
      isNewRelease = true,
      isForYou = true,
      episodes = generateEpisodes("drama_lycan_queen", 51, "The Rise of the Lycan Queen")
    ),
    Drama(
      id = "drama_weakest_bastard",
      title = "The Weakest Bastard Shakes the World",
      coverStorySubtitle = "ACTION EPICS",
      genre = "Martial Arts Fantasy",
      coverResId = R.drawable.poster_weakest_bastard_1789461185110,
      description = "Deemed born with defective mana veins and discarded into the salt mines, illegitimate son Arthur stumbles upon the sealed tomb of the Immortal God of War. Armed with forbidden blade arts, his return shocks the entire continent.",
      tags = listOf("MARTIAL ARTS", "ZERO TO HERO", "DARK FANTASY", "IMMORTAL SWORD"),
      episodesCount = 68,
      isTrending = false,
      isNewRelease = false,
      isForYou = true,
      episodes = generateEpisodes("drama_weakest_bastard", 68, "The Weakest Bastard Shakes the World")
    )
  )

  private val _dramas = MutableStateFlow<List<Drama>>(initialDramas)
  val dramas: StateFlow<List<Drama>> = _dramas.asStateFlow()

  private val _profiles = MutableStateFlow<List<UserProfile>>(defaultProfiles)
  val profiles: StateFlow<List<UserProfile>> = _profiles.asStateFlow()

  private val _currentProfile = MutableStateFlow<UserProfile>(defaultProfiles[0])
  val currentProfile: StateFlow<UserProfile> = _currentProfile.asStateFlow()

  private val _adConfig = MutableStateFlow<AdConfig>(AdConfig())
  val adConfig: StateFlow<AdConfig> = _adConfig.asStateFlow()

  fun switchProfile(profileId: String) {
    val found = _profiles.value.find { it.id == profileId } ?: return
    _currentProfile.value = found
  }

  fun createProfile(name: String, email: String, isAdmin: Boolean = false) {
    val newProfile = UserProfile(
      id = "user_" + UUID.randomUUID().toString().take(8),
      name = name.ifBlank { "Viewer" },
      email = email,
      avatarColorHex = listOf(0xFFE50914, 0xFF00E676, 0xFF2979FF, 0xFFFF9100, 0xFF7C4DFF).random(),
      isAdmin = isAdmin
    )
    _profiles.update { it + newProfile }
    _currentProfile.value = newProfile
  }

  fun toggleFavorite(dramaId: String) {
    val current = _currentProfile.value
    val newSaved = if (current.savedDramaIds.contains(dramaId)) {
      current.savedDramaIds - dramaId
    } else {
      current.savedDramaIds + dramaId
    }
    val updatedProfile = current.copy(savedDramaIds = newSaved)
    _currentProfile.value = updatedProfile
    _profiles.update { list -> list.map { if (it.id == updatedProfile.id) updatedProfile else it } }
  }

  fun toggleDownload(dramaId: String) {
    val current = _currentProfile.value
    val newDownloads = if (current.downloadedDramaIds.contains(dramaId)) {
      current.downloadedDramaIds - dramaId
    } else {
      current.downloadedDramaIds + dramaId
    }
    val updatedProfile = current.copy(downloadedDramaIds = newDownloads)
    _currentProfile.value = updatedProfile
    _profiles.update { list -> list.map { if (it.id == updatedProfile.id) updatedProfile else it } }
  }

  fun recordWatchProgress(dramaId: String, episodeNumber: Int, positionMs: Long, durationMs: Long) {
    val current = _currentProfile.value
    val progress = WatchProgress(dramaId, episodeNumber, positionMs, durationMs)
    val newHistory = current.watchHistory + (dramaId to progress)
    val updatedProfile = current.copy(watchHistory = newHistory)
    _currentProfile.value = updatedProfile
    _profiles.update { list -> list.map { if (it.id == updatedProfile.id) updatedProfile else it } }
  }

  // Admin Controls
  fun addDrama(
    title: String,
    genre: String,
    description: String,
    tags: List<String>,
    episodesCount: Int,
    coverUrl: String? = null,
    streamUrl: String? = null
  ) {
    val dramaId = "drama_" + UUID.randomUUID().toString().take(8)
    val initialEpisodes = (1..episodesCount).map { epNum ->
      Episode(
        id = "${dramaId}_ep_$epNum",
        dramaId = dramaId,
        episodeNumber = epNum,
        title = "Episode $epNum",
        durationSeconds = 120,
        videoUrl = streamUrl?.ifBlank { null }
          ?: sampleVideoUrls[(epNum - 1) % sampleVideoUrls.size],
        subtitleText = "Streaming episode $epNum of $title."
      )
    }

    val newDrama = Drama(
      id = dramaId,
      title = title,
      genre = genre,
      description = description,
      tags = tags,
      episodesCount = episodesCount,
      coverUrl = coverUrl,
      coverResId = R.drawable.poster_mafia_don_1789461041545, // fallback attractive poster
      isNewRelease = true,
      isForYou = true,
      episodes = initialEpisodes
    )
    _dramas.update { listOf(newDrama) + it }
  }

  fun deleteDrama(dramaId: String) {
    _dramas.update { it.filterNot { drama -> drama.id == dramaId } }
  }

  fun addEpisode(dramaId: String, title: String, videoUrl: String, durationSec: Int = 120) {
    _dramas.update { dramaList ->
      dramaList.map { drama ->
        if (drama.id == dramaId) {
          val nextNum = drama.episodes.size + 1
          val newEp = Episode(
            id = "${dramaId}_ep_$nextNum",
            dramaId = dramaId,
            episodeNumber = nextNum,
            title = title.ifBlank { "Episode $nextNum" },
            durationSeconds = durationSec,
            videoUrl = videoUrl
          )
          drama.copy(
            episodes = drama.episodes + newEp,
            episodesCount = drama.episodes.size + 1
          )
        } else drama
      }
    }
  }

  fun deleteEpisode(dramaId: String, episodeId: String) {
    _dramas.update { dramaList ->
      dramaList.map { drama ->
        if (drama.id == dramaId) {
          val filtered = drama.episodes.filterNot { it.id == episodeId }
          drama.copy(
            episodes = filtered,
            episodesCount = filtered.size
          )
        } else drama
      }
    }
  }

  fun updateAdConfig(newConfig: AdConfig) {
    _adConfig.value = newConfig
  }
}
