package com.example.airsoftshottimer.data

import android.content.Context
import android.content.SharedPreferences

class SessionManager(private val prefs: SharedPreferences) {

    val currentSessionId: Long
        get() {
            val stored = prefs.getLong(KEY, 0L)
            return if (stored == 0L) startNewSession() else stored
        }

    fun startNewSession(): Long {
        val id = System.currentTimeMillis()
        prefs.edit().putLong(KEY, id).apply()
        return id
    }

    companion object {
        private const val KEY = "current_session_id"

        fun create(context: Context): SessionManager =
            SessionManager(context.getSharedPreferences("session", Context.MODE_PRIVATE))
    }
}
