package com.example.airsoftshottimer.presentation.pages

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.example.airsoftshottimer.presentation.DetectionMode
import com.example.airsoftshottimer.presentation.ShotTimerViewModel

@Composable
fun SettingsPage(viewModel: ShotTimerViewModel) {
    // 預設根據當前模式決定初始頁面
    val initialPage = if (viewModel.detectionMode == DetectionMode.MICROPHONE) 0 else 1
    val verticalPagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { 3 } // Mic, Accel, Reset
    )

    // 連動邏輯：當使用者手動滑動垂直分頁時，更新 ViewModel 中的模式（僅前兩頁）
    LaunchedEffect(verticalPagerState.currentPage) {
        if (!verticalPagerState.isScrollInProgress) {
            when (verticalPagerState.currentPage) {
                0 -> viewModel.updateDetectionMode(DetectionMode.MICROPHONE)
                1 -> viewModel.updateDetectionMode(DetectionMode.ACCELEROMETER)
            }
        }
    }

    // 連動邏輯：如果外部（如 Factory Reset）改變了模式，自動捲動分頁
    LaunchedEffect(viewModel.detectionMode) {
        val targetPage = if (viewModel.detectionMode == DetectionMode.MICROPHONE) 0 else 1
        if (verticalPagerState.currentPage != targetPage && !verticalPagerState.isScrollInProgress) {
            verticalPagerState.animateScrollToPage(targetPage)
        }
    }

    VerticalPager(
        state = verticalPagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        when (page) {
            0 -> MicSettingsPage(viewModel)
            1 -> AccelSettingsPage(viewModel, verticalPagerState)
            2 -> ResetPage(viewModel, verticalPagerState)
        }
    }
}
