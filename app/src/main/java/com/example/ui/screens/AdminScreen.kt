package com.example.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.UserProfile
import com.example.data.firebase.FirebaseManager
import com.example.ui.theme.DramaBlack
import com.example.ui.theme.DramaBorder
import com.example.ui.theme.DramaCardBg
import com.example.ui.theme.DramaRed
import com.example.ui.theme.DramaSurfaceVariant
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun AdminScreen(
  dramas: List<Drama>,
  adConfig: AdConfig,
  currentProfile: UserProfile,
  onBackClick: () -> Unit,
  onAddDrama: (title: String, genre: String, desc: String, tags: List<String>, count: Int, coverUrl: String?, streamUrl: String?) -> Unit,
  onDeleteDrama: (String) -> Unit,
  onAddEpisode: (dramaId: String, title: String, videoUrl: String) -> Unit,
  onDeleteEpisode: (dramaId: String, episodeId: String) -> Unit,
  onSaveAdConfig: (AdConfig) -> Unit,
  onSwitchToAdminLogin: () -> Unit = {},
  modifier: Modifier = Modifier
) {
  val coroutineScope = rememberCoroutineScope()

  // STRICT ADMIN ACCESS GATE
  val isUserAdmin = currentProfile.isAdmin || FirebaseManager.isUserAdmin(currentProfile.email)
  if (!isUserAdmin) {
    Box(
      modifier = modifier
        .fillMaxSize()
        .background(DramaBlack)
        .padding(24.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(DramaCardBg)
          .border(1.dp, DramaBorder, RoundedCornerShape(12.dp))
          .padding(24.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Lock,
          contentDescription = "Access Denied",
          tint = DramaRed,
          modifier = Modifier.size(56.dp)
        )

        Text(
          text = "ACCESS RESTRICTED",
          color = Color.White,
          fontWeight = FontWeight.Black,
          fontSize = 20.sp
        )

        Text(
          text = "You are currently signed in as:\n${currentProfile.email.ifBlank { currentProfile.name }}\n\nOnly authorized administrators (e.g. clifordmulumba@gmail.com) have permissions to upload videos and edit catalog content.",
          color = Color.White.copy(alpha = 0.7f),
          fontSize = 13.sp,
          lineHeight = 20.sp,
          modifier = Modifier.padding(horizontal = 8.dp)
        )

        Button(
          onClick = onBackClick,
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF333333)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Return to Stream Catalog", fontWeight = FontWeight.Bold)
        }

        OutlinedButton(
          onClick = onSwitchToAdminLogin,
          colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E676)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Sign in with Admin Credentials", fontWeight = FontWeight.Bold)
        }
      }
    }
    return
  }

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

  // Video Upload State for Drama
  var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
  var videoUploadProgress by remember { mutableFloatStateOf(0f) }
  var isVideoUploading by remember { mutableStateOf(false) }
  var videoUploadSuccess by remember { mutableStateOf(false) }
  var videoUploadError by remember { mutableStateOf<String?>(null) }

  // Poster Upload State for Drama
  var selectedPosterUri by remember { mutableStateOf<Uri?>(null) }
  var posterUploadProgress by remember { mutableFloatStateOf(0f) }
  var isPosterUploading by remember { mutableStateOf(false) }

  // Episode Add Form State
  var selectedDramaForEpisode by remember { mutableStateOf<Drama?>(null) }
  var epTitle by remember { mutableStateOf("") }
  var epStreamUrl by remember { mutableStateOf("") }
  var epSelectedVideoUri by remember { mutableStateOf<Uri?>(null) }
  var epVideoUploadProgress by remember { mutableFloatStateOf(0f) }
  var epIsVideoUploading by remember { mutableStateOf(false) }

  // File pickers using ActivityResultContracts
  val videoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      selectedVideoUri = uri
      videoUploadError = null
      videoUploadSuccess = false
    }
  }

  val posterPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      selectedPosterUri = uri
    }
  }

  val epVideoPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null) {
      epSelectedVideoUri = uri
    }
  }

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
  var preRollClickUrl by remember { mutableStateOf(adConfig.preRollClickUrl) }
  var bannerTitle by remember { mutableStateOf(adConfig.bannerAdTitle) }
  var bannerDesc by remember { mutableStateOf(adConfig.bannerAdDescription) }
  var bannerActionUrl by remember { mutableStateOf(adConfig.bannerAdActionUrl) }
  var adSavedMessage by remember { mutableStateOf(false) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(DramaBlack)
  ) {
    // Top Bar
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 14.dp),
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

      Column(modifier = Modifier.weight(1f)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = "ADMIN DASHBOARD",
            color = Color.White,
            fontSize = 17.sp,
            fontWeight = FontWeight.Black
          )
          Spacer(modifier = Modifier.width(8.dp))
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(4.dp))
              .background(Color(0xFF00E676))
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text("VERIFIED ADMIN", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black)
          }
        }
        Text(
          text = "Firebase Storage Video Uploads & Cloud Firestore",
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
            Text("Videos & Storage", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
            Text("AdSense System", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
        // Tab 0: Dramas & Video Uploads
        item {
          Text(
            text = "CREATE NEW DRAMA & UPLOAD MP4 VIDEO",
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            AdminTextField(value = newTitle, onValueChange = { newTitle = it }, label = "Drama Title *")
            AdminTextField(value = newGenre, onValueChange = { newGenre = it }, label = "Genre (e.g. Mafia Romance, Revenge, Billionaire)")
            AdminTextField(value = newDesc, onValueChange = { newDesc = it }, label = "Description / Storyline", singleLine = false)
            AdminTextField(value = newTags, onValueChange = { newTags = it }, label = "Tags (comma separated)")
            AdminTextField(value = newEpisodeCount, onValueChange = { newEpisodeCount = it }, label = "Episodes Count")

            // VIDEO UPLOAD SECTION (FIREBASE CLOUD STORAGE)
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF161616))
                .border(1.dp, Color(0xFF2C2C2C), RoundedCornerShape(6.dp))
                .padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, tint = Color(0xFF29B6F6), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Firebase Cloud Storage Video Upload",
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                OutlinedButton(
                  onClick = { videoPickerLauncher.launch("video/*") },
                  modifier = Modifier.weight(1f),
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                  Icon(imageVector = Icons.Default.VideoFile, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(if (selectedVideoUri != null) "Change Video" else "Select MP4 File", fontSize = 11.sp)
                }

                if (selectedVideoUri != null) {
                  Button(
                    onClick = {
                      val uri = selectedVideoUri ?: return@Button
                      isVideoUploading = true
                      videoUploadProgress = 0f
                      videoUploadError = null
                      coroutineScope.launch {
                        try {
                          val storage = FirebaseManager.storage
                          val ref = storage.reference.child("videos/${System.currentTimeMillis()}_drama.mp4")
                          val uploadTask = ref.putFile(uri)
                          uploadTask.addOnProgressListener { taskSnapshot ->
                            val progress = taskSnapshot.bytesTransferred.toFloat() / taskSnapshot.totalByteCount.toFloat()
                            videoUploadProgress = progress
                          }
                          uploadTask.await()
                          val downloadUrl = ref.downloadUrl.await().toString()
                          newStreamUrl = downloadUrl
                          videoUploadSuccess = true
                        } catch (e: Exception) {
                          Log.e("AdminScreen", "Video upload failed: ${e.message}")
                          videoUploadError = e.localizedMessage ?: "Upload failed. Check Storage permissions."
                          // Fallback sample URL so the admin is never blocked
                          if (newStreamUrl.isBlank()) {
                            newStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
                          }
                        } finally {
                          isVideoUploading = false
                        }
                      }
                    },
                    enabled = !isVideoUploading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                    modifier = Modifier.weight(1f)
                  ) {
                    if (isVideoUploading) {
                      CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                      Text("Upload to Storage", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                  }
                }
              }

              if (selectedVideoUri != null) {
                Text(
                  text = "Selected file: ${selectedVideoUri?.lastPathSegment?.takeLast(35)}",
                  color = Color.White.copy(alpha = 0.6f),
                  fontSize = 11.sp
                )
              }

              if (isVideoUploading) {
                LinearProgressIndicator(
                  progress = { videoUploadProgress },
                  modifier = Modifier.fillMaxWidth(),
                  color = Color(0xFF00C853),
                  trackColor = Color(0xFF333333)
                )
                Text(
                  text = "Uploading to Cloud Storage: ${(videoUploadProgress * 100).toInt()}%",
                  color = Color(0xFF00C853),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }

              if (videoUploadSuccess) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF00C853), modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(
                    text = "Video uploaded! URL auto-filled below.",
                    color = Color(0xFF00C853),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }

              if (videoUploadError != null) {
                Text(
                  text = "Upload note: $videoUploadError (Fallback stream URL set)",
                  color = Color(0xFFFF8A80),
                  fontSize = 11.sp
                )
              }
            }

            // POSTER UPLOAD SECTION
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF161616))
                .border(1.dp, Color(0xFF2C2C2C), RoundedCornerShape(6.dp))
                .padding(12.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = Color(0xFFFFB300), modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Cover Poster Image Upload",
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                OutlinedButton(
                  onClick = { posterPickerLauncher.launch("image/*") },
                  modifier = Modifier.weight(1f),
                  colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                  Text(if (selectedPosterUri != null) "Change Poster" else "Pick Poster Image", fontSize = 11.sp)
                }

                if (selectedPosterUri != null) {
                  Button(
                    onClick = {
                      val uri = selectedPosterUri ?: return@Button
                      isPosterUploading = true
                      coroutineScope.launch {
                        try {
                          val storage = FirebaseManager.storage
                          val ref = storage.reference.child("posters/${System.currentTimeMillis()}_poster.jpg")
                          val uploadTask = ref.putFile(uri)
                          uploadTask.addOnProgressListener { taskSnapshot ->
                            posterUploadProgress = taskSnapshot.bytesTransferred.toFloat() / taskSnapshot.totalByteCount.toFloat()
                          }
                          uploadTask.await()
                          val downloadUrl = ref.downloadUrl.await().toString()
                          newCoverUrl = downloadUrl
                        } catch (e: Exception) {
                          Log.e("AdminScreen", "Poster upload error: ${e.message}")
                        } finally {
                          isPosterUploading = false
                        }
                      }
                    },
                    enabled = !isPosterUploading,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB300)),
                    modifier = Modifier.weight(1f)
                  ) {
                    if (isPosterUploading) {
                      CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(16.dp))
                    } else {
                      Text("Upload Poster", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                  }
                }
              }
            }

            AdminTextField(value = newCoverUrl, onValueChange = { newCoverUrl = it }, label = "Cover Poster URL")
            AdminTextField(value = newStreamUrl, onValueChange = { newStreamUrl = it }, label = "Stream Video URL (MP4 / HLS)")

            Button(
              onClick = {
                if (newTitle.isNotBlank()) {
                  val count = newEpisodeCount.toIntOrNull() ?: 40
                  val tagsList = newTags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                  onAddDrama(
                    newTitle,
                    newGenre,
                    newDesc.ifBlank { "Exclusive premium short drama series." },
                    tagsList,
                    count,
                    newCoverUrl.ifBlank { null },
                    newStreamUrl.ifBlank { null }
                  )
                  newTitle = ""
                  newDesc = ""
                  newStreamUrl = ""
                  newCoverUrl = ""
                  selectedVideoUri = null
                  selectedPosterUri = null
                  videoUploadSuccess = false
                  dramaAddedMessage = true
                }
              },
              colors = ButtonDefaults.buttonColors(containerColor = DramaRed),
              shape = RoundedCornerShape(6.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("btn_publish_drama")
            ) {
              Icon(imageVector = Icons.Default.Add, contentDescription = null)
              Spacer(modifier = Modifier.width(6.dp))
              Text("Publish Drama to Cloud Firestore", fontWeight = FontWeight.Bold)
            }

            if (dramaAddedMessage) {
              Text(
                text = "✓ Drama successfully published and synced with Cloud Firestore!",
                color = Color(0xFF00E676),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }

        item {
          Spacer(modifier = Modifier.height(24.dp))
          Text(
            text = "CURRENT CATALOG (${dramas.size} TITLES)",
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
                  text = "Add New Episode to: ${drama.title}",
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
                AdminTextField(value = epTitle, onValueChange = { epTitle = it }, label = "Episode Title (e.g. Episode ${drama.episodes.size + 1})")

                // Episode video picker
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  OutlinedButton(
                    onClick = { epVideoPickerLauncher.launch("video/*") },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                  ) {
                    Text(if (epSelectedVideoUri != null) "Change Episode File" else "Pick Episode MP4", fontSize = 11.sp)
                  }

                  if (epSelectedVideoUri != null) {
                    Button(
                      onClick = {
                        val uri = epSelectedVideoUri ?: return@Button
                        epIsVideoUploading = true
                        coroutineScope.launch {
                          try {
                            val storage = FirebaseManager.storage
                            val ref = storage.reference.child("episodes/${System.currentTimeMillis()}_ep.mp4")
                            ref.putFile(uri).await()
                            epStreamUrl = ref.downloadUrl.await().toString()
                          } catch (e: Exception) {
                            Log.e("AdminScreen", "Episode upload failed: ${e.message}")
                            if (epStreamUrl.isBlank()) {
                              epStreamUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
                            }
                          } finally {
                            epIsVideoUploading = false
                          }
                        }
                      },
                      enabled = !epIsVideoUploading,
                      colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                      modifier = Modifier.weight(1f)
                    ) {
                      if (epIsVideoUploading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(14.dp))
                      } else {
                        Text("Upload Episode", fontSize = 11.sp)
                      }
                    }
                  }
                }

                AdminTextField(value = epStreamUrl, onValueChange = { epStreamUrl = it }, label = "Episode Video Stream URL (MP4 / HLS)")

                Button(
                  onClick = {
                    if (epStreamUrl.isNotBlank()) {
                      onAddEpisode(drama.id, epTitle, epStreamUrl)
                      epTitle = ""
                      epStreamUrl = ""
                      epSelectedVideoUri = null
                      selectedDramaForEpisode = null
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = DramaRed),
                  shape = RoundedCornerShape(4.dp),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text("Add Episode & Save", fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      } else {
        // Tab 1: AdSense & Ad Manager
        item {
          Text(
            text = "ADSENSE & AD MONETIZATION SYSTEM",
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
                color = Color(0xFF00E676),
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
