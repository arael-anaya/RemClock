package com.araelAnaya.remclock.storage.repository

import com.araelAnaya.remclock.time.Time12
import kotlinx.coroutines.flow.StateFlow

interface AlarmSettingsRepository {
    val alarmTime: StateFlow<Time12>
    suspend fun setAlarmTime(time: Time12)
}
