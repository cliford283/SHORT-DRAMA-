package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.example.data.AdConfig
import com.example.data.Drama
import com.example.data.Episode
import com.example.data.UserProfile
import com.example.ui.theme.DramaBlack
import com.example.ui.theme.DramaCardBg
import com.example.ui.theme.DramaRed
import com.example.ui.theme.DramaSurface
import com.example.ui.theme.DramaSurfaceVariant
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class, ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
  drama: Drama,
  initialEpisode: Episode,
  currentProfile: UserProfile,
  adConfig: AdConfig,
  onBackClick: () -> Unit,
  onFavoriteClick: (String) -> Unit,
  onDownloadClick: (String) -> Unit,
  onRecordProgress: (String, Int, Long, Long) -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  var wasPlayingBeforePause by remember { mutableStateOf(true) }

  var currentEpisode by remember(initialEpisode.id) { mutableStateOf(initialEpisode) }
  var isPlaying by remember { mutableStateOf(true) }
  var currentPositionMs by remember { mutableLongStateOf(0L) }
  var totalDurationMs by remember { mutableLongStateOf(106000L) }
  var areControlsVisible by remember { mutableStateOf(true) }

  // Modals
  var showEpisodesSheet by remember { mutableStateOf(false) }
  var showSettingsSheet by remember { mutableStateOf(false) }

  // Player settings
  var autoPlayNext by remember { mutableStateOf(true) }
  var selectedQuality by remember { mutableStateOf("Auto 1080p") }
  var selectedSubtitle by remember { mutableStateOf("English (Auto)") }

  // Ad states
  var isPreRollActive by remember { mutableStateOf(adConfig.isPreRollEnabled) }
  var preRollRemainingSec by remember { mutableStateOf(adConfig.preRollDurationSec) }
  var canSkipPreRoll by remember { mutableStateOf(false) }

  // Initialize ExoPlayer
  val exoPlayer = remember {
    ExoPlayer.Builder(context).build().apply {
      playWhenReady = true
      repeatMode = Player.REPEAT_MODE_OFF
    }
  }

  // Pre-roll Ad Timer
  LaunchedEffect(isPreRollActive) {
    if (isPreRollActive) {
      preRollRemainingSec = adConfig.preRollDurationSec
      canSkipPreRoll = false
      while (preRollRemainingSec > 0 && isPreRollActive) {
        delay(1000L)
        preRollRemainingSec--
        if (adConfig.preRollDurationSec - preRollRemainingSec >= adConfig.preRollSkipSec) {
          canSkipPreRoll = true
        }
      }
      isPreRollActive = false
    }
  }

  // Load episode stream into ExoPlayer once pre-roll completes
  LaunchedEffect(currentEpisode.id, isPreRollActive) {
    if (!isPreRollActive) {
      try {
        val mediaItem = MediaItem.fromUri(Uri.parse(currentEpisode.videoUrl))
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
        isPlaying = true
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }

  // Position and progress update loop
  LaunchedEffect(exoPlayer, isPreRollActive) {
    while (true) {
      if (!isPreRollActive && exoPlayer.isPlaying) {
        currentPositionMs = exoPlayer.currentPosition
        if (exoPlayer.duration > 0) {
          totalDurationMs = exoPlayer.duration
        }
        onRecordProgress(drama.id, currentEpisode.episodeNumber, currentPositionMs, totalDurationMs)
      }
      delay(500L)
    }
  }

  // Autoplay next episode listener
  DisposableEffect(exoPlayer) {
    val listener = object : Player.Listener {
      override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_ENDED) {
          if (autoPlayNext) {
            val currentIndex = drama.episodes.indexOfFirst { it.id == currentEpisode.id }
            if (currentIndex in 0 until drama.episodes.size - 1) {
              currentEpisode = drama.episodes[currentIndex + 1]
            }
          }
        }
      }
      override fun onIsPlayingChanged(playing: Boolean) {
        isPlaying = playing
      }
    }
    exoPlayer.addListener(listener)
    onDispose {
      exoPlayer.removeListener(listener)
      exoPlayer.release()
    }
  }

  // Lifecycle observer: pause on background, resume on foreground
  DisposableEffect(lifecycleOwner, exoPlayer) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
          wasPlayingBeforePause = exoPlayer.isPlaying
          exoPlayer.pause()
          isPlaying = false
        }
        Lifecycle.Event.ON_RESUME -> {
          if (wasPlayingBeforePause && !isPreRollActive) {
            exoPlayer.play()
            isPlaying = true
          }
        }
        else -> Unit
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
    }
  }

  val isFav = currentProfile.savedDramaIds.contains(drama.id)

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black)
      .pointerInput(Unit) {
        detectTapGestures(
          onTap = { areControlsVisible = !areControlsVisible }
        )
      }
  ) {
    // Media3 Video Surface
    AndroidView(
      factory = { ctx ->
        PlayerView(ctx).apply {
          player = exoPlayer
          useController = false
          layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
          )
        }
      },
      modifier = Modifier.fillMaxSize()
    )

    // Subtitle Display in center / lower third (matching photo 8 & 9)
    if (selectedSubtitle != "None" && !isPreRollActive) {
      val subtitle = currentEpisode.subtitleText.ifBlank { "Limited edition Bugatti." }
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .align(Alignment.Center)
          .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = subtitle,
          color = Color.White,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center,
          style = androidx.compose.ui.text.TextStyle(
            shadow = androidx.compose.ui.graphics.Shadow(
              color = Color.Black,
              blurRadius = 8f
            )
          )
        )
      }
    }

    // Top Right Watermark "FREE SHORT DRAMA" (Photo 9)
    Box(
      modifier = Modifier
        .align(Alignment.TopEnd)
        .padding(top = 54.dp, end = 16.dp)
        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
        .border(0.5.dp, DramaRed.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
        .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
      Text(
        text = "FREE SHORT DRAMA",
        color = DramaRed,
        fontSize = 8.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
      )
    }

    // Controls Overlay
    AnimatedVisibility(
      visible = areControlsVisible && !isPreRollActive,
      enter = fadeIn(),
      exit = fadeOut()
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors = listOf(
                Color.Black.copy(alpha = 0.7f),
                Color.Transparent,
                Color.Black.copy(alpha = 0.85f)
              )
            )
          )
      ) {
        // Top Navigation Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 24.dp)
            .align(Alignment.TopCenter),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = onBackClick,
            modifier = Modifier.testTag("btn_player_back")
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = Color.White
            )
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            // Heart Favorite
            IconButton(onClick = { onFavoriteClick(drama.id) }) {
              Icon(
                imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Favorite",
                tint = if (isFav) DramaRed else Color.White
              )
            }

            // Share
            IconButton(
              onClick = {
                val sendIntent = Intent().apply {
                  action = Intent.ACTION_SEND
                  putExtra(Intent.EXTRA_TEXT, "Watch ${drama.title} Episode ${currentEpisode.episodeNumber}!")
                  type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Share Episode"))
              }
            ) {
              Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share",
                tint = Color.White
              )
            }

            // Episodes List drawer button
            IconButton(
              onClick = { showEpisodesSheet = true },
              modifier = Modifier.testTag("btn_player_episodes")
            ) {
              Icon(
                imageVector = Icons.Default.ViewList,
                contentDescription = "Episodes",
                tint = Color.White
              )
            }

            // Settings drawer button
            IconButton(
              onClick = { showSettingsSheet = true },
              modifier = Modifier.testTag("btn_player_settings")
            ) {
              Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Settings",
                tint = Color.White
              )
            }
          }
        }

        // Center Playback Buttons: Prev, Rewind 10, Play/Pause, Forward 10, Next
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.Center),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Skip Prev
          IconButton(
            onClick = {
              val currentIndex = drama.episodes.indexOfFirst { it.id == currentEpisode.id }
              if (currentIndex > 0) {
                currentEpisode = drama.episodes[currentIndex - 1]
              }
            },
            modifier = Modifier.size(48.dp)
          ) {
            Icon(
              imageVector = Icons.Default.SkipPrevious,
              contentDescription = "Previous Episode",
              tint = Color.White,
              modifier = Modifier.size(28.dp)
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          // Rewind 10s
          IconButton(
            onClick = {
              val newPos = (exoPlayer.currentPosition - 10000).coerceAtLeast(0)
              exoPlayer.seekTo(newPos)
            },
            modifier = Modifier.size(48.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Replay10,
              contentDescription = "Rewind 10s",
              tint = Color.White,
              modifier = Modifier.size(30.dp)
            )
          }

          Spacer(modifier = Modifier.width(16.dp))

          // Big Center Play/Pause Circle
          Box(
            modifier = Modifier
              .size(68.dp)
              .clip(CircleShape)
              .background(Color.White)
              .clickable {
                if (exoPlayer.isPlaying) {
                  exoPlayer.pause()
                  isPlaying = false
                } else {
                  exoPlayer.play()
                  isPlaying = true
                }
              }
              .testTag("btn_player_toggle_playback"),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
              contentDescription = if (isPlaying) "Pause" else "Play",
              tint = Color.Black,
              modifier = Modifier.size(38.dp)
            )
          }

          Spacer(modifier = Modifier.width(16.dp))

          // Forward 10s
          IconButton(
            onClick = {
              val newPos = (exoPlayer.currentPosition + 10000).coerceAtMost(exoPlayer.duration)
              exoPlayer.seekTo(newPos)
            },
            modifier = Modifier.size(48.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Forward10,
              contentDescription = "Forward 10s",
              tint = Color.White,
              modifier = Modifier.size(30.dp)
            )
          }

          Spacer(modifier = Modifier.width(8.dp))

          // Skip Next
          IconButton(
            onClick = {
              val currentIndex = drama.episodes.indexOfFirst { it.id == currentEpisode.id }
              if (currentIndex in 0 until drama.episodes.size - 1) {
                currentEpisode = drama.episodes[currentIndex + 1]
              }
            },
            modifier = Modifier.size(48.dp)
          ) {
            Icon(
              imageVector = Icons.Default.SkipNext,
              contentDescription = "Next Episode",
              tint = Color.White,
              modifier = Modifier.size(28.dp)
            )
          }
        }

        // Bottom Details & Scrubber
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .padding(horizontal = 16.dp, vertical = 20.dp)
        ) {
          Text(
            text = drama.title,
            color = Color.White,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          )

          Text(
            text = "Episode ${currentEpisode.episodeNumber}/${drama.episodesCount}",
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
          )

          // Time numbers
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(
              text = formatTime(currentPositionMs),
              color = Color.White.copy(alpha = 0.8f),
              fontSize = 11.sp
            )
            Text(
              text = formatTime(totalDurationMs),
              color = Color.White.copy(alpha = 0.8f),
              fontSize = 11.sp
            )
          }

          // Scrubber Slider
          Slider(
            value = currentPositionMs.toFloat().coerceIn(0f, totalDurationMs.toFloat()),
            onValueChange = { newPos ->
              currentPositionMs = newPos.toLong()
              exoPlayer.seekTo(newPos.toLong())
            },
            valueRange = 0f..totalDurationMs.toFloat().coerceAtLeast(1f),
            colors = SliderDefaults.colors(
              thumbColor = DramaRed,
              activeTrackColor = DramaRed,
              inactiveTrackColor = Color.White.copy(alpha = 0.3f)
            ),
            modifier = Modifier
              .fillMaxWidth()
              .height(24.dp)
              .testTag("player_scrubber")
          )
        }
      }
    }

    // Pre-roll Ad Overlay (AdSense Engine)
    if (isPreRollActive) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(Color.Black.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(
            modifier = Modifier
              .background(DramaRed, RoundedCornerShape(4.dp))
              .padding(horizontal = 8.dp, vertical = 3.dp)
          ) {
            Text(
              text = "ADSENSE SPONSORED AD",
              color = Color.White,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.sp
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = adConfig.preRollAdTitle,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
          )

          Text(
            text = adConfig.preRollAdDescription,
            color = Color.White.copy(alpha = 0.75f),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
          )

          // Countdown / Skip Ad Button
          Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            if (canSkipPreRoll) {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(20.dp))
                  .background(Color.White)
                  .clickable { isPreRollActive = false }
                  .padding(horizontal = 20.dp, vertical = 10.dp)
                  .testTag("btn_skip_ad")
              ) {
                Text(
                  text = "Skip Ad ▶",
                  color = Color.Black,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp
                )
              }
            } else {
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(20.dp))
                  .background(Color.White.copy(alpha = 0.2f))
                  .padding(horizontal = 16.dp, vertical = 8.dp)
              ) {
                Text(
                  text = "Ad playing • Skip in ${preRollRemainingSec}s",
                  color = Color.White,
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium
                )
              }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Visit Link
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .clickable {
                  runCatching {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(adConfig.preRollClickUrl))
                    context.startActivity(intent)
                  }
                }
                .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
              Text(
                text = "Learn More ↗",
                color = Color.White,
                fontSize = 12.sp
              )
            }
          }
        }
      }
    }

    // Modal Bottom Sheet: Episodes Drawer (Photo 10)
    if (showEpisodesSheet) {
      ModalBottomSheet(
        onDismissRequest = { showEpisodesSheet = false },
        containerColor = DramaSurface,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        sheetState = rememberModalBottomSheetState()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
        ) {
          Text(
            text = "Episodes",
            color = Color.White,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
          )

          LazyColumn(
            modifier = Modifier.fillMaxWidth()
          ) {
            items(drama.episodes) { ep ->
              val isCurrent = ep.id == currentEpisode.id
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable {
                    currentEpisode = ep
                    showEpisodesSheet = false
                  }
                  .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  // Thumbnail
                  Box(
                    modifier = Modifier
                      .size(width = 64.dp, height = 44.dp)
                      .clip(RoundedCornerShape(4.dp))
                      .background(DramaCardBg),
                    contentAlignment = Alignment.Center
                  ) {
                    if (drama.coverResId != null) {
                      AsyncImage(
                        model = drama.coverResId,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                      )
                    }
                    if (isCurrent) {
                      Box(
                        modifier = Modifier
                          .fillMaxSize()
                          .background(Color.Black.copy(alpha = 0.4f)),
                        contentAlignment = Alignment.Center
                      ) {
                        Icon(
                          imageVector = Icons.Filled.PlayArrow,
                          contentDescription = null,
                          tint = DramaRed,
                          modifier = Modifier.size(20.dp)
                        )
                      }
                    }
                  }

                  Spacer(modifier = Modifier.width(12.dp))

                  Column {
                    Text(
                      text = "Episode ${ep.episodeNumber}",
                      color = if (isCurrent) DramaRed else Color.White,
                      fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                      fontSize = 14.sp
                    )
                    Text(
                      text = "${ep.durationSeconds}s",
                      color = Color.White.copy(alpha = 0.5f),
                      fontSize = 11.sp
                    )
                  }
                }

                IconButton(onClick = { onDownloadClick(drama.id) }) {
                  Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download Episode",
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                  )
                }
              }
            }
          }
        }
      }
    }

    // Modal Bottom Sheet: Settings Drawer (Photo 11)
    if (showSettingsSheet) {
      ModalBottomSheet(
        onDismissRequest = { showSettingsSheet = false },
        containerColor = DramaSurface,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        sheetState = rememberModalBottomSheetState()
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .padding(bottom = 32.dp)
        ) {
          Text(
            text = "Settings",
            color = Color.White,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 16.dp)
          )

          // Auto Play Next Toggle
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "AUTO PLAY NEXT",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                letterSpacing = 0.5.sp
              )
              Text(
                text = "Play next episode automatically",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 2.dp)
              )
            }

            Switch(
              checked = autoPlayNext,
              onCheckedChange = { autoPlayNext = it },
              colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = DramaRed,
                uncheckedTrackColor = DramaSurfaceVariant
              )
            )
          }

          Spacer(modifier = Modifier.height(16.dp))

          // Video Quality Selector
          Text(
            text = "VIDEO QUALITY",
            color = DramaRed,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
          )

          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            listOf("Auto 1080p", "720p HD", "480p SD").forEach { quality ->
              val isSel = selectedQuality == quality
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (isSel) DramaSurfaceVariant else Color.Transparent)
                  .border(
                    1.dp,
                    if (isSel) DramaRed else Color.White.copy(alpha = 0.2f),
                    RoundedCornerShape(6.dp)
                  )
                  .clickable { selectedQuality = quality }
                  .padding(horizontal = 12.dp, vertical = 6.dp)
              ) {
                Text(
                  text = quality,
                  color = if (isSel) Color.White else Color.White.copy(alpha = 0.6f),
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Subtitles Selector
          Text(
            text = "SUBTITLES",
            color = DramaRed,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp)
          )

          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            listOf("None", "English (Auto)", "Spanish (Auto)").forEach { sub ->
              val isSel = selectedSubtitle == sub
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (isSel) DramaSurfaceVariant else Color.Transparent)
                  .border(
                    1.dp,
                    if (isSel) DramaRed else Color.White.copy(alpha = 0.2f),
                    RoundedCornerShape(6.dp)
                  )
                  .clickable { selectedSubtitle = sub }
                  .padding(horizontal = 12.dp, vertical = 6.dp)
              ) {
                Text(
                  text = sub,
                  color = if (isSel) Color.White else Color.White.copy(alpha = 0.6f),
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }
        }
      }
    }
  }
}

private fun formatTime(millis: Long): String {
  val totalSec = (millis / 1000).coerceAtLeast(0)
  val minutes = totalSec / 60
  val seconds = totalSec % 60
  return "%d:%02d".format(minutes, seconds)
}
