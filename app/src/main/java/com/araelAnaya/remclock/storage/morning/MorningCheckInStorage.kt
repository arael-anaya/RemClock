package com.araelAnaya.remclock.storage.morning

import android.content.Context
import java.time.LocalDate

class MorningCheckInStore(context: Context) {
    private val prefs = context.getSharedPreferences("morning_checkin", Context.MODE_PRIVATE)

    private fun today(): String = LocalDate.now().toString()

    fun shouldShow(): Boolean {
        val last = prefs.getString("last_completed_day", null)
        return last != today()
    }

    fun markCompleted() {
        prefs.edit()
            .putString("last_completed_day", today())
            .apply()
    }

}




