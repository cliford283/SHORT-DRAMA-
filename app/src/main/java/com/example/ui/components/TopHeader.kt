package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfile
import com.example.ui.theme.DramaBlack
import com.example.ui.theme.DramaRed
import com.example.ui.theme.DramaSurfaceVariant

@Composable
fun TopHeader(
  currentProfile: UserProfile,
  onProfileClick: () -> Unit,
  onAdminClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 12.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Brand header matching screenshots:
    // Red vertical bar with "FREE" and "SHORT DRAMA"
    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Red vertical accent bar
      Box(
        modifier = Modifier
          .width(4.dp)
          .height(28.dp)
          .clip(RoundedCornerShape(2.dp))
          .background(DramaRed)
      )

      Spacer(modifier = Modifier.width(8.dp))

      Column {
        Text(
          text = "FREE",
          color = Color.White.copy(alpha = 0.85f),
          fontSize = 10.sp,
          fontWeight = FontWeight.Bold,
          letterSpacing = 2.sp
        )
        Text(
          text = "SHORT DRAMA",
          color = Color.White,
          fontSize = 15.sp,
          fontWeight = FontWeight.ExtraBold,
          letterSpacing = 1.sp
        )
      }
    }

    // Right action icons: Admin Dashboard + User Profile switcher
    Row(verticalAlignment = Alignment.CenterVertically) {
      // Admin Control button
      IconButton(
        onClick = onAdminClick,
        modifier = Modifier
          .size(36.dp)
          .testTag("btn_admin_control")
      ) {
        Icon(
          imageVector = Icons.Default.AdminPanelSettings,
          contentDescription = "Admin Control & Ad Manager",
          tint = if (currentProfile.isAdmin) DramaRed else Color.White.copy(alpha = 0.7f),
          modifier = Modifier.size(22.dp)
        )
      }

      Spacer(modifier = Modifier.width(4.dp))

      // User Profile Avatar
      Box(
        modifier = Modifier
          .size(34.dp)
          .clip(CircleShape)
          .background(Color(currentProfile.avatarColorHex))
          .clickable { onProfileClick() }
          .testTag("btn_user_profile"),
        contentAlignment = Alignment.Center
      ) {
        Text(
          text = currentProfile.name.take(1).uppercase(),
          color = Color.White,
          fontSize = 14.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }
  }
}
