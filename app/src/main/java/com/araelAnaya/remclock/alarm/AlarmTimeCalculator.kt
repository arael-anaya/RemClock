package com.araelAnaya.remclock.alarm

import java.util.Calendar
import java.util.concurrent.TimeUnit

object AlarmTimeCalculator {

    fun computeTriggerTimeMillis(
        nowMillis: Long,
        hour24: Int,
        minute: Int
    ): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            set(Calendar.HOUR_OF_DAY, hour24)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        if (calendar.timeInMillis <= nowMillis) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return calendar.timeInMillis
    }
}

object WakeWindowCalculator {

    private const val WINDOW_BEFORE_MIN = 15
    private const val WINDOW_AFTER_MIN = 5

    data class WakeWindow(
        val windowStartMillis: Long,
        val hardStopMillis: Long
    )

    fun fromTargetTime(targetMillis: Long): WakeWindow {
        val start =
            targetMillis - TimeUnit.MINUTES.toMillis(WINDOW_BEFORE_MIN.toLong())

        val end =
            targetMillis + TimeUnit.MINUTES.toMillis(WINDOW_AFTER_MIN.toLong())

        return WakeWindow(
            windowStartMillis = start,
            hardStopMillis = end
        )
    }
}