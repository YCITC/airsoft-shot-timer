package com.example.airsoftshottimer.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.airsoftshottimer.data.AppDatabase
import com.example.airsoftshottimer.data.ShotRecord
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).shotDao()

    val shots: StateFlow<List<ShotRecord>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bestShot: StateFlow<ShotRecord?> = dao.getBestShot()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
