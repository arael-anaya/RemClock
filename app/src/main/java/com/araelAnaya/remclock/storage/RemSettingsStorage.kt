package com.araelAnaya.remclock.storage

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import com.araelAnaya.remclock.storage.dataStore


object RemSettingsStorage {

    private val REM_ENABLED = booleanPreferencesKey("rem_enabled")

    fun remEnabledFlow(context: Context): Flow<Boolean> =
        context.dataStore.data.map { prefs ->
            prefs[REM_ENABLED] ?: false
        }

    suspend fun setRemEnabled(context: Context, enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[REM_ENABLED] = enabled
        }
    }
}
