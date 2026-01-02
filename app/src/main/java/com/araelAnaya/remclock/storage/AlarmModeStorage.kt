package com.araelAnaya.remclock.storage

import android.content.Context
import com.araelAnaya.remclock.alarm.AlarmMode

class AlarmModeStorage(context: Context) {

    private val prefs =
        context.getSharedPreferences("alarm_mode", Context.MODE_PRIVATE)

    fun getMode(): AlarmMode {
        val name = prefs.getString("mode", AlarmMode.EXACT.name)
        return AlarmMode.valueOf(name!!)
    }

    fun setMode(mode: AlarmMode) {
        prefs.edit().putString("mode", mode.name).apply()
    }
}
