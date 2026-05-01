package com.example.airsoftshottimer.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.airsoftshottimer.data.ShotRecord
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private val DELETE_BUTTON_WIDTH = 80.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryListScreen(viewModel: HistoryViewModel = viewModel()) {
    val groupedShots by viewModel.groupedShots.collectAsState()
    val bestShot by viewModel.bestShot.collectAsState()
    val bestId = bestShot?.id

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shot History") },
                actions = {
                    IconButton(onClick = { viewModel.startNewSession() }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "開始新訓練"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (groupedShots.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("No shots recorded yet.", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                groupedShots.forEach { (sessionId, shots) ->
                    item(key = "header_$sessionId") {
                        SwipeToRevealSessionHeader(
                            sessionId = sessionId,
                            onDelete = { viewModel.deleteSession(sessionId) }
                        )
                    }
                    items(shots, key = { it.id }) { shot ->
                        ShotItem(shot = shot, isBest = shot.id == bestId)
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun SwipeToRevealSessionHeader(
    sessionId: Long,
    onDelete: () -> Unit
) {
    val density = LocalDensity.current
    val deleteWidthPx = with(density) { DELETE_BUTTON_WIDTH.toPx() }
    val scope = rememberCoroutineScope()
    val offsetX = remember(sessionId) { Animatable(0f) }

    Box(modifier = Modifier.fillMaxWidth()) {
        // Delete button revealed behind
        Box(
            modifier = Modifier
                .width(DELETE_BUTTON_WIDTH)
                .matchParentSize()
                .align(Alignment.CenterEnd)
                .background(Color(0xFFD32F2F)),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(80.dp)
                    .clickable {
                        scope.launch {
                            offsetX.animateTo(0f, tween(150))
                            onDelete()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "刪除",
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // Session header (draggable layer)
        SessionHeader(
            sessionId = sessionId,
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        scope.launch {
                            val newOffset = (offsetX.value + delta).coerceIn(-deleteWidthPx, 0f)
                            offsetX.snapTo(newOffset)
                        }
                    },
                    onDragStopped = { velocity ->
                        scope.launch {
                            val target = if (-offsetX.value > deleteWidthPx / 2 || velocity < -500f) {
                                -deleteWidthPx
                            } else {
                                0f
                            }
                            offsetX.animateTo(target, tween(200))
                        }
                    }
                )
        )
    }
    HorizontalDivider()
}

@Composable
private fun SessionHeader(sessionId: Long, modifier: Modifier = Modifier) {
    val dateStr = SimpleDateFormat("yyyy-MM-dd  HH:mm", Locale.getDefault())
        .format(Date(sessionId))

    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = dateStr,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "← 左滑刪除",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
    }
}

@Composable
private fun ShotItem(shot: ShotRecord, isBest: Boolean = false) {
    val dateStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        .format(Date(shot.timestamp))
    val backgroundColor = if (isBest) MaterialTheme.colorScheme.primaryContainer
                          else MaterialTheme.colorScheme.surface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isBest) {
                Text("★", color = MaterialTheme.colorScheme.primary)
            }
            Text(
                text = "%.2f s".format(shot.time),
                style = MaterialTheme.typography.titleMedium
            )
        }
        Text(
            text = dateStr,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
