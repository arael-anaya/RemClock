package com.araelAnaya.remclock.time

import kotlin.math.abs

data class Time12(
    val hour: Int,    // 1..12
    val minute: Int,  // 0..59
    val isAm: Boolean
)

fun Time12.to24Hour(): Pair<Int, Int> {
    val h24 = when {
        isAm && hour == 12 -> 0
        !isAm && hour == 12 -> 12
        isAm -> hour
        else -> hour + 12
    }
    return h24 to minute
}

fun clampHour12(h: Int): Int = when {
    h < 1 -> 12
    h > 12 -> 1
    else -> h
}

fun clampMinute(m: Int): Int = when {
    m < 0 -> 59
    m > 59 -> 0
    else -> m
}

fun Time12.toMinutesSinceMidnight(): Int {
    val hour24 = when {
        isAm && hour == 12 -> 0        // 12 AM → 0
        !isAm && hour == 12 -> 12      // 12 PM → 12
        isAm -> hour                   // 1–11 AM
        else -> hour + 12              // 1–11 PM
    }
    return hour24 * 60 + minute
}


fun computeSleepDurationMinutes(
    bedtime: Time12,
    wakeTime: Time12
): Int {
    val bed = bedtime.toMinutesSinceMidnight()
    val wake = wakeTime.toMinutesSinceMidnight()

    return if (wake >= bed) {
        wake - bed
    } else {
        (1440 - bed) + wake
    }
}

fun computeRemWakeTimes(
    bedtime: Time12,
    totalSleepMinutes: Int,
    cycleMinutes: Int = 90
): List<Int> {
    val bed = bedtime.toMinutesSinceMidnight()
    val times = mutableListOf<Int>()

    var elapsed = cycleMinutes
    while (elapsed <= totalSleepMinutes) {
        val wake = (bed + elapsed) % 1440
        times.add(wake)
        elapsed += cycleMinutes
    }

    return times
}

fun minutesToTime12(minutes: Int): Time12 {
    val hour24 = (minutes / 60) % 24
    val minute = minutes % 60

    val isAm = hour24 < 12
    val hour12 = when {
        hour24 == 0 -> 12
        hour24 > 12 -> hour24 - 12
        else -> hour24
    }

    return Time12(hour12, minute, isAm)
}

fun findClosestRemTime(
    remTimes: List<Int>,
    alarmTime: Time12
): Int? {
    if (remTimes.isEmpty()) return null

    val alarmMinutes = alarmTime.toMinutesSinceMidnight()

    return remTimes.minByOrNull { rem ->
        abs(rem - alarmMinutes)
    }
}

fun formatSleepDuration(totalMinutes: Int): Pair<Int, Int> {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return hours to minutes
}

