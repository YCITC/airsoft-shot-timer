package com.example.airsoftshottimer.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.airsoftshottimer.data.AppDatabase
import com.example.airsoftshottimer.data.SessionManager
import com.example.airsoftshottimer.data.ShotRecord
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).shotDao()
    private val sessionManager = SessionManager.create(application)

    val groupedShots: StateFlow<List<Pair<Long, List<ShotRecord>>>> = dao.getAll()
        .map { records ->
            records.groupBy { it.sessionId }
                .entries
                .sortedByDescending { it.key }
                .map { it.key to it.value }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bestShot: StateFlow<ShotRecord?> = dao.getBestShot()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun startNewSession() {
        sessionManager.startNewSession()
    }

    fun deleteSession(sessionId: Long) {
        viewModelScope.launch { dao.deleteBySession(sessionId) }
    }
}
