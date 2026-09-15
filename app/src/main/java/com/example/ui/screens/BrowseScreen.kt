package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Drama
import com.example.data.UserProfile
import com.example.ui.components.DramaPosterCard
import com.example.ui.theme.DramaBlack
import com.example.ui.theme.DramaBorder
import com.example.ui.theme.DramaCardBg
import com.example.ui.theme.DramaRed
import com.example.ui.theme.DramaSurfaceVariant

@Composable
fun BrowseScreen(
  dramas: List<Drama>,
  currentProfile: UserProfile,
  onDramaClick: (Drama) -> Unit,
  onFavoriteClick: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedCategory by remember { mutableStateOf("COLLECTION") }

  val categories = listOf(
    "COLLECTION",
    "NEW",
    "HOTLIST",
    "FANTASY",
    "ASIA",
    "FEMALE",
    "MALE",
    "CEO/ROMANCE",
    "REVENGE"
  )

  val filteredDramas = dramas.filter { drama ->
    when (selectedCategory) {
      "COLLECTION" -> true
      "NEW" -> drama.isNewRelease
      "HOTLIST" -> drama.isTrending
      "FANTASY" -> drama.genre.contains("Fantasy", ignoreCase = true) || drama.genre.contains("Werewolf", ignoreCase = true) || drama.genre.contains("Magic", ignoreCase = true)
      "ASIA" -> drama.genre.contains("Martial", ignoreCase = true) || drama.tags.any { it.contains("REBIRTH", ignoreCase = true) || it.contains("IMMORTAL", ignoreCase = true) }
      "FEMALE" -> drama.genre.contains("Romance", ignoreCase = true) || drama.tags.any { it.contains("MARRIAGE", ignoreCase = true) || it.contains("QUEEN", ignoreCase = true) }
      "MALE" -> drama.genre.contains("Billionaire", ignoreCase = true) || drama.genre.contains("Mafia", ignoreCase = true)
      "CEO/ROMANCE" -> drama.genre.contains("Billionaire", ignoreCase = true) || drama.genre.contains("Romance", ignoreCase = true) || drama.tags.any { it.contains("BILLIONAIRE", ignoreCase = true) }
      "REVENGE" -> drama.genre.contains("Revenge", ignoreCase = true) || drama.tags.any { it.contains("REVENGE", ignoreCase = true) }
      else -> true
    }
  }

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
          text = "BROWSE",
          color = Color.White,
          fontSize = 34.sp,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = 1.sp
        )

        Text(
          text = "Channels • curated shelves",
          color = Color.White.copy(alpha = 0.6f),
          fontSize = 13.sp,
          modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
        )

        // Horizontal Category Filter Pills
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          items(categories) { category ->
            val isSelected = selectedCategory == category
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .border(
                  1.dp,
                  if (isSelected) DramaRed else DramaBorder,
                  RoundedCornerShape(4.dp)
                )
                .background(if (isSelected) DramaRed else DramaCardBg)
                .clickable { selectedCategory = category }
                .padding(horizontal = 14.dp, vertical = 7.dp)
                .testTag("browse_cat_$category")
            ) {
              Text(
                text = category,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp
              )
            }
          }
        }
      }
    }

    // 3-Column Grid of Dramas
    val chunked = filteredDramas.chunked(3)
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
            isFavorite = currentProfile.savedDramaIds.contains(drama.id),
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
}
