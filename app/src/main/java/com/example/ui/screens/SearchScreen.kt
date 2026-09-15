package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
  dramas: List<Drama>,
  currentProfile: UserProfile,
  onDramaClick: (Drama) -> Unit,
  onFavoriteClick: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedFilter by remember { mutableStateOf("ALL") }

  val trendingKeywords = listOf(
    "My Off-limits Neighbor",
    "The Bastard Is the Chosen King",
    "My Flash-Marriage Husband is a Golden Durian",
    "The True Crown Luna",
    "Bully Me? I Run the Mob!",
    "Reborn for Revenge The Wolfless King",
    "Twelve Years of Lies: The Gymnast's Rebirth",
    "One Test Report Shattered Her Billionaire Dream",
    "Blood Awakening Forsaken Wolf Heir",
    "Recommend"
  )

  val filteredDramas = dramas.filter { drama ->
    val matchesQuery = searchQuery.isBlank() ||
      drama.title.contains(searchQuery, ignoreCase = true) ||
      drama.genre.contains(searchQuery, ignoreCase = true) ||
      drama.tags.any { it.contains(searchQuery, ignoreCase = true) } ||
      drama.description.contains(searchQuery, ignoreCase = true)

    val matchesFilter = when (selectedFilter) {
      "NEW" -> drama.isNewRelease
      "Female" -> drama.genre.contains("Romance", ignoreCase = true) || drama.tags.any { it.contains("MARRIAGE", ignoreCase = true) || it.contains("QUEEN", ignoreCase = true) }
      "Male" -> drama.genre.contains("Billionaire", ignoreCase = true) || drama.genre.contains("Fantasy", ignoreCase = true) || drama.tags.any { it.contains("ARCHMAGE", ignoreCase = true) || it.contains("KING", ignoreCase = true) }
      else -> true
    }
    matchesQuery && matchesFilter
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(DramaBlack),
    contentPadding = PaddingValues(bottom = 90.dp)
  ) {
    // Search Screen Header
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 16.dp, end = 16.dp, top = 28.dp, bottom = 12.dp)
      ) {
        Text(
          text = "SEARCH",
          color = Color.White,
          fontSize = 34.sp,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = 1.sp
        )

        Text(
          text = "Titles, genres, moods",
          color = Color.White.copy(alpha = 0.6f),
          fontSize = 13.sp,
          modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
        )

        // Search Input Bar
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = {
            Text(
              text = "Titles, genres, moods...",
              color = Color.White.copy(alpha = 0.4f),
              fontSize = 14.sp
            )
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search",
              tint = Color.White.copy(alpha = 0.6f)
            )
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(
                  imageVector = Icons.Default.Clear,
                  contentDescription = "Clear",
                  tint = Color.White.copy(alpha = 0.6f)
                )
              }
            }
          },
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = DramaRed,
            unfocusedBorderColor = DramaBorder,
            focusedContainerColor = DramaCardBg,
            unfocusedContainerColor = DramaCardBg,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
          ),
          shape = RoundedCornerShape(8.dp),
          singleLine = true,
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_search_drama")
        )
      }
    }

    // If search is empty, display Popular Right Now chips (Photo 7)
    if (searchQuery.isBlank()) {
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
          Text(
            text = "TRENDING SEARCHES",
            color = DramaRed,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp
          )

          Text(
            text = "Popular right now",
            color = Color.White,
            fontFamily = FontFamily.Serif,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            modifier = Modifier.padding(top = 2.dp, bottom = 14.dp)
          )

          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            trendingKeywords.forEach { keyword ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .border(1.dp, DramaBorder, RoundedCornerShape(4.dp))
                  .background(DramaCardBg)
                  .clickable { searchQuery = keyword }
                  .padding(horizontal = 12.dp, vertical = 8.dp)
              ) {
                Text(
                  text = keyword,
                  color = Color.White.copy(alpha = 0.85f),
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(20.dp))

          // Filter tags: NEW, Female, Male
          Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            listOf("ALL", "NEW", "Female", "Male").forEach { filter ->
              val isSel = selectedFilter == filter
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(4.dp))
                  .border(
                    1.dp,
                    if (isSel) DramaRed else DramaBorder,
                    RoundedCornerShape(4.dp)
                  )
                  .background(if (isSel) DramaSurfaceVariant else DramaCardBg)
                  .clickable { selectedFilter = filter }
                  .padding(horizontal = 14.dp, vertical = 6.dp)
              ) {
                Text(
                  text = filter,
                  color = if (isSel) Color.White else Color.White.copy(alpha = 0.7f),
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }
    }

    // Results Header
    item {
      Text(
        text = if (searchQuery.isNotBlank()) "Results for \"$searchQuery\" (${filteredDramas.size})" else "Discover Dramas",
        color = Color.White,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
      )
    }

    // 3-column Grid of Dramas
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
