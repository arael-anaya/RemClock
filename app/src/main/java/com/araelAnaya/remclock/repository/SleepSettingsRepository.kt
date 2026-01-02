package com.araelAnaya.remclock.repository

import com.araelAnaya.remclock.time.Time12
import kotlinx.coroutines.flow.StateFlow

interface SleepSettingsRepository {
    val bedtime: StateFlow<Time12>
    suspend fun setBedtime(time: Time12)
}
