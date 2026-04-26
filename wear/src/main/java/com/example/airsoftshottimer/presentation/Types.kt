package com.example.airsoftshottimer.presentation

enum class TimerState { IDLE, DELAYING, RUNNING, STOPPED }
enum class DetectionMode { MICROPHONE, ACCELEROMETER }

object TimerConstants {
    const val DEFAULT_MIC_SENS = -35.0
    const val MIC_MIN = -55.0
    const val MIC_MAX = -15.0
    
    const val DEFAULT_ACCEL_SENS = 30.0f
    const val ACCEL_MIN = 20.0f
    const val ACCEL_MAX = 50.0f
}
