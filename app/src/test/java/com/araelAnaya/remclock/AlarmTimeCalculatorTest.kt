package com.araelAnaya.remclock.alarm

import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class AlarmTimeCalculatorTest {

    @Test
    fun alarmInFuture_sameDay() {
        val now = Calendar.getInstance().apply {
            set(2026, 0, 1, 6, 0, 0) // Jan 1, 06:00
        }.timeInMillis

        val trigger = AlarmTimeCalculator.computeTriggerTimeMillis(
            nowMillis = now,
            hour24 = 8,
            minute = 0
        )

        assertTrue(trigger > now)
    }

    @Test
    fun alarmInPast_rollsToNextDay() {
        val now = Calendar.getInstance().apply {
            set(2026, 0, 1, 9, 0, 0) // Jan 1, 09:00
        }.timeInMillis

        val trigger = AlarmTimeCalculator.computeTriggerTimeMillis(
            nowMillis = now,
            hour24 = 8,
            minute = 0
        )

        val nextDay = Calendar.getInstance().apply {
            set(2026, 0, 2, 8, 0, 0)
        }.timeInMillis

        assertTrue(trigger > now)

    }
}
