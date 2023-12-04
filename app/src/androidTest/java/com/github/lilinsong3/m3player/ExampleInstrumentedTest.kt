package com.github.lilinsong3.m3player

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.lilinsong3.m3player.data.local.AppDatabase
import com.github.lilinsong3.m3player.data.local.dao.PlayListDao
import com.github.lilinsong3.m3player.data.model.SongIdOnly
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    private lateinit var playListDao: PlayListDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        playListDao = db.playListDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun testPlayListDaoInsertBatch() = runTest {
        val tag = "testPlayListDaoInsertBatch()"
        val list = playListDao.upsertByIds(listOf(SongIdOnly(1)))
        Log.println(Log.ASSERT, tag, "insert list = $list")
        assertEquals(1, list[0])
        val rawList = playListDao.queryAllRaw()
        Log.println(Log.ASSERT, tag, "PlayList = $rawList")
        assertNotNull(rawList[0].datetime)
    }

    @Test
    @Throws(Exception::class)
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.github.lilinsong3.m3player", appContext.packageName)
    }
}