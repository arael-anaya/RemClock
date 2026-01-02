package com.araelAnaya.remclock.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        val typeString = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_TYPE)
        val alarmType = try {
            AlarmScheduler.AlarmType.valueOf(typeString ?: "")
        } catch (e: Exception) {
            AlarmScheduler.AlarmType.FAILSAFE
        }

        when (alarmType) {

            AlarmScheduler.AlarmType.WINDOW_START -> {
                if (WakeConfidenceEvaluator.shouldWakeNow(context)) {
                    startAlarm(context)
                }
                return
            }

            AlarmScheduler.AlarmType.HARD_STOP,
            AlarmScheduler.AlarmType.FAILSAFE -> {
                startAlarm(context)
            }
        }
    }

    private fun startAlarm(context: Context) {

        // 1. Start foreground alarm sound
        val serviceIntent = Intent(context, AlarmForegroundService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)

        // 2. Launch alarm UI
        val activityIntent = Intent(context, AlarmActivity::class.java).apply {
            flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        context.startActivity(activityIntent)
    }
}
