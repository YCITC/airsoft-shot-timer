package com.example.airsoftshottimer

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.airsoftshottimer.data.AppDatabase
import com.example.airsoftshottimer.data.ShotDao
import com.example.airsoftshottimer.data.ShotRecord
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {
    private lateinit var shotDao: ShotDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context, AppDatabase::class.java).build()
        shotDao = db.shotDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun writeUserAndReadInList() = runBlocking {
        val record = ShotRecord(timestamp = System.currentTimeMillis(), time = 0.85f)
        shotDao.insert(record)

        val allShots = shotDao.getAll().first()
        assertEquals(allShots[0].time, 0.85f)
    }

    @Test
    @Throws(Exception::class)
    fun getBestShot_returnsRecordWithMinimumTime() = runBlocking {
        shotDao.insert(ShotRecord(timestamp = 1000L, time = 1.0f))
        shotDao.insert(ShotRecord(timestamp = 2000L, time = 0.5f))
        shotDao.insert(ShotRecord(timestamp = 3000L, time = 0.75f))

        val best = shotDao.getBestShot().first()
        assertEquals(0.5f, best?.time)
    }

    @Test
    @Throws(Exception::class)
    fun getBestShot_returnsNullWhenEmpty() = runBlocking {
        val best = shotDao.getBestShot().first()
        assertEquals(null, best)
    }

    @Test
    @Throws(Exception::class)
    fun deleteBySession_removesOnlyTargetSession() = runBlocking {
        val sessionA = 1000L
        val sessionB = 2000L
        shotDao.insert(ShotRecord(timestamp = 100L, time = 0.5f, sessionId = sessionA))
        shotDao.insert(ShotRecord(timestamp = 200L, time = 0.6f, sessionId = sessionA))
        shotDao.insert(ShotRecord(timestamp = 300L, time = 0.7f, sessionId = sessionB))

        shotDao.deleteBySession(sessionA)

        val remaining = shotDao.getAll().first()
        assertEquals(1, remaining.size)
        assertEquals(sessionB, remaining[0].sessionId)
    }

    @Test
    @Throws(Exception::class)
    fun getAll_orderedBySessionIdDescThenTimestampDesc() = runBlocking {
        val oldSession = 1000L
        val newSession = 2000L
        shotDao.insert(ShotRecord(timestamp = 100L, time = 0.9f, sessionId = oldSession))
        shotDao.insert(ShotRecord(timestamp = 300L, time = 0.5f, sessionId = newSession))
        shotDao.insert(ShotRecord(timestamp = 200L, time = 0.7f, sessionId = newSession))

        val all = shotDao.getAll().first()
        // newest session first (2000L), then within session timestamp DESC (300 before 200)
        assertEquals(newSession, all[0].sessionId)
        assertEquals(300L, all[0].timestamp)
        assertEquals(newSession, all[1].sessionId)
        assertEquals(200L, all[1].timestamp)
        assertEquals(oldSession, all[2].sessionId)
    }
}
