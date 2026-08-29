package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.repository.EmochiRepository
import org.junit.Assert.assertEquals
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
    assertEquals("Velora Ado AI", appName)
  }

  @Test
  fun `test time perception 3 intervals console output`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    val repo = EmochiRepository(db, context)
    val testOutput = repo.runTimePerceptionConsoleTest()
    println(testOutput)
    assertTrue(testOutput.contains("Aralık 1"))
    assertTrue(testOutput.contains("Aralık 2"))
    assertTrue(testOutput.contains("Aralık 3"))
    db.close()
  }
}
