package com.araelAnaya.remclock.storage

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import com.araelAnaya.remclock.time.Time12
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object AlarmSettingsStorage {

    private val ALARM_HOUR = intPreferencesKey("alarm_hour")
    private val ALARM_MINUTE = intPreferencesKey("alarm_minute")
    private val ALARM_IS_AM = booleanPreferencesKey("alarm_is_am")

    fun alarmTimeFlow(context: Context): Flow<Time12> =
        context.dataStore.data.map { prefs: Preferences ->
            Time12(
                hour = prefs[ALARM_HOUR] ?: 8,
                minute = prefs[ALARM_MINUTE] ?: 0,
                isAm = prefs[ALARM_IS_AM] ?: true
            )
        }

    suspend fun setAlarmTime(context: Context, time: Time12) {
        context.dataStore.edit { prefs ->
            prefs[ALARM_HOUR] = time.hour
            prefs[ALARM_MINUTE] = time.minute
            prefs[ALARM_IS_AM] = time.isAm
        }
    }
}
