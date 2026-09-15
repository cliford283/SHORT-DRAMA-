package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.Drama
import com.example.data.UserProfile
import com.example.data.db.WatchlistEntity
import com.example.ui.components.DramaPosterCard
import com.example.ui.theme.DramaBlack
import com.example.ui.theme.DramaCardBg
import com.example.ui.theme.DramaRed

@Composable
fun MyListScreen(
  dramas: List<Drama>,
  currentProfile: UserProfile,
  watchlist: List<WatchlistEntity> = emptyList(),
  onDramaClick: (Drama) -> Unit,
  onFavoriteClick: (String) -> Unit,
  onRemoveDownload: (String) -> Unit,
  onRemoveFromWatchlist: (String) -> Unit = {},
  modifier: Modifier = Modifier
) {
  val downloadedDramas = dramas.filter { currentProfile.downloadedDramaIds.contains(it.id) }
  val favoriteDramas = dramas.filter { currentProfile.savedDramaIds.contains(it.id) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(DramaBlack),
    contentPadding = PaddingValues(bottom = 90.dp)
  ) {
    // Header
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 16.dp, end = 16.dp, top = 28.dp, bottom = 12.dp)
      ) {
        Text(
          text = "MY LIST",
          color = Color.White,
          fontSize = 34.sp,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = 1.sp
        )

        Text(
          text = "Favorites & offline episodes",
          color = Color.White.copy(alpha = 0.6f),
          fontSize = 13.sp,
          modifier = Modifier.padding(top = 2.dp)
        )
      }
    }

    // ROOM DATABASE PERSISTENT WATCHLIST SECTION
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp)
      ) {
        Text(
          text = "ROOM DATABASE",
          color = DramaRed,
          fontSize = 9.sp,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = 1.5.sp
        )
        Text(
          text = "Persistent Watchlist (${watchlist.size})",
          color = Color.White,
          fontFamily = FontFamily.Serif,
          fontWeight = FontWeight.Bold,
          fontSize = 20.sp,
          modifier = Modifier.padding(top = 2.dp)
        )
      }
    }

    if (watchlist.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(DramaCardBg, RoundedCornerShape(8.dp))
            .padding(16.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Your persistent Room database watchlist is empty.\nTap the heart icon on any drama to save it!",
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 13.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        }
      }
    } else {
      items(watchlist) { item ->
        val matchingDrama = dramas.find { it.id == item.id }
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DramaCardBg)
            .clickable {
              matchingDrama?.let { onDramaClick(it) }
            }
            .padding(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Poster thumbnail
          Box(
            modifier = Modifier
              .width(55.dp)
              .aspectRatio(0.70f)
              .clip(RoundedCornerShape(4.dp))
          ) {
            if (item.coverResId != null) {
              AsyncImage(
                model = item.coverResId,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )
            } else if (!item.coverUrl.isNullOrBlank()) {
              AsyncImage(
                model = item.coverUrl,
                contentDescription = item.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )
            } else {
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(Color.DarkGray)
              )
            }
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = item.title,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Text(
              text = item.genre,
              color = DramaRed,
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(top = 2.dp)
            )
            Text(
              text = "${item.episodesCount} Episodes • Watchlist Saved",
              color = Color.White.copy(alpha = 0.6f),
              fontSize = 11.sp,
              modifier = Modifier.padding(top = 2.dp)
            )
          }

          // Delete / Remove from Watchlist button
          IconButton(
            onClick = { onRemoveFromWatchlist(item.id) },
            modifier = Modifier.testTag("btn_remove_watchlist_${item.id}")
          ) {
            Icon(
              imageVector = Icons.Default.DeleteOutline,
              contentDescription = "Remove from Watchlist",
              tint = DramaRed
            )
          }
        }
      }
    }

    // OFFLINE Downloads Section (Photo 14)
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp)
      ) {
        Text(
          text = "OFFLINE",
          color = DramaRed,
          fontSize = 9.sp,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = 1.5.sp
        )
        Text(
          text = "Downloads",
          color = Color.White,
          fontFamily = FontFamily.Serif,
          fontWeight = FontWeight.Bold,
          fontSize = 20.sp,
          modifier = Modifier.padding(top = 2.dp)
        )
      }
    }

    if (downloadedDramas.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "No offline episodes downloaded yet.",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 13.sp
          )
        }
      }
    } else {
      items(downloadedDramas) { drama ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DramaCardBg)
            .clickable { onDramaClick(drama) }
            .padding(10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(width = 60.dp, height = 80.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color.Black)
            ) {
              if (drama.coverResId != null) {
                AsyncImage(
                  model = drama.coverResId,
                  contentDescription = drama.title,
                  contentScale = ContentScale.Crop,
                  modifier = Modifier.fillMaxSize()
                )
              }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
              Box(
                modifier = Modifier
                  .background(DramaRed, RoundedCornerShape(3.dp))
                  .padding(horizontal = 5.dp, vertical = 2.dp)
              ) {
                Text(
                  text = "COMPLETE",
                  color = Color.White,
                  fontSize = 8.sp,
                  fontWeight = FontWeight.Bold
                )
              }

              Text(
                text = drama.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
              )

              Text(
                text = "All episodes downloaded",
                color = DramaRed,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
              )
            }
          }

          IconButton(
            onClick = { onRemoveDownload(drama.id) },
            modifier = Modifier.testTag("btn_remove_download_${drama.id}")
          ) {
            Icon(
              imageVector = Icons.Default.DeleteOutline,
              contentDescription = "Delete Download",
              tint = Color.White.copy(alpha = 0.6f)
            )
          }
        }
      }
    }

    // YOUR LIST Favorites Section (Photo 14)
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
      ) {
        Text(
          text = "YOUR LIST",
          color = DramaRed,
          fontSize = 9.sp,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = 1.5.sp
        )
        Text(
          text = "Favorites",
          color = Color.White,
          fontFamily = FontFamily.Serif,
          fontWeight = FontWeight.Bold,
          fontSize = 20.sp,
          modifier = Modifier.padding(top = 2.dp)
        )
      }
    }

    if (favoriteDramas.isEmpty()) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Your list is empty. Tap the heart on any drama to add it here!",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 13.sp
          )
        }
      }
    } else {
      val chunked = favoriteDramas.chunked(3)
      items(chunked) { rowDramas ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          for (drama in rowDramas) {
            DramaPosterCard(
              drama = drama,
              isFavorite = true,
              onCardClick = { onDramaClick(drama) },
              onFavoriteClick = { onFavoriteClick(drama.id) },
              aspectRatio = 0.70f,
              showTitleBelow = true,
              modifier = Modifier.weight(1f)
            )
          }
          repeat(3 - rowDramas.size) {
            Spacer(modifier = Modifier.weight(1f))
          }
        }
      }
    }

    // Version Footer (Photo 14)
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 32.dp, bottom = 16.dp),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = "Free Short Drama • Version 1.3.1 (23)",
          color = Color.White.copy(alpha = 0.35f),
          fontSize = 11.sp
        )
      }
    }
  }
}
