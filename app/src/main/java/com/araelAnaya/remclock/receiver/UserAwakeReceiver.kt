package com.araelAnaya.remclock.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.araelAnaya.remclock.alarm.AlarmScheduler
import com.araelAnaya.remclock.alarm.WakeConfidenceEvaluator

class UserAwakeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        WakeConfidenceEvaluator.recordUserInteraction()
    }

}
