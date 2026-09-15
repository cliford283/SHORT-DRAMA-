package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.Drama
import com.example.data.Episode
import com.example.data.UserProfile
import com.example.ui.theme.DramaBlack
import com.example.ui.theme.DramaCardBg
import com.example.ui.theme.DramaPill
import com.example.ui.theme.DramaRed
import com.example.ui.theme.DramaSurfaceVariant

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import com.example.ui.components.DramaDetailSkeleton
import com.example.ui.components.shimmerBrush
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun DramaDetailScreen(
  drama: Drama,
  currentProfile: UserProfile,
  onBackClick: () -> Unit,
  onPlayEpisode: (Drama, Episode) -> Unit,
  onFavoriteClick: (String) -> Unit,
  modifier: Modifier = Modifier,
  isLoading: Boolean = false
) {
  if (isLoading) {
    DramaDetailSkeleton(onBackClick = onBackClick, modifier = modifier)
    return
  }

  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val isFav = currentProfile.savedDramaIds.contains(drama.id)
  var showWatchlistToast by remember { mutableStateOf(false) }
  var watchlistToastMessage by remember { mutableStateOf("") }

  // Check if user has saved playback progress in Firestore
  val savedProgress = currentProfile.watchHistory[drama.id]
  val resumeEp = savedProgress?.let { prog ->
    drama.episodes.find { it.episodeNumber == prog.episodeNumber }
  } ?: drama.episodes.firstOrNull()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(DramaBlack),
    contentPadding = PaddingValues(bottom = 90.dp)
  ) {
    // Hero Header with Poster & Controls
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(420.dp)
      ) {
        // Poster Background
        if (drama.coverResId != null) {
          AsyncImage(
            model = drama.coverResId,
            contentDescription = drama.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        } else if (!drama.coverUrl.isNullOrBlank()) {
          AsyncImage(
            model = drama.coverUrl,
            contentDescription = drama.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        } else {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(DramaCardBg)
          )
        }

        // Dark Gradient overlay
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  Color.Black.copy(alpha = 0.5f),
                  Color.Transparent,
                  DramaBlack.copy(alpha = 0.8f),
                  DramaBlack
                )
              )
            )
        )

        // Top Bar: Back & Share buttons
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .align(Alignment.TopCenter),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
          ) {
            IconButton(
              onClick = onBackClick,
              modifier = Modifier.testTag("btn_detail_back")
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
              )
            }
          }

          Box(
            modifier = Modifier
              .size(40.dp)
              .clip(CircleShape)
              .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
          ) {
            IconButton(
              onClick = {
                val sendIntent = Intent().apply {
                  action = Intent.ACTION_SEND
                  putExtra(Intent.EXTRA_TEXT, "Watch ${drama.title} on Free Short Drama!")
                  type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, "Share Drama"))
              },
              modifier = Modifier.testTag("btn_detail_share")
            ) {
              Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share",
                tint = Color.White
              )
            }
          }
        }

        // Bottom Info on Hero
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomStart)
            .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
          // Tags
          Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            val displayTags = if (drama.tags.isNotEmpty()) drama.tags else listOf("FREE STREAM", "SHORT NOVEL", "EXCLUSIVE")
            displayTags.take(3).forEach { tag ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .background(Color.White.copy(alpha = 0.15f))
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = tag,
                  color = Color.White.copy(alpha = 0.9f),
                  fontSize = 9.sp,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1
                )
              }
            }
          }

          // Title
          Text(
            text = drama.title,
            color = Color.White,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp,
            modifier = Modifier.padding(top = 10.dp)
          )

          // Episodes Count
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 6.dp)
          ) {
            Icon(
              imageVector = Icons.Filled.PlayArrow,
              contentDescription = null,
              tint = DramaRed,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${drama.episodesCount} Episodes",
              color = Color.White.copy(alpha = 0.9f),
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold
            )
          }

          // Action Buttons: Play + Watchlist Heart Toggle
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Button(
              onClick = {
                val targetEp = resumeEp ?: drama.episodes.firstOrNull() ?: Episode(
                  id = "${drama.id}_ep_1",
                  dramaId = drama.id,
                  episodeNumber = 1,
                  title = "Episode 1",
                  videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
                )
                onPlayEpisode(drama, targetEp)
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = Color.White,
                contentColor = Color.Black
              ),
              shape = RoundedCornerShape(4.dp),
              modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .testTag("btn_detail_play")
            ) {
              Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = if (savedProgress != null && savedProgress.episodeNumber > 1) {
                  "Resume Ep ${savedProgress.episodeNumber}"
                } else if (savedProgress != null) {
                  "Resume Ep 1"
                } else {
                  "Play"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Watchlist Heart Toggle Icon
            Box(
              modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(4.dp))
                .border(
                  width = 1.dp,
                  color = if (isFav) DramaRed else Color.White.copy(alpha = 0.4f),
                  shape = RoundedCornerShape(4.dp)
                )
                .background(if (isFav) DramaRed.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.5f))
                .clickable {
                  val willBeSaved = !isFav
                  onFavoriteClick(drama.id)
                  watchlistToastMessage = if (willBeSaved) "Added to Watchlist ❤️" else "Removed from Watchlist"
                  showWatchlistToast = true
                  coroutineScope.launch {
                    delay(2000L)
                    showWatchlistToast = false
                  }
                }
                .testTag("btn_detail_watchlist_toggle"),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (isFav) "Remove from Watchlist" else "Add to Watchlist",
                tint = if (isFav) DramaRed else Color.White,
                modifier = Modifier
                  .size(22.dp)
                  .testTag("btn_detail_heart")
              )
            }
          }

          // Watchlist feedback toast
          AnimatedVisibility(
            visible = showWatchlistToast,
            enter = fadeIn(),
            exit = fadeOut()
          ) {
            Box(
              modifier = Modifier
                .padding(top = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isFav) DramaRed else Color(0xFF2C2D35))
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Text(
                text = watchlistToastMessage,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }

    // Storyline Section
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Text(
          text = "STORYLINE",
          color = DramaRed,
          fontSize = 10.sp,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = 1.5.sp
        )

        Text(
          text = drama.description,
          color = Color.White.copy(alpha = 0.85f),
          fontSize = 13.sp,
          lineHeight = 19.sp,
          modifier = Modifier.padding(top = 6.dp)
        )
      }
    }

    // Episodes Section Header
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "EPISODES",
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 15.sp,
          letterSpacing = 0.5.sp
        )

        Text(
          text = "${drama.episodes.size.coerceAtLeast(drama.episodesCount)} Clips",
          color = Color.White.copy(alpha = 0.6f),
          fontSize = 12.sp
        )
      }
    }

    // Episodes Grid (4 columns)
    val episodes = drama.episodes.ifEmpty {
      (1..drama.episodesCount).map { epNum ->
        Episode(
          id = "${drama.id}_ep_$epNum",
          dramaId = drama.id,
          episodeNumber = epNum,
          title = "Episode $epNum",
          videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
        )
      }
    }

    val chunked = episodes.chunked(4)
    items(chunked) { rowEps ->
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        for (ep in rowEps) {
          EpisodeThumbCard(
            episode = ep,
            drama = drama,
            onClick = { onPlayEpisode(drama, ep) },
            modifier = Modifier.weight(1f)
          )
        }
        repeat(4 - rowEps.size) {
          Spacer(modifier = Modifier.weight(1f))
        }
      }
    }
  }
}

@Composable
fun EpisodeThumbCard(
  episode: Episode,
  drama: Drama,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .aspectRatio(0.75f)
      .clip(RoundedCornerShape(6.dp))
      .background(DramaCardBg)
      .clickable { onClick() }
      .testTag("ep_card_${episode.episodeNumber}")
  ) {
    if (drama.coverResId != null) {
      AsyncImage(
        model = drama.coverResId,
        contentDescription = episode.title,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
      )
    }

    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              Color.Transparent,
              Color.Black.copy(alpha = 0.85f)
            )
          )
        )
    )

    Text(
      text = "EP ${episode.episodeNumber}",
      color = Color.White,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 6.dp)
    )
  }
}
