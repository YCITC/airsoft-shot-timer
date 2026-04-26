package com.example.airsoftshottimer.presentation.pages

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ProgressIndicatorDefaults
import androidx.wear.compose.material3.Text
import com.example.airsoftshottimer.presentation.ShotTimerViewModel
import com.example.airsoftshottimer.presentation.TimerConstants

@Composable
fun MicSettingsPage(viewModel: ShotTimerViewModel) {
    val focusRequester = remember { FocusRequester() }
    
    val animatedProgress by animateFloatAsState(
        targetValue = ((viewModel.noiseThresholdDb - TimerConstants.MIC_MIN) / (TimerConstants.MIC_MAX - TimerConstants.MIC_MIN)).toFloat().coerceIn(0f, 1f),
        label = "MicProgressAnimation"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .onRotaryScrollEvent {
                val newSens = (viewModel.noiseThresholdDb + (it.verticalScrollPixels / 50.0))
                    .coerceIn(TimerConstants.MIC_MIN, TimerConstants.MIC_MAX)
                viewModel.updateMicSens(newSens)
                true
            }
            .focusRequester(focusRequester)
            .focusable(),
        contentAlignment = Alignment.Center
    ) {
        // 每次進入此 Composable 時都請求焦點
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxSize().padding(12.dp),
            strokeWidth = 8.dp,
            colors = ProgressIndicatorDefaults.colors(
                indicatorColor = Color.Yellow,
                trackColor = Color.Yellow.copy(alpha = 0.1f)
            )
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "MIC SENSITIVITY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Yellow)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "${viewModel.noiseThresholdDb.toInt()} dB", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "⇅ 模式與重置", fontSize = 9.sp, color = Color.Gray)
        }
    }
}
