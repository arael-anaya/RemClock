package com.araelAnaya.remclock.storage

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.dataStore by preferencesDataStore(
    name = "remclock_prefs"
)
