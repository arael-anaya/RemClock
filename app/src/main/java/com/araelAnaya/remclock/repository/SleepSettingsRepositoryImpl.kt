package com.araelAnaya.remclock.repository

import android.content.Context
import com.araelAnaya.remclock.Time12
import com.araelAnaya.remclock.storage.SleepSettingsStorage
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class SleepSettingsRepositoryImpl(
    private val context: Context
) : SleepSettingsRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    override val bedtime: StateFlow<Time12> =
        SleepSettingsStorage
            .bedtimeFlow(context)
            .stateIn(
                scope,
                SharingStarted.Eagerly,
                Time12(11, 0, false)
            )

    override suspend fun setBedtime(time: Time12) {
        SleepSettingsStorage.setBedtime(context, time)
    }
}
