package com.araelAnaya.remclock.repository

import android.content.Context
import com.araelAnaya.remclock.storage.RemSettingsStorage
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class RemSettingsRepositoryImpl(
    private val context: Context
) : RemSettingsRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    override val remEnabled: StateFlow<Boolean> =
        RemSettingsStorage
            .remEnabledFlow(context)
            .stateIn(
                scope,
                SharingStarted.Eagerly,
                false
            )

    override suspend fun setRemEnabled(enabled: Boolean) {
        RemSettingsStorage.setRemEnabled(context, enabled)
    }
}
