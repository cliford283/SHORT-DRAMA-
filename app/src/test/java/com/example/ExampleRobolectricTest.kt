package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import com.example.data.db.WatchlistEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Free Short Drama", appName)
  }

  @Test
  fun `test watchlist dao insert and retrieve`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    val dao = db.watchlistDao()

    val item = WatchlistEntity(
      id = "test_drama_1",
      title = "Test Alpha Drama",
      genre = "Romance",
      description = "A test drama description",
      episodesCount = 45
    )

    dao.insert(item)
    val list = dao.getAllWatchlistList()
    assertEquals(1, list.size)
    assertEquals("test_drama_1", list[0].id)
    assertEquals("Test Alpha Drama", list[0].title)
    assertTrue(dao.contains("test_drama_1"))

    dao.deleteById("test_drama_1")
    val emptyList = dao.getAllWatchlistList()
    assertEquals(0, emptyList.size)
    assertFalse(dao.contains("test_drama_1"))

    db.close()
  }
}
