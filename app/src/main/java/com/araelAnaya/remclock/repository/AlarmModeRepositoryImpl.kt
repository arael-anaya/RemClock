package com.araelAnaya.remclock.repository

import com.araelAnaya.remclock.alarm.AlarmMode
import com.araelAnaya.remclock.storage.AlarmModeStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AlarmModeRepositoryImpl(
    private val storage: AlarmModeStorage
) : AlarmModeRepository {

    private val _mode = MutableStateFlow(storage.getMode())
    override val mode = _mode.asStateFlow()

    override suspend fun setMode(mode: AlarmMode) {
        storage.setMode(mode)
        _mode.value = mode
    }
}
