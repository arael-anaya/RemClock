package com.araelAnaya.remclock.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.araelAnaya.remclock.alarm.AlarmMode
import com.araelAnaya.remclock.alarm.AlarmScheduler
import com.araelAnaya.remclock.alarm.WakeWindowCalculator
import com.araelAnaya.remclock.storage.repository.impl.AlarmModeRepositoryImpl
import com.araelAnaya.remclock.storage.repository.impl.AlarmSettingsRepositoryImpl
import com.araelAnaya.remclock.storage.AlarmModeStorage
import com.araelAnaya.remclock.time.to24Hour
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // --- Load persisted alarm time ---
                val alarmRepo = AlarmSettingsRepositoryImpl(appContext)
                val alarmTime = alarmRepo.alarmTime.first()

                val (hour24, minute) = alarmTime.to24Hour()

                // --- Load alarm mode ---
                val alarmModeRepo = AlarmModeRepositoryImpl(
                    AlarmModeStorage(appContext)
                )
                val alarmMode = alarmModeRepo.mode.first()

                when (alarmMode) {
                    AlarmMode.EXACT -> {
                        // Schedule exact alarm at user-selected time
                        AlarmScheduler.scheduleAlarm(
                            appContext,
                            hour24,
                            minute
                        )
                    }

                    AlarmMode.SMART_WINDOW -> {
                        // Compute next target time in millis
                        val now = System.currentTimeMillis()
                        val targetMillis = Calendar.getInstance().apply {
                            timeInMillis = now
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                            set(Calendar.HOUR_OF_DAY, hour24)
                            set(Calendar.MINUTE, minute)

                            if (timeInMillis <= now) {
                                add(Calendar.DAY_OF_YEAR, 1)
                            }
                        }.timeInMillis

                        val window = WakeWindowCalculator.fromTargetTime(
                            nowMillis = now,
                            targetMillis = targetMillis
                        )

                        // Schedule wake window alarms
                        AlarmScheduler.scheduleWakeWindow(
                            context = appContext,
                            windowStartMillis = window.windowStartMillis,
                            hardStopMillis = window.hardStopMillis
                        )
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
