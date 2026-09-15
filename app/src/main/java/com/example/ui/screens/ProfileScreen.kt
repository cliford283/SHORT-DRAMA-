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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserProfile
import com.example.ui.theme.DramaBlack
import com.example.ui.theme.DramaBorder
import com.example.ui.theme.DramaCardBg
import com.example.ui.theme.DramaRed
import com.example.ui.theme.DramaSurfaceVariant

@Composable
fun ProfileScreen(
  currentProfile: UserProfile,
  allProfiles: List<UserProfile>,
  onSelectProfile: (String) -> Unit,
  onCreateProfile: (name: String, email: String, isAdmin: Boolean) -> Unit,
  onBackClick: () -> Unit,
  onOpenAdmin: () -> Unit,
  onSignOut: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  var showCreateForm by remember { mutableStateOf(false) }
  var newName by remember { mutableStateOf("") }
  var newEmail by remember { mutableStateOf("") }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(DramaBlack),
    contentPadding = PaddingValues(16.dp)
  ) {
    // Top Bar
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(
          onClick = onBackClick,
          modifier = Modifier.testTag("btn_profile_back")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = Color.White
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
          text = "VISITOR PROFILES & PRIVACY",
          color = Color.White,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )
      }
    }

    // Active Profile Card
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 20.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(DramaCardBg)
          .border(1.dp, DramaBorder, RoundedCornerShape(12.dp))
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Box(
          modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(Color(currentProfile.avatarColorHex)),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = currentProfile.name.take(1).uppercase(),
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
          )
        }

        Text(
          text = currentProfile.name,
          color = Color.White,
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(top = 12.dp)
        )

        Text(
          text = currentProfile.email.ifBlank { "Private Viewer Session" },
          color = Color.White.copy(alpha = 0.6f),
          fontSize = 12.sp,
          modifier = Modifier.padding(top = 2.dp)
        )

        // Private Stats Pill Row
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          StatItem(label = "Saved", value = currentProfile.savedDramaIds.size.toString())
          StatItem(label = "Downloads", value = currentProfile.downloadedDramaIds.size.toString())
          StatItem(label = "History", value = "${currentProfile.watchHistory.size} eps")
        }

        if (currentProfile.isAdmin) {
          Button(
            onClick = onOpenAdmin,
            colors = ButtonDefaults.buttonColors(containerColor = DramaRed),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 16.dp)
              .testTag("btn_admin_portal")
          ) {
            Icon(imageVector = Icons.Default.Security, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("Open Admin & AdSense Control", fontWeight = FontWeight.Bold)
          }
        }

        androidx.compose.material3.OutlinedButton(
          onClick = onSignOut,
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF5252)),
          shape = RoundedCornerShape(6.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .testTag("btn_sign_out")
        ) {
          Text("Sign Out of Session", fontWeight = FontWeight.Bold)
        }
      }
    }

    // Switch Profile Section
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 28.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "SWITCH VISITOR PROFILE",
          color = DramaRed,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold
        )

        Text(
          text = "+ Add Profile",
          color = Color.White,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.clickable { showCreateForm = !showCreateForm }
        )
      }
    }

    // New Profile Creation Form
    if (showCreateForm) {
      item {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(DramaSurfaceVariant)
            .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = "Create Isolated Viewer Profile",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          )

          OutlinedTextField(
            value = newName,
            onValueChange = { newName = it },
            placeholder = { Text("Profile Name (e.g. Alex, Mom, Roommate)", fontSize = 12.sp) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = DramaRed,
              unfocusedBorderColor = DramaBorder,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          OutlinedTextField(
            value = newEmail,
            onValueChange = { newEmail = it },
            placeholder = { Text("Email (Optional for cloud sync)", fontSize = 12.sp) },
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = DramaRed,
              unfocusedBorderColor = DramaBorder,
              focusedTextColor = Color.White,
              unfocusedTextColor = Color.White
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )

          Button(
            onClick = {
              if (newName.isNotBlank()) {
                onCreateProfile(newName, newEmail, false)
                newName = ""
                newEmail = ""
                showCreateForm = false
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = DramaRed),
            shape = RoundedCornerShape(4.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Create & Switch to Profile", fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Profile list
    items(allProfiles) { profile ->
      val isCurrent = profile.id == currentProfile.id
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 4.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(if (isCurrent) DramaSurfaceVariant else DramaCardBg)
          .border(
            1.dp,
            if (isCurrent) DramaRed else DramaBorder,
            RoundedCornerShape(8.dp)
          )
          .clickable { onSelectProfile(profile.id) }
          .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(Color(profile.avatarColorHex)),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = profile.name.take(1).uppercase(),
              color = Color.White,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.width(12.dp))

          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = profile.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
              if (profile.isAdmin) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                  modifier = Modifier
                    .background(DramaRed, RoundedCornerShape(3.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                  Text(text = "ADMIN", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
            Text(
              text = "${profile.savedDramaIds.size} saved • ${profile.watchHistory.size} history items (Private)",
              color = Color.White.copy(alpha = 0.5f),
              fontSize = 11.sp
            )
          }
        }

        if (isCurrent) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Active",
            tint = DramaRed,
            modifier = Modifier.size(20.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun StatItem(label: String, value: String) {
  Column(horizontalAlignment = Alignment.CenterHorizontally) {
    Text(
      text = value,
      color = Color.White,
      fontSize = 18.sp,
      fontWeight = FontWeight.Bold
    )
    Text(
      text = label,
      color = Color.White.copy(alpha = 0.6f),
      fontSize = 11.sp
    )
  }
}
