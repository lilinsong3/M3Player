package com.github.lilinsong3.m3player

import android.content.Context
import android.util.Log
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.lilinsong3.m3player.data.local.AppDatabase
import com.github.lilinsong3.m3player.data.local.dao.PlayListDao
import com.github.lilinsong3.m3player.data.local.dao.SongDao
import com.github.lilinsong3.m3player.data.model.SongKeyModel
import com.github.lilinsong3.m3player.data.model.SongModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
    private lateinit var songDao: SongDao
    private lateinit var playListDao: PlayListDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        songDao = db.songDao()
        playListDao = db.playListDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Throws(Exception::class)
    fun testSongDaoQueryAll() = runTest {

        SongModel(datetime = "2023-11-15").apply {
            songDao.insert(
                path,
                title,
                artist,
                albumTitle,
                albumArtist,
                displayTitle,
                subtitle,
                description,
                artworkLocation,
                lyricLocation,
                recordingYear,
                recordingMonth,
                recordingDay,
                releaseYear,
                releaseMonth,
                releaseDay,
                writer,
                composer,
                conductor,
                discNumber,
                totalDiscCount,
                genre,
                compilation,
                duration,
                datetime
            )
        }
        val list = songDao.queryAll()
        Log.println(Log.ASSERT, "testSongDaoQueryAll()", "query all = $list")
        assertNotEquals(0, list[0].id)
        assertTrue(list[0].datetime.startsWith("2023"))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Throws(Exception::class)
    fun testPlayListDaoQueryById() = runTest {
        val model = playListDao.querySongById(5)
        assertEquals(null, model)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    @Throws(Exception::class)
    fun testPlayListDaoInsertBatch() = runTest {
        val tag = "testPlayListDaoInsertBatch()"
        val list = playListDao.insertByIds(listOf(SongKeyModel(1)))
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