package com.example.ui.components

import android.net.Uri
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay

/**
 * Reusable VideoPlayer composable using Media3 ExoPlayer.
 *
 * Handles:
 * - Playback of streaming or local video URLs.
 * - State persistence across configuration changes and lifecycle events.
 * - Automatic pausing on ON_PAUSE / ON_STOP and resuming on ON_RESUME.
 * - Clean disposal and release of ExoPlayer resources.
 */
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
  videoUrl: String,
  modifier: Modifier = Modifier,
  playWhenReady: Boolean = true,
  initialPositionMs: Long = 0L,
  useController: Boolean = false,
  resizeMode: Int = AspectRatioFrameLayout.RESIZE_MODE_ZOOM,
  onPositionChanged: ((currentMs: Long, durationMs: Long) -> Unit)? = null,
  onPlaybackEnded: (() -> Unit)? = null,
  onIsPlayingChanged: ((Boolean) -> Unit)? = null,
  onPlayerReady: ((ExoPlayer) -> Unit)? = null
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current

  // State persistence across recomposition & lifecycle recreation
  var savedPositionMs by rememberSaveable(videoUrl) { mutableLongStateOf(initialPositionMs) }
  var wasPlayingBeforePause by rememberSaveable { mutableStateOf(playWhenReady) }

  val currentOnPositionChanged by rememberUpdatedState(onPositionChanged)
  val currentOnPlaybackEnded by rememberUpdatedState(onPlaybackEnded)
  val currentOnIsPlayingChanged by rememberUpdatedState(onIsPlayingChanged)
  val currentOnPlayerReady by rememberUpdatedState(onPlayerReady)

  // Initialize Media3 ExoPlayer instance
  val exoPlayer = remember(context) {
    ExoPlayer.Builder(context).build().apply {
      repeatMode = Player.REPEAT_MODE_OFF
    }
  }

  // Notify parent of ready player
  LaunchedEffect(exoPlayer) {
    currentOnPlayerReady?.invoke(exoPlayer)
  }

  // Load and prepare media whenever videoUrl changes
  LaunchedEffect(videoUrl) {
    try {
      val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
      exoPlayer.setMediaItem(mediaItem)
      if (savedPositionMs > 0L) {
        exoPlayer.seekTo(savedPositionMs)
      }
      exoPlayer.prepare()
      exoPlayer.playWhenReady = playWhenReady
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  // Sync playWhenReady parameter
  LaunchedEffect(playWhenReady) {
    exoPlayer.playWhenReady = playWhenReady
  }

  // Periodic position observer for state persistence and progress callbacks
  LaunchedEffect(exoPlayer) {
    while (true) {
      if (exoPlayer.isPlaying) {
        val current = exoPlayer.currentPosition
        val duration = if (exoPlayer.duration > 0) exoPlayer.duration else 0L
        savedPositionMs = current
        currentOnPositionChanged?.invoke(current, duration)
      }
      delay(500L)
    }
  }

  // Media3 Player Event Listener
  DisposableEffect(exoPlayer) {
    val listener = object : Player.Listener {
      override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_ENDED) {
          currentOnPlaybackEnded?.invoke()
        }
      }

      override fun onIsPlayingChanged(isPlaying: Boolean) {
        currentOnIsPlayingChanged?.invoke(isPlaying)
      }
    }
    exoPlayer.addListener(listener)
    onDispose {
      exoPlayer.removeListener(listener)
    }
  }

  // Lifecycle observer: automatic pausing and resuming
  DisposableEffect(lifecycleOwner, exoPlayer) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP -> {
          savedPositionMs = exoPlayer.currentPosition
          wasPlayingBeforePause = exoPlayer.isPlaying
          exoPlayer.pause()
        }
        Lifecycle.Event.ON_RESUME -> {
          if (wasPlayingBeforePause && playWhenReady) {
            exoPlayer.play()
          }
        }
        Lifecycle.Event.ON_DESTROY -> {
          savedPositionMs = exoPlayer.currentPosition
          exoPlayer.stop()
          exoPlayer.release()
        }
        else -> Unit
      }
    }

    lifecycleOwner.lifecycle.addObserver(observer)

    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
      savedPositionMs = exoPlayer.currentPosition
      exoPlayer.release()
    }
  }

  // AndroidView hosting the Media3 PlayerView surface
  Box(
    modifier = modifier
      .fillMaxSize()
      .background(Color.Black)
  ) {
    AndroidView(
      factory = { ctx ->
        PlayerView(ctx).apply {
          player = exoPlayer
          this.useController = useController
          this.resizeMode = resizeMode
          layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
          )
          keepScreenOn = true
        }
      },
      update = { playerView ->
        playerView.player = exoPlayer
        playerView.useController = useController
        playerView.resizeMode = resizeMode
      },
      modifier = Modifier.fillMaxSize()
    )
  }
}
