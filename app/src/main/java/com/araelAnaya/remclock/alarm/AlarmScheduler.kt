package com.araelAnaya.remclock.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.araelAnaya.remclock.alarm.AlarmTimeCalculator
import java.util.Calendar

class AlarmScheduler(
    private val context: Context
) {

    private fun pendingIntent(): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java)

        return PendingIntent.getBroadcast(
            context,
            0, // fixed requestCode = identity
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun scheduleOrReplaceAlarm(hour24: Int, minute: Int) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Always cancel first to guarantee replacement
        alarmManager.cancel(pendingIntent())

        val triggerTimeMillis =
            AlarmTimeCalculator.computeTriggerTimeMillis(
                nowMillis = System.currentTimeMillis(),
                hour24 = hour24,
                minute = minute
            )

        alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            triggerTimeMillis,
            pendingIntent()
        )
    }

    fun cancelAlarm() {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        alarmManager.cancel(pendingIntent())
    }


}
