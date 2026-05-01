package com.example.airsoftshottimer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.airsoftshottimer.ui.AirsoftShotTimerTheme
import com.example.airsoftshottimer.ui.HistoryListScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AirsoftShotTimerTheme {
                HistoryListScreen()
            }
        }
    }
}
