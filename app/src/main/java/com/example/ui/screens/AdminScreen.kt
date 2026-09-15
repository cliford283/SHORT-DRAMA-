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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.AdConfig
import com.example.data.Drama
import com.example.ui.theme.DramaBlack
import com.example.ui.theme.DramaBorder
import com.example.ui.theme.DramaCardBg
import com.example.ui.theme.DramaRed
import com.example.ui.theme.DramaSurfaceVariant

@Composable
fun AdminScreen(
  dramas: List<Drama>,
  adConfig: AdConfig,
  onBackClick: () -> Unit,
  onAddDrama: (title: String, genre: String, desc: String, tags: List<String>, count: Int, coverUrl: String?, streamUrl: String?) -> Unit,
  onDeleteDrama: (String) -> Unit,
  onAddEpisode: (dramaId: String, title: String, videoUrl: String) -> Unit,
  onDeleteEpisode: (dramaId: String, episodeId: String) -> Unit,
  onSaveAdConfig: (AdConfig) -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableIntStateOf(0) }

  // New Drama Form State
  var newTitle by remember { mutableStateOf("") }
  var newGenre by remember { mutableStateOf("Mafia Romance") }
  var newDesc by remember { mutableStateOf("") }
  var newTags by remember { mutableStateOf("EXCLUSIVE, REVENGE") }
  var newEpisodeCount by remember { mutableStateOf("40") }
  var newCoverUrl by remember { mutableStateOf("") }
  var newStreamUrl by remember { mutableStateOf("") }
  var dramaAddedMessage by remember { mutableStateOf(false) }

  // Ad Configuration State
  var isAdSenseEnabled by remember { mutableStateOf(adConfig.isAdSenseEnabled) }
  var publisherId by remember { mutableStateOf(adConfig.adSensePublisherId) }
  var bannerSlotId by remember { mutableStateOf(adConfig.bannerSlotId) }
  var isPreRollEnabled by remember { mutableStateOf(adConfig.isPreRollEnabled) }
  var isMidRollEnabled by remember { mutableStateOf(adConfig.isMidRollEnabled) }
  var isBannerEnabled by remember { mutableStateOf(adConfig.isBannerEnabled) }
  var preRollDuration by remember { mutableStateOf(adConfig.preRollDurationSec.toString()) }
  var preRollSkip by remember { mutableStateOf(adConfig.preRollSkipSec.toString()) }
  var preRollTitle by remember { mutableStateOf(adConfig.preRollAdTitle) }
  var preRollDesc by remember { mutableStateOf(adConfig.preRollAdDescription) }
  var preRollVideoUrl by remember { mutableStateOf(adConfig.preRollAdMediaUrl) }
  var preRollClickUrl by remember { mutableStateOf(adConfig.preRollClickUrl) }
  var bannerTitle by remember { mutableStateOf(adConfig.bannerAdTitle) }
  var bannerDesc by remember { mutableStateOf(adConfig.bannerAdDescription) }
  var bannerActionUrl by remember { mutableStateOf(adConfig.bannerAdActionUrl) }
  var adSavedMessage by remember { mutableStateOf(false) }

  // Episode Add Form State
  var selectedDramaForEpisode by remember { mutableStateOf<Drama?>(null) }
  var epTitle by remember { mutableStateOf("") }
  var epStreamUrl by remember { mutableStateOf("") }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DramaBlack)
  ) {
    // Top Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      IconButton(
        onClick = onBackClick,
        modifier = Modifier.testTag("btn_admin_back")
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Filled.ArrowBack,
          contentDescription = "Back",
          tint = Color.White
        )
      }

      Spacer(modifier = Modifier.width(8.dp))

      Column {
        Text(
          text = "ADMIN CONTROL PANEL",
          color = Color.White,
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "Manage Dramas, Stream URLs & AdSense Ads",
          color = Color.White.copy(alpha = 0.6f),
          fontSize = 11.sp
        )
      }
    }

    // Tab Navigation
    TabRow(
      selectedTabIndex = selectedTab,
      containerColor = DramaCardBg,
      contentColor = Color.White,
      indicator = { tabPositions ->
        TabRowDefaults.SecondaryIndicator(
          modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
          color = DramaRed
        )
      }
    ) {
      Tab(
        selected = selectedTab == 0,
        onClick = { selectedTab = 0 },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Movie, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Videos & Dramas", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      )
      Tab(
        selected = selectedTab == 1,
        onClick = { selectedTab = 1 },
        text = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("AdSense Ads", fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      )
    }

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      contentPadding = PaddingValues(vertical = 16.dp)
    ) {
      if (selectedTab == 0) {
        // Tab 0: Dramas & Videos
        item {
          Text(
            text = "ADD NEW DRAMA / VIDEO SERIES",
            color = DramaRed,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
          )

          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(DramaCardBg)
              .border(1.dp, DramaBorder, RoundedCornerShape(8.dp))
              .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            AdminTextField(value = newTitle, onValueChange = { newTitle = it }, label = "Drama Title *")
            AdminTextField(value = newGenre, onValueChange = { newGenre = it }, label = "Genre (e.g. Mafia Romance, Werewolf, Billionaire)")
            AdminTextField(value = newDesc, onValueChange = { newDesc = it }, label = "Description / Storyline", singleLine = false)
            AdminTextField(value = newTags, onValueChange = { newTags = it }, label = "Tags (comma separated)")
            AdminTextField(value = newEpisodeCount, onValueChange = { newEpisodeCount = it }, label = "Episodes Count")
            AdminTextField(value = newCoverUrl, onValueChange = { newCoverUrl = it }, label = "Custom Cover Poster URL (optional)")
            AdminTextField(value = newStreamUrl, onValueChange = { newStreamUrl = it }, label = "Stream Video URL (MP4 / HLS)")

            Button(
              onClick = {
                if (newTitle.isNotBlank()) {
                  val count = newEpisodeCount.toIntOrNull() ?: 40
                  val tagsList = newTags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                  onAddDrama(
                    newTitle,
                    newGenre,
                    newDesc.ifBlank { "Exciting new short drama series." },
                    tagsList,
                    count,
                    newCoverUrl.ifBlank { null },
                    newStreamUrl.ifBlank { null }
                  )
                  newTitle = ""
                  newDesc = ""
                  dramaAddedMessage = true
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = DramaRed),
              shape = RoundedCornerShape(6.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("btn_publish_drama")
            ) {
              Icon(imageVector = Icons.Default.Add, contentDescription = null)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Publish Drama Series", fontWeight = FontWeight.Bold)
            }

            if (dramaAddedMessage) {
              Text(
                text = "✓ Drama successfully created and added to catalog!",
                color = Color(0xFF4CAF50),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }

        item {
          Spacer(modifier = Modifier.height(24.dp))
          Text(
            text = "EXISTING DRAMAS (${dramas.size})",
            color = DramaRed,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
          )
        }

        items(dramas) { drama ->
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 6.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(DramaCardBg)
              .border(1.dp, DramaBorder, RoundedCornerShape(8.dp))
              .padding(12.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = drama.title,
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp
                )
                Text(
                  text = "${drama.genre} • ${drama.episodesCount} episodes",
                  color = Color.White.copy(alpha = 0.6f),
                  fontSize = 11.sp
                )
              }

              Row {
                IconButton(
                  onClick = { selectedDramaForEpisode = if (selectedDramaForEpisode?.id == drama.id) null else drama }
                ) {
                  Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Episode",
                    tint = Color.White
                  )
                }

                IconButton(
                  onClick = { onDeleteDrama(drama.id) },
                  modifier = Modifier.testTag("btn_delete_drama_${drama.id}")
                ) {
                  Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Drama",
                    tint = DramaRed
                  )
                }
              }
            }

            // Episode Add Sub-form
            if (selectedDramaForEpisode?.id == drama.id) {
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(top = 10.dp)
                  .background(DramaSurfaceVariant, RoundedCornerShape(6.dp))
                  .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Text(
                  text = "Add Episode to ${drama.title}",
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
                AdminTextField(value = epTitle, onValueChange = { epTitle = it }, label = "Episode Title (e.g. Episode ${drama.episodes.size + 1})")
                AdminTextField(value = epStreamUrl, onValueChange = { epStreamUrl = it }, label = "Episode Video Stream URL (MP4 / HLS)")

                Button(
                  onClick = {
                    if (epStreamUrl.isNotBlank()) {
                      onAddEpisode(drama.id, epTitle, epStreamUrl)
                      epTitle = ""
                      epStreamUrl = ""
                      selectedDramaForEpisode = null
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = DramaRed),
                  shape = RoundedCornerShape(4.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text("Add Episode")
                }
              }
            }
          }
        }
      } else {
        // Tab 1: AdSense & Ad Manager
        item {
          Text(
            text = "ADSENSE & CUSTOM AD SYSTEM",
            color = DramaRed,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
          )

          Column(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .background(DramaCardBg)
              .border(1.dp, DramaBorder, RoundedCornerShape(8.dp))
              .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
          ) {
            // AdSense Master Switch
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(text = "Enable Google AdSense", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = "Runs real AdSense banner tags and pre-roll slot ads", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
              }
              Switch(
                checked = isAdSenseEnabled,
                onCheckedChange = { isAdSenseEnabled = it },
                colors = SwitchDefaults.colors(checkedTrackColor = DramaRed)
              )
            }

            AdminTextField(value = publisherId, onValueChange = { publisherId = it }, label = "AdSense Publisher ID (ca-pub-XXXXXXXX)")
            AdminTextField(value = bannerSlotId, onValueChange = { bannerSlotId = it }, label = "Banner Ad Slot ID")

            Spacer(modifier = Modifier.height(6.dp))

            // Pre-Roll Ad Switch
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(text = "Pre-Roll Video Ads", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = "Plays sponsored ad before drama episode starts with countdown", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
              }
              Switch(
                checked = isPreRollEnabled,
                onCheckedChange = { isPreRollEnabled = it },
                colors = SwitchDefaults.colors(checkedTrackColor = DramaRed)
              )
            }

            AdminTextField(value = preRollDuration, onValueChange = { preRollDuration = it }, label = "Pre-Roll Ad Duration (seconds)")
            AdminTextField(value = preRollSkip, onValueChange = { preRollSkip = it }, label = "Skip Button Appears After (seconds)")
            AdminTextField(value = preRollTitle, onValueChange = { preRollTitle = it }, label = "Pre-Roll Ad Headline")
            AdminTextField(value = preRollDesc, onValueChange = { preRollDesc = it }, label = "Pre-Roll Ad Description")
            AdminTextField(value = preRollClickUrl, onValueChange = { preRollClickUrl = it }, label = "Pre-Roll Advertiser Click URL")

            Spacer(modifier = Modifier.height(6.dp))

            // Banner Ad Switch
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(text = "Feed Banner Ads", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(text = "Displays sponsor cards on Home and Browse feeds", color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
              }
              Switch(
                checked = isBannerEnabled,
                onCheckedChange = { isBannerEnabled = it },
                colors = SwitchDefaults.colors(checkedTrackColor = DramaRed)
              )
            }

            AdminTextField(value = bannerTitle, onValueChange = { bannerTitle = it }, label = "Banner Ad Title")
            AdminTextField(value = bannerDesc, onValueChange = { bannerDesc = it }, label = "Banner Ad Subtitle / Copy")
            AdminTextField(value = bannerActionUrl, onValueChange = { bannerActionUrl = it }, label = "Banner Click Destination URL")

            Button(
              onClick = {
                val newCfg = adConfig.copy(
                  isAdSenseEnabled = isAdSenseEnabled,
                  adSensePublisherId = publisherId,
                  bannerSlotId = bannerSlotId,
                  isPreRollEnabled = isPreRollEnabled,
                  isMidRollEnabled = isMidRollEnabled,
                  isBannerEnabled = isBannerEnabled,
                  preRollDurationSec = preRollDuration.toIntOrNull() ?: 5,
                  preRollSkipSec = preRollSkip.toIntOrNull() ?: 3,
                  preRollAdTitle = preRollTitle,
                  preRollAdDescription = preRollDesc,
                  preRollClickUrl = preRollClickUrl,
                  bannerAdTitle = bannerTitle,
                  bannerAdDescription = bannerDesc,
                  bannerAdActionUrl = bannerActionUrl
                )
                onSaveAdConfig(newCfg)
                adSavedMessage = true
              },
              colors = ButtonDefaults.buttonColors(containerColor = DramaRed),
              shape = RoundedCornerShape(6.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("btn_save_ad_config")
            ) {
              Icon(imageVector = Icons.Default.Save, contentDescription = null)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Save & Apply Ad System", fontWeight = FontWeight.Bold)
            }

            if (adSavedMessage) {
              Text(
                text = "✓ Ad settings updated! Changes are live across all video players and feeds.",
                color = Color(0xFF4CAF50),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun AdminTextField(
  value: String,
  onValueChange: (String) -> Unit,
  label: String,
  singleLine: Boolean = true,
  modifier: Modifier = Modifier
) {
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    label = { Text(label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.6f)) },
    colors = OutlinedTextFieldDefaults.colors(
      focusedBorderColor = DramaRed,
      unfocusedBorderColor = DramaBorder,
      focusedTextColor = Color.White,
      unfocusedTextColor = Color.White,
      focusedContainerColor = DramaBlack,
      unfocusedContainerColor = DramaBlack
    ),
    singleLine = singleLine,
    shape = RoundedCornerShape(6.dp),
    modifier = modifier.fillMaxWidth()
  )
}
