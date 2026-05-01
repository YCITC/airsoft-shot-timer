package com.example.airsoftshottimer

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.airsoftshottimer.data.SessionManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessionManagerTest {

    private lateinit var manager: SessionManager

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Use a dedicated test prefs file and clear it before each test
        val prefs = context.getSharedPreferences("test_session", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        manager = SessionManager(prefs)
    }

    @Test
    fun currentSessionId_initializesOnFirstAccess() {
        val id = manager.currentSessionId
        assertTrue("sessionId should be a positive timestamp", id > 0L)
    }

    @Test
    fun currentSessionId_returnsSameValueOnSubsequentCalls() {
        val first = manager.currentSessionId
        val second = manager.currentSessionId
        assertEquals(first, second)
    }

    @Test
    fun startNewSession_returnsValueDifferentFromPrevious() {
        val old = manager.currentSessionId
        Thread.sleep(2)
        val new = manager.startNewSession()
        assertNotEquals(old, new)
        assertTrue(new > old)
    }

    @Test
    fun startNewSession_updatesCurrentSessionId() {
        Thread.sleep(2)
        val new = manager.startNewSession()
        assertEquals(new, manager.currentSessionId)
    }
}
