package com.araelAnaya.remclock.storage.repository

import kotlinx.coroutines.flow.StateFlow

interface RemSettingsRepository {
    val remEnabled: StateFlow<Boolean>
    val remCycleMinutes: StateFlow<Int>

    suspend fun setRemEnabled(enabled: Boolean)
    suspend fun setRemCycleMinutes(minutes: Int)
}

