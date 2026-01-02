package com.araelAnaya.remclock.alarm

import android.content.Context
import android.os.PowerManager
import android.os.SystemClock

object WakeConfidenceEvaluator {

    private const val RECENT_INTERACTION_MS = 2 * 60 * 1000L // 2 minutes

    // Timestamp set when user unlocks phone or opens app
    @Volatile
    private var lastUserInteractionElapsed: Long = 0L

    fun recordUserInteraction() {
        lastUserInteractionElapsed = SystemClock.elapsedRealtime()
    }

    fun shouldWakeNow(context: Context): Boolean {
        val now = SystemClock.elapsedRealtime()

        // 1. Screen is on
        val powerManager =
            context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val screenOn = powerManager.isInteractive

        // 2. User interacted recently
        val interactedRecently =
            now - lastUserInteractionElapsed < RECENT_INTERACTION_MS

        return screenOn && interactedRecently
    }
}
