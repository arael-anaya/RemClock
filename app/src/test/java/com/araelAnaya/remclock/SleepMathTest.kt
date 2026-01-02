package com.araelAnaya.remclock

import com.araelAnaya.remclock.time.Time12
import com.araelAnaya.remclock.time.computeRemWakeTimes
import com.araelAnaya.remclock.time.computeSleepDurationMinutes
import org.junit.Assert
import org.junit.Test

class SleepMathTest {

    @Test
    fun sleepDuration_crossesMidnight_correctlyComputed() {
        val bedtime = Time12(11, 0, false) // 11:00 PM
        val wakeTime = Time12(8, 0, true)  // 8:00 AM

        val minutes = computeSleepDurationMinutes(bedtime, wakeTime)

        Assert.assertEquals(540, minutes) // 9 hours * 60
    }

    @Test
    fun remWakeTimes_generated_every90Minutes_includingWakeTime() {
        val bedtime = Time12(11, 0, false) // 11:00 PM
        val wakeTime = Time12(8, 0, true)  // 8:00 AM

        val sleepMinutes = computeSleepDurationMinutes(bedtime, wakeTime)
        val remTimes = computeRemWakeTimes(bedtime, sleepMinutes)

        val expected = listOf(
            30,   // 12:30 AM
            120,  // 2:00 AM
            210,  // 3:30 AM
            300,  // 5:00 AM
            390,  // 6:30 AM
            480   // 8:00 AM (wake-up REM)
        )

        Assert.assertEquals(expected, remTimes)
    }
    @Test
    fun sleepAndRem_whenBedtimeIsMidnight() {
        val bedtime = Time12(12, 0, true)   // 12:00 AM
        val wakeTime = Time12(6, 0, true)   // 6:00 AM

        val sleepMinutes = computeSleepDurationMinutes(bedtime, wakeTime)
        Assert.assertEquals(360, sleepMinutes)

        val remTimes = computeRemWakeTimes(bedtime, sleepMinutes)

        val expected = listOf(
            90,   // 1:30 AM
            180,  // 3:00 AM
            270,  // 4:30 AM
            360   // 6:00 AM
        )

        Assert.assertEquals(expected, remTimes)
    }

    @Test
    fun noRemCycles_whenSleepIsTooShort() {
        val bedtime = Time12(1, 0, true)   // 1:00 AM
        val wakeTime = Time12(2, 0, true)  // 2:00 AM

        val sleepMinutes = computeSleepDurationMinutes(bedtime, wakeTime)
        Assert.assertEquals(60, sleepMinutes)

        val remTimes = computeRemWakeTimes(bedtime, sleepMinutes)

        Assert.assertTrue(remTimes.isEmpty())
    }

    @Test
    fun exactlyOneRemCycle_whenSleepIsNinetyMinutes() {
        val bedtime = Time12(10, 0, false)  // 10:00 PM
        val wakeTime = Time12(11, 30, false) // 11:30 PM

        val sleepMinutes = computeSleepDurationMinutes(bedtime, wakeTime)
        Assert.assertEquals(90, sleepMinutes)

        val remTimes = computeRemWakeTimes(bedtime, sleepMinutes)

        val expected = listOf(
            1410 // 11:30 PM in minutes since midnight
        )

        Assert.assertEquals(expected, remTimes)
    }





}