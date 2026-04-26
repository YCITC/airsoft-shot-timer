package com.example.airsoftshottimer

import android.util.Log
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class MessageService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)

        // Check for the correct message path
        if (messageEvent.path == "/ping_path") {
            // Decode the message
            val message = String(messageEvent.data)
            Log.d("MessageService", "Received message: $message from node: ${messageEvent.sourceNodeId}")
        }
    }
}
