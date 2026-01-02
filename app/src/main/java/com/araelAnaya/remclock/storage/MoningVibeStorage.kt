package com.araelAnaya.remclock.storage

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import com.araelAnaya.remclock.viewmodel.model

object MorningVibeStorage {

    private val LAST_VIBE = intPreferencesKey("last_vibe")

    suspend fun recordVibe(context: Context, vibe: model.MorningVibe) {
        context.dataStore.edit { prefs ->
            prefs[LAST_VIBE] = vibe.ordinal
        }
    }
}
