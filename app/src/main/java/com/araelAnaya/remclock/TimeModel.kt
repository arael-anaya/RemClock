package com.araelAnaya.remclock

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
