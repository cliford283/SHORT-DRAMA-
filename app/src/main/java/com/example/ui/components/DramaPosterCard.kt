package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.Drama
import com.example.ui.theme.DramaBlack
import com.example.ui.theme.DramaCardBg
import com.example.ui.theme.DramaRed

@Composable
fun DramaPosterCard(
  drama: Drama,
  isFavorite: Boolean,
  onCardClick: () -> Unit,
  onFavoriteClick: () -> Unit,
  modifier: Modifier = Modifier,
  showProgress: Boolean = false,
  showDismiss: Boolean = false,
  onDismissClick: (() -> Unit)? = null,
  showTitleBelow: Boolean = false,
  aspectRatio: Float = 0.68f
) {
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .clickable { onCardClick() }
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(aspectRatio)
        .clip(RoundedCornerShape(8.dp))
        .background(DramaCardBg)
    ) {
      // Poster Image
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
            .background(DramaCardBg),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = drama.title,
            color = Color.White,
            fontSize = 12.sp,
            modifier = Modifier.padding(8.dp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      // Subtle gradient at bottom for readability
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.verticalGradient(
              colors = listOf(
                Color.Black.copy(alpha = 0.4f),
                Color.Transparent,
                Color.Black.copy(alpha = 0.75f)
              )
            )
          )
      )

      // Top Left Favorite Button
      IconButton(
        onClick = onFavoriteClick,
        modifier = Modifier
          .align(Alignment.TopStart)
          .size(36.dp)
          .testTag("fav_btn_${drama.id}")
      ) {
        Icon(
          imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
          contentDescription = "Favorite",
          tint = if (isFavorite) DramaRed else Color.White.copy(alpha = 0.85f),
          modifier = Modifier.size(18.dp)
        )
      }

      // Top Right: Dismiss (X) or Episode Count badge
      if (showDismiss && onDismissClick != null) {
        IconButton(
          onClick = onDismissClick,
          modifier = Modifier
            .align(Alignment.TopEnd)
            .size(36.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Dismiss",
            tint = Color.White.copy(alpha = 0.85f),
            modifier = Modifier.size(16.dp)
          )
        }
      } else {
        Box(
          modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(6.dp)
            .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
          Text(
            text = "${drama.episodesCount} EP",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      // Bottom Progress Bar if Resume
      if (showProgress) {
        LinearProgressIndicator(
          progress = { drama.resumeProgress },
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.BottomCenter)
            .height(3.dp),
          color = DramaRed,
          trackColor = Color.White.copy(alpha = 0.2f),
        )
      }
    }

    if (showTitleBelow) {
      Text(
        text = drama.title,
        color = Color.White,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(top = 4.dp, start = 2.dp, end = 2.dp)
      )
    }
  }
}
