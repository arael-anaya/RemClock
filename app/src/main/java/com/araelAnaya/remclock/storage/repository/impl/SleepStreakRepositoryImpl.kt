package com.araelAnaya.remclock.storage.repository.impl

import android.content.Context
import com.araelAnaya.remclock.storage.SleepStreakStorage
import com.araelAnaya.remclock.storage.repository.SleepStreakRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class SleepStreakRepositoryImpl(
    private val context: Context
) : SleepStreakRepository {

    override val streak =
        SleepStreakStorage.streakFlow(context)
            .stateIn(
                CoroutineScope(Dispatchers.IO),
                SharingStarted.Eagerly,
                0
            )

    override suspend fun recordNight(success: Boolean) {
        SleepStreakStorage.recordNight(context, success)
    }
}
