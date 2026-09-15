package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DramaNavBg
import com.example.ui.theme.DramaRed
import com.example.ui.theme.DramaTextMuted
import com.example.ui.theme.DramaTextPrimary

enum class DramaNavDestination(val label: String) {
  HOME("Home"),
  SEARCH("Search"),
  STREAM_REEL("Play"),
  BROWSE("Browse"),
  MY_LIST("List")
}

@Composable
fun DramaBottomNavigation(
  currentDestination: DramaNavDestination,
  onNavigate: (DramaNavDestination) -> Unit,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 12.dp),
    contentAlignment = Alignment.Center
  ) {
    // Floating Pill Container
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(64.dp)
        .shadow(16.dp, RoundedCornerShape(32.dp), spotColor = Color.Black)
        .clip(RoundedCornerShape(32.dp))
        .background(DramaNavBg)
        .padding(horizontal = 8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().height(64.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Home
        NavItem(
          icon = if (currentDestination == DramaNavDestination.HOME) Icons.Filled.Home else Icons.Outlined.Home,
          label = "Home",
          selected = currentDestination == DramaNavDestination.HOME,
          testTag = "nav_home",
          onClick = { onNavigate(DramaNavDestination.HOME) }
        )

        // Search
        NavItem(
          icon = if (currentDestination == DramaNavDestination.SEARCH) Icons.Filled.Search else Icons.Outlined.Search,
          label = "Search",
          selected = currentDestination == DramaNavDestination.SEARCH,
          testTag = "nav_search",
          onClick = { onNavigate(DramaNavDestination.SEARCH) }
        )

        // Space for Center Play Button
        Box(modifier = Modifier.size(52.dp))

        // Browse
        NavItem(
          icon = if (currentDestination == DramaNavDestination.BROWSE) Icons.Filled.Explore else Icons.Outlined.Explore,
          label = "Browse",
          selected = currentDestination == DramaNavDestination.BROWSE,
          testTag = "nav_browse",
          onClick = { onNavigate(DramaNavDestination.BROWSE) }
        )

        // List
        NavItem(
          icon = if (currentDestination == DramaNavDestination.MY_LIST) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
          label = "List",
          selected = currentDestination == DramaNavDestination.MY_LIST,
          testTag = "nav_list",
          onClick = { onNavigate(DramaNavDestination.MY_LIST) }
        )
      }
    }

    // Center Big Red Play Button
    Box(
      modifier = Modifier
        .offset(y = (-4).dp)
        .size(54.dp)
        .shadow(10.dp, CircleShape, spotColor = DramaRed)
        .clip(CircleShape)
        .background(Color.White)
        .clickable(
          interactionSource = remember { MutableInteractionSource() },
          indication = null
        ) { onNavigate(DramaNavDestination.STREAM_REEL) }
        .testTag("nav_center_play"),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = Icons.Filled.PlayArrow,
        contentDescription = "Watch Short Drama Reel",
        tint = Color.Black,
        modifier = Modifier.size(30.dp)
      )
    }
  }
}

@Composable
private fun NavItem(
  icon: ImageVector,
  label: String,
  selected: Boolean,
  testTag: String,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
    modifier = Modifier
      .clip(RoundedCornerShape(16.dp))
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null
      ) { onClick() }
      .padding(horizontal = 8.dp, vertical = 6.dp)
      .testTag(testTag)
  ) {
    Icon(
      imageVector = icon,
      contentDescription = label,
      tint = if (selected) DramaTextPrimary else DramaTextMuted,
      modifier = Modifier.size(22.dp)
    )
    Text(
      text = label,
      color = if (selected) DramaTextPrimary else DramaTextMuted,
      fontSize = 10.sp,
      modifier = Modifier.padding(top = 2.dp)
    )
  }
}
