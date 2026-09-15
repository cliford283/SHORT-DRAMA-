package com.example.data.firebase

import android.util.Log
import com.example.data.Drama
import com.example.data.DramaRepository
import com.example.data.Episode
import com.example.data.WatchProgress
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

object FirestoreDramaRepository {
  private const val TAG = "FirestoreDramaRepo"
  private val db get() = FirebaseManager.firestore
  private val dramasCollection get() = db.collection("dramas")
  private val usersCollection get() = db.collection("users")

  private var isListening = false
  private val _isLoading = MutableStateFlow(true)
  val isLoading = _isLoading.asStateFlow()

  fun parseDramaDoc(doc: DocumentSnapshot): Drama? {
    return try {
      val id = doc.id
      val title = doc.getString("title") ?: "Untitled Drama"
      val genre = doc.getString("genre") ?: "Romance"
      val desc = doc.getString("description") ?: ""
      val coverUrl = doc.getString("coverUrl")
      val tags = (doc.get("tags") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
      val epCount = doc.getLong("episodesCount")?.toInt() ?: 10
      val isTrending = doc.getBoolean("isTrending") ?: false
      val isNewRelease = doc.getBoolean("isNewRelease") ?: false
      val isForYou = doc.getBoolean("isForYou") ?: false

      val rawEpisodes = (doc.get("episodes") as? List<*>) ?: emptyList<Any>()
      val episodes = rawEpisodes.mapIndexedNotNull { index, epObj ->
        val epMap = epObj as? Map<*, *> ?: return@mapIndexedNotNull null
        val epId = epMap["id"]?.toString() ?: "${id}_ep_${index + 1}"
        val epTitle = epMap["title"]?.toString() ?: "Episode ${index + 1}"
        val epNum = (epMap["episodeNumber"] as? Number)?.toInt() ?: (index + 1)
        val videoUrl = epMap["videoUrl"]?.toString()
          ?: "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
        val duration = (epMap["durationSeconds"] as? Number)?.toInt() ?: 120
        val subText = epMap["subtitleText"]?.toString() ?: ""

        Episode(
          id = epId,
          dramaId = id,
          episodeNumber = epNum,
          title = epTitle,
          durationSeconds = duration,
          videoUrl = videoUrl,
          subtitleText = subText
        )
      }

      Drama(
        id = id,
        title = title,
        genre = genre,
        description = desc,
        coverUrl = coverUrl,
        tags = tags,
        episodesCount = if (episodes.isNotEmpty()) episodes.size else epCount,
        isTrending = isTrending,
        isNewRelease = isNewRelease,
        isForYou = isForYou,
        episodes = if (episodes.isNotEmpty()) episodes else DramaRepository.generateDefaultEpisodes(id, epCount, title)
      )
    } catch (e: Exception) {
      Log.e(TAG, "Error parsing drama doc ${doc.id}: ${e.message}")
      null
    }
  }

  fun startListening(scope: CoroutineScope) {
    if (isListening) return
    isListening = true
    _isLoading.value = true

    // Real-time snapshot listener on dramas
    try {
      dramasCollection.addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.w(TAG, "Firestore listen failed: ${error.message}")
          _isLoading.value = false
          return@addSnapshotListener
        }

        if (snapshot != null && !snapshot.isEmpty) {
          val fetchedDramas = snapshot.documents.mapNotNull { doc -> parseDramaDoc(doc) }
          if (fetchedDramas.isNotEmpty()) {
            DramaRepository.setDramasFromFirestore(fetchedDramas)
          }
          _isLoading.value = false
        } else if (snapshot != null && snapshot.isEmpty) {
          // If Firestore is empty, seed initial viral dramas to Firestore in the background
          scope.launch(Dispatchers.IO) {
            seedInitialDramasToFirestore()
            _isLoading.value = false
          }
        } else {
          _isLoading.value = false
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error initializing snapshot listener: ${e.message}")
      _isLoading.value = false
    }
  }

  private suspend fun seedInitialDramasToFirestore() {
    try {
      val initialList = DramaRepository.initialDramas
      for (drama in initialList) {
        val dramaMap = hashMapOf(
          "title" to drama.title,
          "genre" to drama.genre,
          "description" to drama.description,
          "coverUrl" to (drama.coverUrl ?: ""),
          "tags" to drama.tags,
          "episodesCount" to drama.episodesCount,
          "isTrending" to drama.isTrending,
          "isNewRelease" to drama.isNewRelease,
          "isForYou" to drama.isForYou,
          "createdAt" to FieldValue.serverTimestamp(),
          "episodes" to drama.episodes.map { ep ->
            hashMapOf(
              "id" to ep.id,
              "episodeNumber" to ep.episodeNumber,
              "title" to ep.title,
              "durationSeconds" to ep.durationSeconds,
              "videoUrl" to ep.videoUrl,
              "subtitleText" to ep.subtitleText
            )
          }
        )
        dramasCollection.document(drama.id).set(dramaMap).await()
      }
      Log.i(TAG, "Successfully seeded ${initialList.size} dramas into Firestore")
    } catch (e: Exception) {
      Log.w(TAG, "Seed to Firestore skipped or error: ${e.message}")
    }
  }

  // FIRESTORE WATCHLIST MANAGEMENT
  fun toggleWatchlistInFirestore(
    userId: String,
    dramaId: String,
    isCurrentlySaved: Boolean,
    onComplete: (Boolean) -> Unit = {}
  ) {
    if (userId.isBlank() || dramaId.isBlank()) return
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val userRef = usersCollection.document(userId)
        val subColRef = userRef.collection("watchlist").document(dramaId)

        if (isCurrentlySaved) {
          userRef.set(
            hashMapOf("savedDramaIds" to FieldValue.arrayRemove(dramaId)),
            SetOptions.merge()
          ).await()
          subColRef.delete().await()
        } else {
          userRef.set(
            hashMapOf("savedDramaIds" to FieldValue.arrayUnion(dramaId)),
            SetOptions.merge()
          ).await()
          subColRef.set(
            hashMapOf(
              "dramaId" to dramaId,
              "addedAt" to FieldValue.serverTimestamp()
            ),
            SetOptions.merge()
          ).await()
        }
        onComplete(true)
      } catch (e: Exception) {
        Log.e(TAG, "Error updating watchlist in Firestore: ${e.message}")
        onComplete(false)
      }
    }
  }

  fun listenToUserWatchlist(userId: String, onUpdate: (Set<String>) -> Unit): ListenerRegistration? {
    if (userId.isBlank()) return null
    return try {
      usersCollection.document(userId).addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.w(TAG, "User watchlist listen failed: ${error.message}")
          return@addSnapshotListener
        }
        val ids = (snapshot?.get("savedDramaIds") as? List<*>)
          ?.mapNotNull { it?.toString() }
          ?.toSet() ?: emptySet()
        onUpdate(ids)
      }
    } catch (e: Exception) {
      Log.w(TAG, "Failed setting up watchlist listener: ${e.message}")
      null
    }
  }

  // FIRESTORE WATCH PROGRESS SYNC
  fun saveWatchProgress(userId: String, dramaId: String, progress: WatchProgress) {
    if (userId.isBlank() || dramaId.isBlank()) return
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val progressMap = hashMapOf(
          "dramaId" to dramaId,
          "episodeNumber" to progress.episodeNumber,
          "positionMs" to progress.positionMs,
          "durationMs" to progress.durationMs,
          "lastWatchedTimestamp" to progress.lastWatchedTimestamp,
          "updatedAt" to FieldValue.serverTimestamp()
        )
        usersCollection.document(userId)
          .collection("watchProgress")
          .document(dramaId)
          .set(progressMap, SetOptions.merge())
          .await()
      } catch (e: Exception) {
        Log.w(TAG, "Error writing watch progress to Firestore: ${e.message}")
      }
    }
  }

  fun listenToUserWatchProgress(
    userId: String,
    onUpdate: (Map<String, WatchProgress>) -> Unit
  ): ListenerRegistration? {
    if (userId.isBlank()) return null
    return try {
      usersCollection.document(userId).collection("watchProgress")
        .addSnapshotListener { snapshot, error ->
          if (error != null) {
            Log.w(TAG, "Watch progress listen failed: ${error.message}")
            return@addSnapshotListener
          }
          if (snapshot != null) {
            val progressMap = snapshot.documents.mapNotNull { doc ->
              try {
                val dId = doc.getString("dramaId") ?: doc.id
                val epNum = doc.getLong("episodeNumber")?.toInt() ?: 1
                val posMs = doc.getLong("positionMs") ?: 0L
                val durMs = doc.getLong("durationMs") ?: 0L
                val ts = doc.getLong("lastWatchedTimestamp") ?: System.currentTimeMillis()
                dId to WatchProgress(dId, epNum, posMs, durMs, ts)
              } catch (e: Exception) {
                null
              }
            }.toMap()
            onUpdate(progressMap)
          }
        }
    } catch (e: Exception) {
      Log.w(TAG, "Failed setting up watch progress listener: ${e.message}")
      null
    }
  }

  // REAL-TIME FIRESTORE SEARCH BY TITLE OR GENRE
  fun searchDramasRealtime(
    query: String,
    onResult: (List<Drama>) -> Unit
  ): ListenerRegistration? {
    val cleanQuery = query.trim()
    if (cleanQuery.isBlank()) {
      onResult(emptyList())
      return null
    }

    return try {
      // Real-time snapshot listener on Firestore collection
      dramasCollection.addSnapshotListener { snapshot, error ->
        if (error != null) {
          Log.w(TAG, "Firestore search error: ${error.message}")
          // Fallback to local filtering if network error
          val localResults = DramaRepository.dramas.value.filter {
            it.title.contains(cleanQuery, ignoreCase = true) ||
            it.genre.contains(cleanQuery, ignoreCase = true) ||
            it.description.contains(cleanQuery, ignoreCase = true) ||
            it.tags.any { tag -> tag.contains(cleanQuery, ignoreCase = true) }
          }
          onResult(localResults)
          return@addSnapshotListener
        }

        if (snapshot != null) {
          val matches = snapshot.documents.mapNotNull { doc ->
            parseDramaDoc(doc)
          }.filter { drama ->
            drama.title.contains(cleanQuery, ignoreCase = true) ||
            drama.genre.contains(cleanQuery, ignoreCase = true) ||
            drama.description.contains(cleanQuery, ignoreCase = true) ||
            drama.tags.any { tag -> tag.contains(cleanQuery, ignoreCase = true) }
          }
          onResult(matches)
        } else {
          onResult(emptyList())
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Search query exception: ${e.message}")
      onResult(emptyList())
      null
    }
  }

  fun saveDrama(drama: Drama, onComplete: (Boolean) -> Unit = {}) {
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val dramaMap = hashMapOf(
          "title" to drama.title,
          "genre" to drama.genre,
          "description" to drama.description,
          "coverUrl" to (drama.coverUrl ?: ""),
          "tags" to drama.tags,
          "episodesCount" to drama.episodesCount,
          "isTrending" to drama.isTrending,
          "isNewRelease" to drama.isNewRelease,
          "isForYou" to drama.isForYou,
          "updatedAt" to FieldValue.serverTimestamp(),
          "episodes" to drama.episodes.map { ep ->
            hashMapOf(
              "id" to ep.id,
              "episodeNumber" to ep.episodeNumber,
              "title" to ep.title,
              "durationSeconds" to ep.durationSeconds,
              "videoUrl" to ep.videoUrl,
              "subtitleText" to ep.subtitleText
            )
          }
        )
        dramasCollection.document(drama.id).set(dramaMap, SetOptions.merge()).await()
        onComplete(true)
      } catch (e: Exception) {
        Log.e(TAG, "Error saving drama to Firestore: ${e.message}")
        onComplete(false)
      }
    }
  }

  fun deleteDrama(dramaId: String, onComplete: (Boolean) -> Unit = {}) {
    CoroutineScope(Dispatchers.IO).launch {
      try {
        dramasCollection.document(dramaId).delete().await()
        onComplete(true)
      } catch (e: Exception) {
        Log.e(TAG, "Error deleting drama from Firestore: ${e.message}")
        onComplete(false)
      }
    }
  }

  fun syncUserProfile(userId: String, email: String, name: String, isAdmin: Boolean) {
    if (userId.isBlank()) return
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val userMap = hashMapOf(
          "uid" to userId,
          "email" to email,
          "displayName" to name,
          "isAdmin" to isAdmin,
          "lastLogin" to FieldValue.serverTimestamp()
        )
        usersCollection.document(userId).set(userMap, SetOptions.merge()).await()
      } catch (e: Exception) {
        Log.w(TAG, "Error syncing user profile to Firestore: ${e.message}")
      }
    }
  }
}
