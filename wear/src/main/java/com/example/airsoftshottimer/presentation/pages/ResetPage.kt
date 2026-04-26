package com.example.airsoftshottimer.presentation.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.CircularProgressIndicator
import androidx.wear.compose.material3.ProgressIndicatorDefaults
import androidx.wear.compose.material3.Text
import com.example.airsoftshottimer.presentation.ShotTimerViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ResetPage(
    viewModel: ShotTimerViewModel,
    pagerState: PagerState
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var isPressed by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(1.0f) }
    var job by remember { mutableStateOf<Job?>(null) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                            job = scope.launch {
                                progress = 1.0f
                                delay(1000)
                                progress = 0.66f
                                delay(1000)
                                progress = 0.33f
                                delay(1000)

                                // on finish
                                progress = 0.0f
                                viewModel.resetToFactory()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            }

                            try {
                                awaitRelease()
                            } finally {
                                isPressed = false
                                job?.cancel()
                                progress = 1.0f
                            }
                        }
                    )
                },
            colors = ProgressIndicatorDefaults.colors(
                indicatorColor = if (isPressed) Color.Red else Color.Transparent
            ),
            strokeWidth = 8.dp
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "DANGER ZONE", color = Color.Red, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(15.dp))

            Text(text = "Factory Reset")

            Spacer(modifier = Modifier.height(15.dp))
            Text(text = "長按以重設", fontSize = 12.sp, color = Color.White)

            Spacer(modifier = Modifier.height(15.dp))
            Text(
                text = "⇅ 返回模式",
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier.clickable {
                    scope.launch {
                        pagerState.animateScrollToPage(1) // 捲動回 Accel 頁面
                    }
                }
            )
        }
    }
}
