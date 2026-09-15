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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.AdConfig
import com.example.data.Drama
import com.example.data.UserProfile
import com.example.ui.components.AdBannerCard
import com.example.ui.components.DramaPosterCard
import com.example.ui.components.TopHeader
import com.example.ui.theme.DramaBlack
import com.example.ui.theme.DramaCardBg
import com.example.ui.theme.DramaRed
import com.example.ui.theme.DramaSurfaceVariant
import com.example.ui.theme.DramaTextMuted

import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import com.example.data.firebase.FirestoreDramaRepository
import com.example.ui.components.DramaCardSkeleton
import com.example.ui.components.DramaGridSkeleton
import com.example.ui.components.DramaRowSkeleton
import com.example.ui.components.FirestoreLoadingSpinner
import com.example.ui.components.shimmerBrush

@Composable
fun HomeScreen(
  dramas: List<Drama>,
  currentProfile: UserProfile,
  adConfig: AdConfig,
  watchlistDramaIds: Set<String> = emptySet(),
  onDramaClick: (Drama) -> Unit,
  onPlayDrama: (Drama) -> Unit,
  onFavoriteClick: (String) -> Unit,
  onToggleWatchlist: ((Drama) -> Unit)? = null,
  onProfileClick: () -> Unit,
  onAdminClick: () -> Unit,
  modifier: Modifier = Modifier,
  isLoading: Boolean = false
) {
  var searchQuery by remember { mutableStateOf("") }
  var selectedTrendingGenreIndex by remember { mutableIntStateOf(0) }
  val trendingGenres = listOf("All", "Romance", "Thriller", "Werewolf", "Billionaire", "Revenge", "Fantasy")
  val selectedGenre = trendingGenres[selectedTrendingGenreIndex]

  // Real-time Firestore Search state
  var firestoreSearchResults by remember { mutableStateOf<List<Drama>>(emptyList()) }
  var isSearchingFirestore by remember { mutableStateOf(false) }

  // Real-time query to Firestore whenever searchQuery changes
  LaunchedEffect(searchQuery) {
    val query = searchQuery.trim()
    if (query.isNotEmpty()) {
      isSearchingFirestore = true
      val reg = FirestoreDramaRepository.searchDramasRealtime(query) { results ->
        firestoreSearchResults = results
        isSearchingFirestore = false
      }
    } else {
      firestoreSearchResults = emptyList()
      isSearchingFirestore = false
    }
  }

  val effectiveSearchResults = if (firestoreSearchResults.isNotEmpty()) {
    firestoreSearchResults
  } else if (searchQuery.isNotBlank()) {
    dramas.filter {
      it.title.contains(searchQuery, ignoreCase = true) ||
      it.genre.contains(searchQuery, ignoreCase = true) ||
      it.description.contains(searchQuery, ignoreCase = true) ||
      it.tags.any { tag -> tag.contains(searchQuery, ignoreCase = true) }
    }
  } else {
    emptyList()
  }

  // Filter trending dramas by TabRow selected genre
  val filteredTrendingDramas = if (selectedGenre == "All") {
    dramas.filter { it.isTrending }
  } else {
    val genreFiltered = dramas.filter { drama ->
      drama.isTrending && (
        drama.genre.contains(selectedGenre, ignoreCase = true) ||
        drama.tags.any { it.contains(selectedGenre, ignoreCase = true) } ||
        when (selectedGenre) {
          "Romance" -> drama.genre.contains("Marriage", ignoreCase = true) || drama.genre.contains("Romance", ignoreCase = true)
          "Thriller" -> drama.genre.contains("Mafia", ignoreCase = true) || drama.genre.contains("Revenge", ignoreCase = true)
          "Werewolf" -> drama.genre.contains("Lycan", ignoreCase = true) || drama.genre.contains("Alpha", ignoreCase = true)
          "Billionaire" -> drama.genre.contains("CEO", ignoreCase = true) || drama.genre.contains("Heir", ignoreCase = true)
          "Revenge" -> drama.tags.any { it.contains("REVENGE", ignoreCase = true) } || drama.genre.contains("Revenge", ignoreCase = true)
          "Fantasy" -> drama.genre.contains("Magic", ignoreCase = true) || drama.genre.contains("Martial", ignoreCase = true)
          else -> true
        }
      )
    }
    if (genreFiltered.isEmpty()) {
      dramas.filter { drama ->
        drama.genre.contains(selectedGenre, ignoreCase = true) ||
        drama.tags.any { it.contains(selectedGenre, ignoreCase = true) }
      }
    } else {
      genreFiltered
    }
  }

  val featuredDramas = dramas.take(4)
  val resumeDramas = dramas.filter { it.isResume }
  val newReleases = dramas.filter { it.isNewRelease }
  val forYouDramas = dramas.filter { it.isForYou }

  val pagerState = rememberPagerState(pageCount = { featuredDramas.size })

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(DramaBlack),
    contentPadding = PaddingValues(bottom = 90.dp)
  ) {
    // Header
    item {
      TopHeader(
        currentProfile = currentProfile,
        onProfileClick = onProfileClick,
        onAdminClick = onAdminClick
      )
    }

    // Search bar component using TextField
    item {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp)
      ) {
        TextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = {
            Text(
              text = "Search dramas by title or description...",
              color = DramaTextMuted,
              fontSize = 13.sp
            )
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = "Search",
              tint = DramaRed,
              modifier = Modifier.size(20.dp)
            )
          },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { searchQuery = "" }) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Clear Search",
                  tint = Color.White.copy(alpha = 0.7f),
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(8.dp),
          colors = TextFieldDefaults.colors(
            focusedContainerColor = DramaCardBg,
            unfocusedContainerColor = DramaCardBg,
            disabledContainerColor = DramaCardBg,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedIndicatorColor = DramaRed,
            unfocusedIndicatorColor = Color.Transparent
          ),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("search_bar_input")
        )
      }
    }

    // Loading Indicator from Firestore
    if (isLoading) {
      item {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
          contentAlignment = Alignment.Center
        ) {
          FirestoreLoadingSpinner("Syncing dramas from Firestore...")
        }
      }
    }

    // Dynamic Search Results (if user is actively searching)
    if (searchQuery.isNotBlank()) {
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "FIRESTORE SEARCH (${effectiveSearchResults.size})",
                color = DramaRed,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp
              )
              if (isSearchingFirestore) {
                Spacer(modifier = Modifier.width(8.dp))
                androidx.compose.material3.CircularProgressIndicator(
                  modifier = Modifier.size(12.dp),
                  strokeWidth = 2.dp,
                  color = DramaRed
                )
              }
            }
            Text(
              text = "Live results for \"$searchQuery\"",
              color = DramaTextMuted,
              fontSize = 11.sp
            )
          }

          if (isSearchingFirestore && effectiveSearchResults.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
              contentAlignment = Alignment.Center
            ) {
              FirestoreLoadingSpinner("Querying Firestore catalog...")
            }
          } else if (!isSearchingFirestore && effectiveSearchResults.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp)
                .background(DramaCardBg, RoundedCornerShape(8.dp))
                .padding(16.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "No dramas found in Firestore matching \"$searchQuery\". Try searching by title or genres like 'Romance', 'Mafia', 'Billionaire'.",
                color = DramaTextMuted,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
              )
            }
          }
        }
      }

      if (isSearchingFirestore && effectiveSearchResults.isEmpty()) {
        item {
          DramaGridSkeleton(columns = 3, rowsCount = 2)
        }
      } else if (effectiveSearchResults.isNotEmpty()) {
        val chunkedSearch = effectiveSearchResults.chunked(3)
        items(chunkedSearch) { rowDramas ->
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            for (drama in rowDramas) {
              val isSaved = watchlistDramaIds.contains(drama.id) || currentProfile.savedDramaIds.contains(drama.id)
              DramaPosterCard(
                drama = drama,
                isFavorite = isSaved,
                onCardClick = { onDramaClick(drama) },
                onFavoriteClick = {
                  onToggleWatchlist?.invoke(drama) ?: onFavoriteClick(drama.id)
                },
                aspectRatio = 0.70f,
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

    // Material3 TabRow component at the top of the main screen to filter trending dramas by genre
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 4.dp, bottom = 8.dp)
      ) {
        ScrollableTabRow(
          selectedTabIndex = selectedTrendingGenreIndex,
          containerColor = DramaBlack,
          contentColor = Color.White,
          edgePadding = 16.dp,
          indicator = { tabPositions ->
            if (selectedTrendingGenreIndex < tabPositions.size) {
              TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTrendingGenreIndex]),
                color = DramaRed
              )
            }
          },
          divider = {
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
          }
        ) {
          trendingGenres.forEachIndexed { index, genre ->
            val isSelected = selectedTrendingGenreIndex == index
            Tab(
              selected = isSelected,
              onClick = { selectedTrendingGenreIndex = index },
              text = {
                Text(
                  text = genre.uppercase(),
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  fontSize = 12.sp,
                  letterSpacing = 1.sp,
                  color = if (isSelected) Color.White else Color.White.copy(alpha = 0.6f)
                )
              },
              modifier = Modifier.testTag("trending_tab_${genre.lowercase()}")
            )
          }
        }
      }
    }

    // Hero Cover Story Carousel
    if (featuredDramas.isNotEmpty()) {
      item {
        Column(modifier = Modifier.fillMaxWidth()) {
          HorizontalPager(
            state = pagerState,
            modifier = Modifier
              .fillMaxWidth()
              .height(440.dp)
          ) { page ->
            val drama = featuredDramas[page]
            val isFav = watchlistDramaIds.contains(drama.id) || currentProfile.savedDramaIds.contains(drama.id)

            Box(
              modifier = Modifier
                .fillMaxSize()
                .clickable { onDramaClick(drama) }
            ) {
              // Cover Artwork
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

              // Top episode badge & heart
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 16.dp, vertical = 8.dp)
                  .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                IconButton(
                  onClick = { onToggleWatchlist?.invoke(drama) ?: onFavoriteClick(drama.id) },
                  modifier = Modifier.size(36.dp)
                ) {
                  Icon(
                    imageVector = if (isFav) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFav) DramaRed else Color.White,
                    modifier = Modifier.size(20.dp)
                  )
                }

                Box(
                  modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                  Text(
                    text = "${drama.episodesCount} EP",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }

              // Gradient Overlay
              Box(
                modifier = Modifier
                  .fillMaxSize()
                  .background(
                    Brush.verticalGradient(
                      colors = listOf(
                        Color.Black.copy(alpha = 0.2f),
                        Color.Transparent,
                        DramaBlack.copy(alpha = 0.85f),
                        DramaBlack
                      ),
                      startY = 100f
                    )
                  )
              )

              // Bottom Overlay Details
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .align(Alignment.BottomCenter)
                  .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(
                  text = drama.coverStorySubtitle.ifBlank { "COVER STORY" },
                  color = DramaRed,
                  fontSize = 10.sp,
                  fontWeight = FontWeight.ExtraBold,
                  letterSpacing = 1.5.sp
                )

                Text(
                  text = drama.title,
                  color = Color.White,
                  fontFamily = FontFamily.Serif,
                  fontWeight = FontWeight.Bold,
                  fontSize = 26.sp,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                )

                // Action Buttons
                Row(
                  horizontalArrangement = Arrangement.Center,
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.fillMaxWidth()
                ) {
                  // Play Button (White rounded)
                  Button(
                    onClick = { onPlayDrama(drama) },
                    colors = ButtonDefaults.buttonColors(
                      containerColor = Color.White,
                      contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                      .height(38.dp)
                      .width(130.dp)
                      .testTag("btn_hero_play")
                  ) {
                    Icon(
                      imageVector = Icons.Filled.PlayArrow,
                      contentDescription = null,
                      modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = "Play",
                      fontWeight = FontWeight.Bold,
                      fontSize = 13.sp
                    )
                  }

                  Spacer(modifier = Modifier.width(12.dp))

                  // More Info Button (Dark outline)
                  OutlinedButton(
                    onClick = { onDramaClick(drama) },
                    colors = ButtonDefaults.outlinedButtonColors(
                      containerColor = Color.Black.copy(alpha = 0.6f),
                      contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier
                      .height(38.dp)
                      .width(130.dp)
                      .border(1.dp, Color.White.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                      .testTag("btn_hero_more_info")
                  ) {
                    Text(
                      text = "More info",
                      fontWeight = FontWeight.Bold,
                      fontSize = 13.sp
                    )
                  }
                }
              }
            }
          }

          // Carousel Indicators
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 8.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            repeat(featuredDramas.size) { index ->
              val isSelected = pagerState.currentPage == index
              Box(
                modifier = Modifier
                  .padding(horizontal = 3.dp)
                  .height(3.dp)
                  .width(if (isSelected) 18.dp else 6.dp)
                  .clip(CircleShape)
                  .background(if (isSelected) Color.White else Color.White.copy(alpha = 0.3f))
              )
            }
          }
        }
      }
    } else if (isLoading) {
      item {
        val brush = shimmerBrush()
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(400.dp)
            .background(brush)
        )
      }
    }

    // Optional AdSense Banner
    item {
      AdBannerCard(adConfig = adConfig)
    }

    // Section 1: RESUME Continue Watching
    if (resumeDramas.isNotEmpty()) {
      item {
        SectionHeader(tag = "RESUME", title = "Continue Watching")
      }
      item {
        LazyRow(
          contentPadding = PaddingValues(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(resumeDramas) { drama ->
            DramaPosterCard(
              drama = drama,
              isFavorite = currentProfile.savedDramaIds.contains(drama.id),
              onCardClick = { onPlayDrama(drama) },
              onFavoriteClick = { onFavoriteClick(drama.id) },
              showProgress = true,
              showDismiss = true,
              aspectRatio = 0.70f,
              modifier = Modifier.width(115.dp)
            )
          }
        }
      }
    }

    // Section 2: THIS WEEK Trending Now (Filtered by TabRow Genre)
    if (filteredTrendingDramas.isNotEmpty()) {
      item {
        SectionHeader(
          tag = "THIS WEEK" + if (selectedGenre != "All") " • ${selectedGenre.uppercase()}" else "",
          title = if (selectedGenre == "All") "Trending Now" else "$selectedGenre Trending"
        )
      }
      item {
        LazyRow(
          contentPadding = PaddingValues(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(filteredTrendingDramas) { drama ->
            val isSaved = watchlistDramaIds.contains(drama.id) || currentProfile.savedDramaIds.contains(drama.id)
            DramaPosterCard(
              drama = drama,
              isFavorite = isSaved,
              onCardClick = { onDramaClick(drama) },
              onFavoriteClick = {
                onToggleWatchlist?.invoke(drama) ?: onFavoriteClick(drama.id)
              },
              aspectRatio = 0.70f,
              modifier = Modifier.width(115.dp)
            )
          }
        }
      }
    }

    // Section 3: JUST DROPPED New Releases
    if (newReleases.isNotEmpty()) {
      item {
        SectionHeader(tag = "JUST DROPPED", title = "New Releases")
      }
      item {
        LazyRow(
          contentPadding = PaddingValues(horizontal = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          items(newReleases) { drama ->
            val isSaved = watchlistDramaIds.contains(drama.id) || currentProfile.savedDramaIds.contains(drama.id)
            DramaPosterCard(
              drama = drama,
              isFavorite = isSaved,
              onCardClick = { onDramaClick(drama) },
              onFavoriteClick = {
                onToggleWatchlist?.invoke(drama) ?: onFavoriteClick(drama.id)
              },
              aspectRatio = 0.70f,
              modifier = Modifier.width(115.dp)
            )
          }
        }
      }
    }

    // Section 4: THE EDIT For You (3-column grid)
    if (forYouDramas.isNotEmpty()) {
      item {
        SectionHeader(tag = "THE EDIT", title = "For You")
      }

      val chunked = forYouDramas.chunked(3)
      items(chunked) { rowDramas ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          for (drama in rowDramas) {
            val isSaved = watchlistDramaIds.contains(drama.id) || currentProfile.savedDramaIds.contains(drama.id)
            DramaPosterCard(
              drama = drama,
              isFavorite = isSaved,
              onCardClick = { onDramaClick(drama) },
              onFavoriteClick = {
                onToggleWatchlist?.invoke(drama) ?: onFavoriteClick(drama.id)
              },
              aspectRatio = 0.70f,
              modifier = Modifier.weight(1f)
            )
          }
          // Fill remaining columns if last row has fewer than 3 items
          repeat(3 - rowDramas.size) {
            Spacer(modifier = Modifier.weight(1f))
          }
        }
      }
    } else if (isLoading) {
      item {
        SectionHeader(tag = "THE EDIT", title = "For You")
      }
      item {
        DramaGridSkeleton(columns = 3, rowsCount = 3)
      }
    }
  }
}

@Composable
fun SectionHeader(tag: String, title: String, modifier: Modifier = Modifier) {
  Column(
    modifier = modifier
      .fillMaxWidth()
      .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp)
  ) {
    Text(
      text = tag,
      color = DramaRed,
      fontSize = 9.sp,
      fontWeight = FontWeight.ExtraBold,
      letterSpacing = 1.5.sp
    )
    Text(
      text = title,
      color = Color.White,
      fontFamily = FontFamily.Serif,
      fontWeight = FontWeight.Bold,
      fontSize = 20.sp,
      modifier = Modifier.padding(top = 1.dp)
    )
  }
}
