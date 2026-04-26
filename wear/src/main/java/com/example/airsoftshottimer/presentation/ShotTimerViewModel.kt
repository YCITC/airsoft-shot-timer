package com.example.airsoftshottimer.presentation

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel

class ShotTimerViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("shot_timer_prefs", Context.MODE_PRIVATE)

    // 共享狀態 - 計時相關
    var timerState by mutableStateOf(TimerState.IDLE)
    var elapsedTime by mutableStateOf(0L)
    
    // 共享狀態 - 設定相關 (從 SharedPreferences 載入)
    var detectionMode by mutableStateOf(
        DetectionMode.valueOf(prefs.getString("detection_mode", DetectionMode.MICROPHONE.name)!!)
    )
        private set

    var noiseThresholdDb by mutableStateOf(prefs.getFloat("mic_sens", TimerConstants.DEFAULT_MIC_SENS.toFloat()).toDouble())
        private set

    var accelerationThreshold by mutableStateOf(prefs.getFloat("accel_sens", TimerConstants.DEFAULT_ACCEL_SENS))
        private set

    // 更新並儲存的方法
    fun updateDetectionMode(mode: DetectionMode) {
        detectionMode = mode
        prefs.edit().putString("detection_mode", mode.name).apply()
    }

    fun updateMicSens(sens: Double) {
        noiseThresholdDb = sens
        prefs.edit().putFloat("mic_sens", sens.toFloat()).apply()
    }

    fun updateAccelSens(sens: Float) {
        accelerationThreshold = sens
        prefs.edit().putFloat("accel_sens", sens).apply()
    }

    fun resetToFactory() {
        updateDetectionMode(DetectionMode.ACCELEROMETER)
        updateMicSens(TimerConstants.DEFAULT_MIC_SENS)
        updateAccelSens(TimerConstants.DEFAULT_ACCEL_SENS)
        timerState = TimerState.IDLE
        elapsedTime = 0L
    }
}
