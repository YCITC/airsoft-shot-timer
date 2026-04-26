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
import androidx.compose.foundation.pager.PagerState
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
fun AccelSettingsPage(viewModel: ShotTimerViewModel, pagerState: PagerState) {
    val focusRequester = remember { FocusRequester() }
    
    val animatedProgress by animateFloatAsState(
        targetValue = ((viewModel.accelerationThreshold - TimerConstants.ACCEL_MIN) / (TimerConstants.ACCEL_MAX - TimerConstants.ACCEL_MIN)).coerceIn(0f, 1f),
        label = "AccelProgressAnimation"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onRotaryScrollEvent {
                val newSens = (viewModel.accelerationThreshold + (it.verticalScrollPixels / 50.0f))
                    .coerceIn(TimerConstants.ACCEL_MIN, TimerConstants.ACCEL_MAX)
                viewModel.updateAccelSens(newSens)
                true
            }
            .focusRequester(focusRequester)
            .focusable(),
        contentAlignment = Alignment.Center
    ) {
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

        CircularProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxSize().padding(12.dp),
            strokeWidth = 8.dp,
            colors = ProgressIndicatorDefaults.colors(
                indicatorColor = Color.Cyan,
                trackColor = Color.Cyan.copy(alpha = 0.1f)
            )
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "ACCEL SENSITIVITY", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Cyan)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "${viewModel.accelerationThreshold.toInt()} G", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "⇅ 模式與重置", fontSize = 9.sp, color = Color.Gray)
        }
    }
}
