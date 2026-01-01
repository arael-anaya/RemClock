package com.araelAnaya.remclock.repository

import kotlinx.coroutines.flow.StateFlow

interface RemSettingsRepository {
    val remEnabled: StateFlow<Boolean>
    suspend fun setRemEnabled(enabled: Boolean)
}
