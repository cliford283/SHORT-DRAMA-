package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DramaBlack
import com.example.ui.theme.DramaCardBg
import com.example.ui.theme.DramaRed

@Composable
fun shimmerBrush(targetValue: Float = 1000f, showShimmer: Boolean = true): Brush {
  return if (showShimmer) {
    val shimmerColors = listOf(
      Color(0xFF1B1C22),
      Color(0xFF2E303B),
      Color(0xFF1B1C22)
    )

    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnimation = transition.animateFloat(
      initialValue = 0f,
      targetValue = targetValue,
      animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Restart
      ),
      label = "shimmerTranslate"
    )

    Brush.linearGradient(
      colors = shimmerColors,
      start = Offset.Zero,
      end = Offset(x = translateAnimation.value, y = translateAnimation.value)
    )
  } else {
    Brush.linearGradient(
      colors = listOf(DramaCardBg, DramaCardBg),
      start = Offset.Zero,
      end = Offset.Zero
    )
  }
}

@Composable
fun DramaCardSkeleton(
  modifier: Modifier = Modifier,
  aspectRatio: Float = 0.68f
) {
  val brush = shimmerBrush()
  Column(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(aspectRatio)
        .clip(RoundedCornerShape(8.dp))
        .background(brush)
    )
    Spacer(modifier = Modifier.height(6.dp))
    Box(
      modifier = Modifier
        .fillMaxWidth(0.75f)
        .height(10.dp)
        .clip(RoundedCornerShape(4.dp))
        .background(brush)
    )
  }
}

@Composable
fun DramaRowSkeleton(
  modifier: Modifier = Modifier,
  cardWidth: Float = 115f,
  aspectRatio: Float = 0.70f
) {
  LazyRow(
    contentPadding = PaddingValues(horizontal = 16.dp),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    modifier = modifier.fillMaxWidth()
  ) {
    items(5) {
      DramaCardSkeleton(
        modifier = Modifier.width(cardWidth.dp),
        aspectRatio = aspectRatio
      )
    }
  }
}

@Composable
fun DramaGridSkeleton(
  modifier: Modifier = Modifier,
  columns: Int = 3,
  rowsCount: Int = 2
) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 4.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    repeat(rowsCount) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        repeat(columns) {
          DramaCardSkeleton(
            modifier = Modifier.weight(1f),
            aspectRatio = 0.70f
          )
        }
      }
    }
  }
}

@Composable
fun FirestoreLoadingSpinner(
  label: String = "Connecting to Firestore...",
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .clip(RoundedCornerShape(20.dp))
      .background(Color(0xFF1E2029))
      .padding(horizontal = 14.dp, vertical = 8.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Center
  ) {
    CircularProgressIndicator(
      modifier = Modifier.size(16.dp),
      strokeWidth = 2.dp,
      color = DramaRed
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = label,
      color = Color.White.copy(alpha = 0.85f),
      fontSize = 12.sp,
      fontWeight = FontWeight.Medium
    )
  }
}

@Composable
fun DramaDetailSkeleton(
  onBackClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val brush = shimmerBrush()
  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DramaBlack)
  ) {
    // Hero Poster Skeleton
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(380.dp)
        .background(brush)
    ) {
      // Top back button
      Box(
        modifier = Modifier
          .padding(start = 16.dp, top = 24.dp)
          .size(40.dp)
          .clip(CircleShape)
          .background(Color.Black.copy(alpha = 0.5f)),
        contentAlignment = Alignment.Center
      ) {
        IconButton(onClick = onBackClick) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.White
          )
        }
      }

      // Bottom floating badges
      Column(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .padding(16.dp)
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Box(
            modifier = Modifier
              .size(width = 60.dp, height = 16.dp)
              .clip(RoundedCornerShape(4.dp))
              .background(Color.White.copy(alpha = 0.3f))
          )
          Box(
            modifier = Modifier
              .size(width = 70.dp, height = 16.dp)
              .clip(RoundedCornerShape(4.dp))
              .background(Color.White.copy(alpha = 0.3f))
          )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Box(
          modifier = Modifier
            .size(width = 220.dp, height = 24.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(Color.White.copy(alpha = 0.4f))
        )
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Play & Watchlist Action button skeleton
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      Box(
        modifier = Modifier
          .weight(1f)
          .height(44.dp)
          .clip(RoundedCornerShape(4.dp))
          .background(brush)
      )
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(RoundedCornerShape(4.dp))
          .background(brush)
      )
    }

    Spacer(modifier = Modifier.height(20.dp))

    // Storyline text placeholder
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Box(
        modifier = Modifier
          .fillMaxWidth(0.3f)
          .height(12.dp)
          .clip(RoundedCornerShape(3.dp))
          .background(brush)
      )
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(12.dp)
          .clip(RoundedCornerShape(3.dp))
          .background(brush)
      )
      Box(
        modifier = Modifier
          .fillMaxWidth(0.85f)
          .height(12.dp)
          .clip(RoundedCornerShape(3.dp))
          .background(brush)
      )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Episodes grid skeleton
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      repeat(4) {
        Box(
          modifier = Modifier
            .weight(1f)
            .aspectRatio(0.75f)
            .clip(RoundedCornerShape(6.dp))
            .background(brush)
        )
      }
    }
  }
}
