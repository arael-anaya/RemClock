package com.araelAnaya.remclock.repository

import android.content.Context
import com.araelAnaya.remclock.time.Time12
import com.araelAnaya.remclock.storage.AlarmSettingsStorage
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class AlarmSettingsRepositoryImpl(
    private val context: Context
) : AlarmSettingsRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    override val alarmTime: StateFlow<Time12> =
        AlarmSettingsStorage
            .alarmTimeFlow(context)
            .stateIn(
                scope,
                SharingStarted.Eagerly,
                Time12(8, 0, true)
            )

    override suspend fun setAlarmTime(time: Time12) {
        AlarmSettingsStorage.setAlarmTime(context, time)
    }
}
