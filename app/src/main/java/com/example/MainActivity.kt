package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.Drama
import com.example.data.DramaRepository
import com.example.data.Episode
import com.example.data.db.WatchlistRepository
import com.example.data.firebase.FirebaseManager
import com.example.data.firebase.FirestoreDramaRepository
import com.example.util.NetworkConnectivityObserver
import com.example.util.NetworkStatus
import kotlinx.coroutines.launch
import com.example.ui.components.DramaBottomNavigation
import com.example.ui.components.DramaNavDestination
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BrowseScreen
import com.example.ui.screens.DramaDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MyListScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.VideoPlayerScreen
import com.example.ui.theme.DramaBlack
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

sealed class AppScreen {
  data class MainTab(val destination: DramaNavDestination) : AppScreen()
  data class Detail(val dramaId: String) : AppScreen()
  data class Player(val dramaId: String, val episodeId: String, val initialPositionMs: Long = 0L) : AppScreen()
  object Admin : AppScreen()
  object Profile : AppScreen()
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    FirebaseManager.initialize(this)

    setContent {
      MyApplicationTheme {
        val auth = FirebaseManager.auth
        var currentUser by remember { mutableStateOf<FirebaseUser?>(auth.currentUser) }

        DisposableEffect(auth) {
          val listener = FirebaseAuth.AuthStateListener { fbAuth ->
            currentUser = fbAuth.currentUser
            val user = fbAuth.currentUser
            if (user != null) {
              DramaRepository.setFirebaseUserProfile(
                uid = user.uid,
                name = user.displayName ?: user.email?.substringBefore("@") ?: "Viewer",
                email = user.email ?: "",
                isAdmin = FirebaseManager.isUserAdmin(user.email)
              )
            }
          }
          auth.addAuthStateListener(listener)
          onDispose {
            auth.removeAuthStateListener(listener)
          }
        }

        val activeUser = currentUser
        if (activeUser == null) {
          AuthScreen(
            onAuthSuccess = { user ->
              currentUser = user
            }
          )
        } else {
          DramaApp(
            currentUser = activeUser,
            onSignOut = {
              auth.signOut()
              currentUser = null
            }
          )
        }
      }
    }
  }
}

@Composable
fun DramaApp(
  currentUser: FirebaseUser,
  onSignOut: () -> Unit
) {
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()

  // Start real-time Firestore sync for dramas catalog and watch progress
  LaunchedEffect(Unit) {
    FirestoreDramaRepository.startListening(this)
  }

  val dramas by DramaRepository.dramas.collectAsState()
  val currentProfile by DramaRepository.currentProfile.collectAsState()
  val allProfiles by DramaRepository.profiles.collectAsState()
  val adConfig by DramaRepository.adConfig.collectAsState()
  val isFirestoreLoading by FirestoreDramaRepository.isLoading.collectAsState()

  // Real-time Firestore sync for user's Watchlist and Watch Progress
  DisposableEffect(currentUser.uid) {
    val watchlistRegistration = FirestoreDramaRepository.listenToUserWatchlist(currentUser.uid) { ids ->
      if (ids.isNotEmpty()) {
        DramaRepository.updateWatchlistFromFirestore(ids)
      }
    }
    val progressRegistration = FirestoreDramaRepository.listenToUserWatchProgress(currentUser.uid) { progressMap ->
      if (progressMap.isNotEmpty()) {
        DramaRepository.updateWatchHistoryFromFirestore(progressMap)
      }
    }
    onDispose {
      watchlistRegistration?.remove()
      progressRegistration?.remove()
    }
  }

  // Room Database Watchlist Repository & State
  val watchlistRepo = remember { WatchlistRepository.getInstance(context) }
  val watchlistItems by watchlistRepo.allWatchlist.collectAsState(initial = emptyList())
  val watchlistDramaIds by watchlistRepo.watchlistedDramaIds.collectAsState(initial = emptySet())

  // Pre-seed watchlist if completely empty so user immediately experiences Room persistence
  LaunchedEffect(dramas) {
    if (watchlistItems.isEmpty() && dramas.isNotEmpty()) {
      dramas.take(2).forEach { drama ->
        watchlistRepo.addToWatchlist(drama)
      }
    }
  }

  // Network Connectivity Observer (Requirement 5)
  val connectivityObserver = remember { NetworkConnectivityObserver(context) }
  val networkStatus by connectivityObserver.observe().collectAsState(initial = NetworkStatus.Available)
  val snackbarHostState = remember { SnackbarHostState() }
  var hasShownLostSnackbar by remember { mutableStateOf(false) }

  LaunchedEffect(networkStatus) {
    when (networkStatus) {
      NetworkStatus.Lost, NetworkStatus.Unavailable -> {
        hasShownLostSnackbar = true
        snackbarHostState.showSnackbar(
          message = "No internet connection. Offline downloads are still available.",
          actionLabel = "Dismiss",
          duration = SnackbarDuration.Indefinite
        )
      }
      NetworkStatus.Available -> {
        if (hasShownLostSnackbar) {
          snackbarHostState.currentSnackbarData?.dismiss()
          snackbarHostState.showSnackbar(
            message = "Internet connection restored.",
            duration = SnackbarDuration.Short
          )
          hasShownLostSnackbar = false
        }
      }
    }
  }

  var currentScreen by remember { mutableStateOf<AppScreen>(AppScreen.MainTab(DramaNavDestination.HOME)) }

  // Handle system back button gracefully
  BackHandler(enabled = currentScreen !is AppScreen.MainTab || (currentScreen as AppScreen.MainTab).destination != DramaNavDestination.HOME) {
    when (currentScreen) {
      is AppScreen.Detail, is AppScreen.Admin, is AppScreen.Profile -> {
        currentScreen = AppScreen.MainTab(DramaNavDestination.HOME)
      }
      is AppScreen.Player -> {
        val playerScreen = currentScreen as AppScreen.Player
        currentScreen = AppScreen.Detail(playerScreen.dramaId)
      }
      is AppScreen.MainTab -> {
        val tabScreen = currentScreen as AppScreen.MainTab
        if (tabScreen.destination != DramaNavDestination.HOME) {
          currentScreen = AppScreen.MainTab(DramaNavDestination.HOME)
        }
      }
    }
  }

  // Responsive container centered for tablets and desktops
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.Black),
    contentAlignment = Alignment.Center
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .widthIn(max = 600.dp)
        .background(DramaBlack)
    ) {
      val isMainTab = currentScreen is AppScreen.MainTab
      val currentDestination = (currentScreen as? AppScreen.MainTab)?.destination ?: DramaNavDestination.HOME

      Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DramaBlack,
        snackbarHost = {
          SnackbarHost(
            hostState = snackbarHostState,
            snackbar = { data ->
              Snackbar(
                snackbarData = data,
                containerColor = Color(0xFF222222),
                contentColor = Color.White,
                actionColor = com.example.ui.theme.DramaRed
              )
            }
          )
        },
        bottomBar = {
          if (isMainTab) {
            DramaBottomNavigation(
              currentDestination = currentDestination,
              onNavigate = { dest ->
                currentScreen = AppScreen.MainTab(dest)
              }
            )
          }
        }
      ) { innerPadding ->
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(
              top = if (currentScreen is AppScreen.Player) 0.dp else innerPadding.calculateTopPadding(),
              bottom = if (isMainTab) 0.dp else innerPadding.calculateBottomPadding()
            )
        ) {
          when (val screen = currentScreen) {
            is AppScreen.MainTab -> {
              when (screen.destination) {
                DramaNavDestination.HOME -> {
                  HomeScreen(
                    dramas = dramas,
                    currentProfile = currentProfile,
                    adConfig = adConfig,
                    watchlistDramaIds = watchlistDramaIds,
                    isLoading = isFirestoreLoading,
                    onDramaClick = { drama -> currentScreen = AppScreen.Detail(drama.id) },
                    onPlayDrama = { drama ->
                      val resumeProg = DramaRepository.getResumeProgress(drama.id)
                      val targetEp = if (resumeProg != null) {
                        drama.episodes.find { it.episodeNumber == resumeProg.episodeNumber } ?: drama.episodes.firstOrNull()
                      } else drama.episodes.firstOrNull()
                      val firstEp = targetEp ?: Episode(
                        id = "${drama.id}_ep_1",
                        dramaId = drama.id,
                        episodeNumber = 1,
                        title = "Episode 1",
                        videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
                      )
                      currentScreen = AppScreen.Player(drama.id, firstEp.id, resumeProg?.positionMs ?: 0L)
                    },
                    onFavoriteClick = { id ->
                      val drama = dramas.find { it.id == id }
                      if (drama != null) {
                        coroutineScope.launch {
                          watchlistRepo.toggleWatchlist(drama)
                          DramaRepository.toggleFavorite(id)
                          FirestoreDramaRepository.toggleWatchlistInFirestore(currentUser.uid, id)
                        }
                      } else {
                        DramaRepository.toggleFavorite(id)
                        FirestoreDramaRepository.toggleWatchlistInFirestore(currentUser.uid, id)
                      }
                    },
                    onToggleWatchlist = { drama ->
                      coroutineScope.launch {
                        watchlistRepo.toggleWatchlist(drama)
                        DramaRepository.toggleFavorite(drama.id)
                        FirestoreDramaRepository.toggleWatchlistInFirestore(currentUser.uid, drama.id)
                      }
                    },
                    onProfileClick = { currentScreen = AppScreen.Profile },
                    onAdminClick = { currentScreen = AppScreen.Admin }
                  )
                }

                DramaNavDestination.SEARCH -> {
                  SearchScreen(
                    dramas = dramas,
                    currentProfile = currentProfile,
                    onDramaClick = { drama -> currentScreen = AppScreen.Detail(drama.id) },
                    onFavoriteClick = { id -> DramaRepository.toggleFavorite(id) }
                  )
                }

                DramaNavDestination.STREAM_REEL -> {
                  // Instant Vertical Drama Reel playback (Photo 8)
                  val featured = dramas.find { it.id == "drama_bugatti" } ?: dramas.first()
                  val initialEp = featured.episodes.firstOrNull() ?: Episode(
                    id = "${featured.id}_ep_1",
                    dramaId = featured.id,
                    episodeNumber = 1,
                    title = "Episode 1",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    subtitleText = "Three million dollars. Leave tonight and never look back."
                  )
                  VideoPlayerScreen(
                    drama = featured,
                    initialEpisode = initialEp,
                    currentProfile = currentProfile,
                    adConfig = adConfig,
                    onBackClick = { currentScreen = AppScreen.MainTab(DramaNavDestination.HOME) },
                    onFavoriteClick = { id -> DramaRepository.toggleFavorite(id) },
                    onDownloadClick = { id -> DramaRepository.toggleDownload(id) },
                    onRecordProgress = { id, epNum, pos, dur ->
                      DramaRepository.recordWatchProgress(id, epNum, pos, dur)
                      FirestoreDramaRepository.saveWatchProgress(currentUser.uid, id, epNum, pos, dur)
                    }
                  )
                }

                DramaNavDestination.BROWSE -> {
                  BrowseScreen(
                    dramas = dramas,
                    currentProfile = currentProfile,
                    onDramaClick = { drama -> currentScreen = AppScreen.Detail(drama.id) },
                    onFavoriteClick = { id -> DramaRepository.toggleFavorite(id) }
                  )
                }

                DramaNavDestination.MY_LIST -> {
                  MyListScreen(
                    dramas = dramas,
                    currentProfile = currentProfile,
                    watchlist = watchlistItems,
                    onDramaClick = { drama -> currentScreen = AppScreen.Detail(drama.id) },
                    onFavoriteClick = { id ->
                      val drama = dramas.find { it.id == id }
                      if (drama != null) {
                        coroutineScope.launch {
                          watchlistRepo.toggleWatchlist(drama)
                          DramaRepository.toggleFavorite(id)
                          FirestoreDramaRepository.toggleWatchlistInFirestore(currentUser.uid, id)
                        }
                      } else {
                        DramaRepository.toggleFavorite(id)
                        FirestoreDramaRepository.toggleWatchlistInFirestore(currentUser.uid, id)
                      }
                    },
                    onRemoveDownload = { id -> DramaRepository.toggleDownload(id) },
                    onRemoveFromWatchlist = { id ->
                      coroutineScope.launch {
                        watchlistRepo.removeFromWatchlist(id)
                        DramaRepository.toggleFavorite(id)
                        FirestoreDramaRepository.toggleWatchlistInFirestore(currentUser.uid, id)
                      }
                    }
                  )
                }
              }
            }

            is AppScreen.Detail -> {
              val drama = dramas.find { it.id == screen.dramaId } ?: dramas.first()
              DramaDetailScreen(
                drama = drama,
                currentProfile = currentProfile,
                isLoading = isFirestoreLoading,
                onBackClick = { currentScreen = AppScreen.MainTab(DramaNavDestination.HOME) },
                onPlayEpisode = { d, ep ->
                  val resumeProg = DramaRepository.getResumeProgress(d.id)
                  val initialPos = if (resumeProg?.episodeNumber == ep.episodeNumber) resumeProg.positionMs else 0L
                  currentScreen = AppScreen.Player(d.id, ep.id, initialPos)
                },
                onFavoriteClick = { id ->
                  val d = dramas.find { it.id == id }
                  if (d != null) {
                    coroutineScope.launch {
                      watchlistRepo.toggleWatchlist(d)
                      DramaRepository.toggleFavorite(id)
                      FirestoreDramaRepository.toggleWatchlistInFirestore(currentUser.uid, id)
                    }
                  } else {
                    DramaRepository.toggleFavorite(id)
                    FirestoreDramaRepository.toggleWatchlistInFirestore(currentUser.uid, id)
                  }
                }
              )
            }

            is AppScreen.Player -> {
              val drama = dramas.find { it.id == screen.dramaId } ?: dramas.first()
              val episode = drama.episodes.find { it.id == screen.episodeId }
                ?: drama.episodes.firstOrNull()
                ?: Episode(
                  id = "${drama.id}_ep_1",
                  dramaId = drama.id,
                  episodeNumber = 1,
                  title = "Episode 1",
                  videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
                )
              VideoPlayerScreen(
                drama = drama,
                initialEpisode = episode,
                currentProfile = currentProfile,
                adConfig = adConfig,
                initialPositionMs = screen.initialPositionMs,
                onBackClick = { currentScreen = AppScreen.Detail(drama.id) },
                onFavoriteClick = { id -> DramaRepository.toggleFavorite(id) },
                onDownloadClick = { id -> DramaRepository.toggleDownload(id) },
                onRecordProgress = { id, epNum, pos, dur ->
                  DramaRepository.recordWatchProgress(id, epNum, pos, dur)
                  FirestoreDramaRepository.saveWatchProgress(currentUser.uid, id, epNum, pos, dur)
                }
              )
            }

            is AppScreen.Admin -> {
              AdminScreen(
                dramas = dramas,
                adConfig = adConfig,
                currentProfile = currentProfile,
                onBackClick = { currentScreen = AppScreen.MainTab(DramaNavDestination.HOME) },
                onAddDrama = { title, genre, desc, tags, count, coverUrl, streamUrl ->
                  DramaRepository.addDrama(title, genre, desc, tags, count, coverUrl, streamUrl)
                },
                onDeleteDrama = { id -> DramaRepository.deleteDrama(id) },
                onAddEpisode = { dId, title, url -> DramaRepository.addEpisode(dId, title, url) },
                onDeleteEpisode = { dId, epId -> DramaRepository.deleteEpisode(dId, epId) },
                onSaveAdConfig = { newCfg -> DramaRepository.updateAdConfig(newCfg) },
                onSwitchToAdminLogin = onSignOut
              )
            }

            is AppScreen.Profile -> {
              ProfileScreen(
                currentProfile = currentProfile,
                allProfiles = allProfiles,
                onSelectProfile = { pId -> DramaRepository.switchProfile(pId) },
                onCreateProfile = { name, email, isAdmin ->
                  DramaRepository.createProfile(name, email, isAdmin)
                },
                onBackClick = { currentScreen = AppScreen.MainTab(DramaNavDestination.HOME) },
                onOpenAdmin = { currentScreen = AppScreen.Admin },
                onSignOut = onSignOut
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
  Text(text = "Hello $name!", modifier = modifier)
}
