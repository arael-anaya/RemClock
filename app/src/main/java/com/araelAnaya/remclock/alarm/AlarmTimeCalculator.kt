package com.araelAnaya.remclock.alarm

import java.util.Calendar

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
