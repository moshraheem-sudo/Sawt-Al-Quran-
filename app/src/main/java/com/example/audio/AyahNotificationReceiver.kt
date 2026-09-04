package com.example.audio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AyahNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            "ACTION_PLAY_PAUSE" -> AyahAudioState.togglePlayPause()
            "ACTION_NEXT" -> AyahAudioState.playNext()
            "ACTION_PREV" -> AyahAudioState.playPrev()
            "ACTION_STOP" -> AyahAudioState.stop()
        }
    }
}

object AyahAudioState {
    var manager: AudioPlayerManager? = null
    
    fun togglePlayPause() { manager?.togglePlayPause() }
    fun playNext() { manager?.playNext() }
    fun playPrev() { manager?.playPrev() }
    fun stop() { manager?.stop() }
}
