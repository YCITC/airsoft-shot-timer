package com.example.airsoftshottimer

import android.util.Log
import com.example.airsoftshottimer.data.AppDatabase
import com.example.airsoftshottimer.data.SessionManager
import com.example.airsoftshottimer.data.ShotRecord
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MessageService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)

        val message = String(messageEvent.data)
        Log.d("MessageService", "Received message: $message from path: ${messageEvent.path}")

        if (messageEvent.path == "/shot_time") {
            val timeFloat = message.toFloatOrNull() ?: return
            val sessionId = SessionManager.create(applicationContext).currentSessionId
            val record = ShotRecord(
                timestamp = System.currentTimeMillis(),
                time = timeFloat,
                sessionId = sessionId
            )
            CoroutineScope(Dispatchers.IO).launch {
                AppDatabase.getDatabase(applicationContext).shotDao().insert(record)
            }
        }
    }
}
