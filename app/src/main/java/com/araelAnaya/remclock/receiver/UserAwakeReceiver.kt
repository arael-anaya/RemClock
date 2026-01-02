package com.araelAnaya.remclock.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.araelAnaya.remclock.alarm.AlarmScheduler

class UserAwakeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_USER_PRESENT -> {
                // User unlocked the phone → assume awake
                AlarmScheduler.cancelAlarm(context.applicationContext)
            }
        }
    }
}
