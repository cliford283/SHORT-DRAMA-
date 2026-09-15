package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {

  @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
  fun getAllWatchlist(): Flow<List<WatchlistEntity>>

  @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
  suspend fun getAllWatchlistList(): List<WatchlistEntity>

  @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE id = :id)")
  fun isWatchlisted(id: String): Flow<Boolean>

  @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE id = :id)")
  suspend fun contains(id: String): Boolean

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(item: WatchlistEntity)

  @Query("DELETE FROM watchlist WHERE id = :id")
  suspend fun deleteById(id: String)

  @Delete
  suspend fun delete(item: WatchlistEntity)

  @Query("DELETE FROM watchlist")
  suspend fun clearAll()
}
