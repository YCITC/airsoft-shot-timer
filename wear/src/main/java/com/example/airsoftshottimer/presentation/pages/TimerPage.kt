package com.example.airsoftshottimer.presentation.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Text
import com.example.airsoftshottimer.presentation.ShotTimerViewModel
import com.example.airsoftshottimer.presentation.TimerState
import java.util.Locale

@Composable
fun TimerPage(
    viewModel: ShotTimerViewModel,
    onStartClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = when (viewModel.timerState) {
                TimerState.IDLE -> "START"
                TimerState.DELAYING -> "Delaying..."
                TimerState.RUNNING -> String.format(Locale.US, "%.2f", viewModel.elapsedTime / 1000.0) + "s"
                TimerState.STOPPED -> String.format(Locale.US, "%.2f", viewModel.elapsedTime / 1000.0) + "s"
            },
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = onStartClick) {
            Text(
                text = when (viewModel.timerState) {
                    TimerState.IDLE -> "START"
                    TimerState.DELAYING -> "CANCEL"
                    TimerState.RUNNING -> "STOP"
                    TimerState.STOPPED -> "RESTART"
                }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "滑動進入設定 →", fontSize = 10.sp, color = Color.Gray)
    }
}
