package com.araelAnaya.remclock.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar
import java.util.concurrent.TimeUnit

object AlarmScheduler {
    const val EXTRA_ALARM_TYPE = "extra_alarm_type"

    enum class AlarmType {
        WINDOW_START,
        HARD_STOP,
        FAILSAFE
    }


    private const val FAILSAFE_DELAY_MINUTES = 10

    fun scheduleAlarm(context: Context, hour24: Int, minute: Int) {
        val now = System.currentTimeMillis()
        val trigger = nextTriggerTimeMillis(now, hour24, minute)
        scheduleAlarmAtMillis(context, trigger)
    }

    fun scheduleAlarmAtMillis(context: Context, triggerTimeMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val mainIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_TYPE, AlarmType.HARD_STOP.name)
        }


        val mainPending = PendingIntent.getBroadcast(
            context,
            0,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val failsafeIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_TYPE, AlarmType.FAILSAFE.name)
        }

        val failsafePending = PendingIntent.getBroadcast(
            context,
            1,
            failsafeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Main alarm (Doze-safe)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTimeMillis,
            mainPending
        )

        // Failsafe alarm
        val failsafeTime = triggerTimeMillis + TimeUnit.MINUTES.toMillis(FAILSAFE_DELAY_MINUTES.toLong())
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            failsafeTime,
            failsafePending
        )
    }

    fun cancelAlarm(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Cancel main + failsafe
        listOf(0, 1).forEach { requestCode ->
            val intent = Intent(context, AlarmReceiver::class.java)
            val pending = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pending)
        }
    }

    private fun nextTriggerTimeMillis(nowMillis: Long, hour24: Int, minute: Int): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.HOUR_OF_DAY, hour24)
            set(Calendar.MINUTE, minute)
        }

        // If it's already passed for today, schedule for tomorrow
        if (cal.timeInMillis <= nowMillis) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }

        return cal.timeInMillis
    }

    fun scheduleWakeWindow(
        context: Context,
        windowStartMillis: Long,
        hardStopMillis: Long
    ) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val startIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(
                EXTRA_ALARM_TYPE,
                AlarmType.WINDOW_START.name
            )
        }


        val startPending = PendingIntent.getBroadcast(
            context,
            100,
            startIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        val hardStopIntent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(
                EXTRA_ALARM_TYPE,
                AlarmType.HARD_STOP.name
            )
        }


        val hardStopPending = PendingIntent.getBroadcast(
            context,
            1,
            hardStopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Wake window start
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            windowStartMillis,
            startPending
        )

        // Guaranteed wake
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            hardStopMillis,
            hardStopPending
        )
    }


    fun snooze(
        context: Context,
        alarmMode: AlarmMode,
        targetMillis: Long? = null
    ) {
        val now = System.currentTimeMillis()

        when (alarmMode) {
            AlarmMode.EXACT -> {
                val snoozeTime = now + 9 * 60 * 1000L
                scheduleAlarmAtMillis(context, snoozeTime)
            }

            AlarmMode.SMART_WINDOW -> {
                if (targetMillis == null) {
                    // Fallback: behave like exact snooze
                    val snoozeTime = now + 9 * 60 * 1000L
                    scheduleAlarmAtMillis(context, snoozeTime)
                    return
                }

                val now = System.currentTimeMillis()
                val window = WakeWindowCalculator.fromTargetTime(
                    nowMillis = now,
                    targetMillis = targetMillis
                )

                val snoozeCandidate = now + 9 * 60 * 1000L

                val finalTime =
                    if (snoozeCandidate <= window.hardStopMillis)
                        snoozeCandidate
                    else
                        window.hardStopMillis

                scheduleAlarmAtMillis(context, finalTime)
            }
        }
    }



}
