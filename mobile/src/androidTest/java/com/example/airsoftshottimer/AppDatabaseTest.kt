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
}
