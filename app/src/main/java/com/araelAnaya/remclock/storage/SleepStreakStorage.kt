package com.araelAnaya.remclock.storage

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import androidx.datastore.preferences.core.edit

object SleepStreakStorage {

    private val STREAK_COUNT = intPreferencesKey("sleep_streak_count")
    private val LAST_SUCCESS_DATE = longPreferencesKey("last_success_date")

    fun streakFlow(context: Context): Flow<Int> =
        context.dataStore.data.map { prefs ->
            prefs[STREAK_COUNT] ?: 0
        }

    suspend fun recordNight(
        context: Context,
        success: Boolean
    ) {
        context.dataStore.edit { prefs ->
            val today = java.time.LocalDate.now().toEpochDay()
            val lastDay = prefs[LAST_SUCCESS_DATE]

            val newStreak = when {
                !success -> 0
                lastDay == today - 1 -> (prefs[STREAK_COUNT] ?: 0) + 1
                lastDay == today -> prefs[STREAK_COUNT] ?: 1
                else -> 1
            }

            prefs[STREAK_COUNT] = newStreak
            prefs[LAST_SUCCESS_DATE] = today
        }
    }
}
