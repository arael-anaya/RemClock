package com.araelAnaya.remclock.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.araelAnaya.remclock.alarm.AlarmScheduler
import com.araelAnaya.remclock.repository.AlarmSettingsRepositoryImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import com.araelAnaya.remclock.to24Hour


class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val appContext = context.applicationContext

        // We are in a BroadcastReceiver → must be fast
        CoroutineScope(Dispatchers.IO).launch {
            val alarmRepo = AlarmSettingsRepositoryImpl(appContext)
            val scheduler = AlarmScheduler(appContext)

            val alarmTime = alarmRepo.alarmTime.first()
            val time24 = alarmTime.to24Hour()
            val hour24 = time24.first
            val minute = time24.second

            scheduler.scheduleOrReplaceAlarm(hour24, minute)
        }
    }
}
