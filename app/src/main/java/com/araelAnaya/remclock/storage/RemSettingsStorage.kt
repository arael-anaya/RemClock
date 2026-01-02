package com.araelAnaya.remclock.storage

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.edit



object RemSettingsStorage {

    private val REM_ENABLED =
        booleanPreferencesKey("rem_enabled")

    private val REM_CYCLE_MINUTES =
        intPreferencesKey("rem_cycle_minutes")

    fun remEnabledFlow(context: Context): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[REM_ENABLED] ?: false
        }

    fun remCycleMinutesFlow(context: Context): Flow<Int> =
        context.dataStore.data.map { prefs ->
            prefs[REM_CYCLE_MINUTES] ?: 90
        }

    suspend fun setRemEnabled(
        context: Context,
        enabled: Boolean
    ) {
        context.dataStore.edit { prefs ->
            prefs[REM_ENABLED] = enabled
        }
    }

    suspend fun setRemCycleMinutes(
        context: Context,
        minutes: Int
    ) {
        context.dataStore.edit { prefs ->
            prefs[REM_CYCLE_MINUTES] =
                minutes.coerceIn(70, 110)
        }
    }
}
