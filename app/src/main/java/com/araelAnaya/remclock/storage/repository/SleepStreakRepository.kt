package com.araelAnaya.remclock.storage.repository


import com.araelAnaya.remclock.time.Time12
import kotlinx.coroutines.flow.StateFlow

interface SleepStreakRepository {
    val streak: StateFlow<Int>
    suspend fun recordNight(success: Boolean)
}
