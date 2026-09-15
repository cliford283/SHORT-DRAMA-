package com.example.data.db

import android.content.Context
import com.example.data.Drama
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class WatchlistRepository(private val dao: WatchlistDao) {

  val allWatchlist: Flow<List<WatchlistEntity>> = dao.getAllWatchlist()

  val watchlistedDramaIds: Flow<Set<String>> = dao.getAllWatchlist().map { list ->
    list.map { it.id }.toSet()
  }

  fun isWatchlisted(dramaId: String): Flow<Boolean> = dao.isWatchlisted(dramaId)

  suspend fun addToWatchlist(drama: Drama) {
    val entity = WatchlistEntity(
      id = drama.id,
      title = drama.title,
      genre = drama.genre,
      description = drama.description,
      coverResId = drama.coverResId,
      coverUrl = drama.coverUrl,
      episodesCount = drama.episodesCount
    )
    dao.insert(entity)
  }

  suspend fun removeFromWatchlist(dramaId: String) {
    dao.deleteById(dramaId)
  }

  suspend fun toggleWatchlist(drama: Drama) {
    if (dao.contains(drama.id)) {
      dao.deleteById(drama.id)
    } else {
      addToWatchlist(drama)
    }
  }

  companion object {
    @Volatile
    private var INSTANCE: WatchlistRepository? = null

    fun getInstance(context: Context): WatchlistRepository {
      return INSTANCE ?: synchronized(this) {
        val database = AppDatabase.getDatabase(context)
        val repo = WatchlistRepository(database.watchlistDao())
        INSTANCE = repo
        repo
      }
    }
  }
}
