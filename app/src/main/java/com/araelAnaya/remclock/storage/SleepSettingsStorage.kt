package com.araelAnaya.remclock.storage

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import com.araelAnaya.remclock.Time12
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object SleepSettingsStorage {

    private val BED_HOUR = intPreferencesKey("bed_hour")
    private val BED_MINUTE = intPreferencesKey("bed_minute")
    private val BED_IS_AM = booleanPreferencesKey("bed_is_am")

    fun bedtimeFlow(context: Context): Flow<Time12> =
        context.dataStore.data.map { prefs: Preferences ->
            Time12(
                hour = prefs[BED_HOUR] ?: 11,
                minute = prefs[BED_MINUTE] ?: 0,
                isAm = prefs[BED_IS_AM] ?: false
            )
        }

    suspend fun setBedtime(context: Context, time: Time12) {
        context.dataStore.edit { prefs ->
            prefs[BED_HOUR] = time.hour
            prefs[BED_MINUTE] = time.minute
            prefs[BED_IS_AM] = time.isAm
        }
    }
}
