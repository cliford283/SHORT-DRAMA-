package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlist")
data class WatchlistEntity(
  @PrimaryKey val id: String,
  val title: String,
  val genre: String,
  val description: String,
  val coverResId: Int? = null,
  val coverUrl: String? = null,
  val episodesCount: Int = 0,
  val addedAt: Long = System.currentTimeMillis()
)
