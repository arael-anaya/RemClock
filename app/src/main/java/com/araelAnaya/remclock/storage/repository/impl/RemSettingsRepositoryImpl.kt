package com.araelAnaya.remclock.storage.repository.impl

import android.content.Context
import com.araelAnaya.remclock.storage.RemSettingsStorage
import com.araelAnaya.remclock.storage.repository.RemSettingsRepository
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class RemSettingsRepositoryImpl(
    private val context: Context
) : RemSettingsRepository {

    override val remEnabled =
        RemSettingsStorage.remEnabledFlow(context)
            .stateIn(
                CoroutineScope(Dispatchers.IO),
                SharingStarted.Eagerly,
                false
            )

    override val remCycleMinutes =
        RemSettingsStorage.remCycleMinutesFlow(context)
            .stateIn(
                scope = CoroutineScope(Dispatchers.IO),
                started = SharingStarted.Eagerly,
                initialValue = 90
            )

    override suspend fun setRemEnabled(enabled: Boolean) {
        RemSettingsStorage.setRemEnabled(context, enabled)
    }

    override suspend fun setRemCycleMinutes(minutes: Int) {
        RemSettingsStorage.setRemCycleMinutes(context, minutes)
    }
}

