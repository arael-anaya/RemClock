package com.araelAnaya.remclock.storage.repository

import com.araelAnaya.remclock.alarm.AlarmMode
import kotlinx.coroutines.flow.Flow

interface AlarmModeRepository {
    val mode: Flow<AlarmMode>
    suspend fun setMode(mode: AlarmMode)
}
