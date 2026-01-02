package com.araelAnaya.remclock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.araelAnaya.remclock.repository.*
import com.araelAnaya.remclock.time.Time12
import com.araelAnaya.remclock.time.computeRemWakeTimes
import com.araelAnaya.remclock.time.computeSleepDurationMinutes
import com.araelAnaya.remclock.time.findClosestRemTime
import com.araelAnaya.remclock.time.minutesToTime12
import com.araelAnaya.remclock.time.to24Hour
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.araelAnaya.remclock.alarm.AlarmMode
import com.araelAnaya.remclock.repository.AlarmModeRepository
import androidx.lifecycle.SavedStateHandle
import com.araelAnaya.remclock.time.toMinutesSinceMidnight


class MainViewModel(
    private val alarmRepo: AlarmSettingsRepository,
    private val sleepRepo: SleepSettingsRepository,
    private val remRepo: RemSettingsRepository,
    private val alarmModeRepo: AlarmModeRepository,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // ---------- Persisted state ----------

    val alarmTime: StateFlow<Time12> = alarmRepo.alarmTime
    val bedtime: StateFlow<Time12> = sleepRepo.bedtime
    val remEnabled: StateFlow<Boolean> = remRepo.remEnabled
    val alarmMode: StateFlow<AlarmMode> =
        alarmModeRepo.mode.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AlarmMode.EXACT
        )

    // ---------- Derived state ----------

    val sleepMinutes: StateFlow<Int> =
        combine(bedtime, alarmTime) { bed, alarm ->
            computeSleepDurationMinutes(bed, alarm)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            0
        )

    val remWakeTimes: StateFlow<List<Int>> =
        combine(bedtime, sleepMinutes) { bed, minutes ->
            computeRemWakeTimes(bed, minutes)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    val closestRem: StateFlow<Int?> =
        combine(remWakeTimes, alarmTime) { rems, alarm ->
            findClosestRemTime(rems, alarm)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            null
        )

    private val MANUAL_BACKUP_KEY = "manual_time_backup_minutes"

    var manualTimeBackupMinutes: Int?
        get() = savedStateHandle[MANUAL_BACKUP_KEY]
        set(value) {
            savedStateHandle[MANUAL_BACKUP_KEY] = value
        }

    // ---------- Events ----------

    fun updateAlarmTime(update: (Time12) -> Time12) {
        viewModelScope.launch {
            alarmRepo.setAlarmTime(update(alarmTime.value))
        }
    }

    fun setBedtime(time: Time12) {
        viewModelScope.launch {
            sleepRepo.setBedtime(time)
        }
    }

    fun setRemEnabled(enabled: Boolean) {
        viewModelScope.launch {
            remRepo.setRemEnabled(enabled)
        }
    }
    fun useRecommendedRemTime() {
        closestRem.value?.let { minutes ->
            viewModelScope.launch {
                alarmRepo.setAlarmTime(minutesToTime12(minutes))
            }
        }
    }

    fun setAlarmMode(mode: AlarmMode) {
        viewModelScope.launch {
            alarmModeRepo.setMode(mode)
        }
    }
    fun getNextAlarmTime24(): Pair<Int, Int> {
        return alarmTime.value.to24Hour()
    }

    fun onRemTimeClicked(minutes: Int) {
        // If REM is disabled or mode is not SMART, ignore
        if (!remEnabled.value || alarmMode.value != AlarmMode.SMART_WINDOW) return

        val currentAlarmMinutes = alarmTime.value.toMinutesSinceMidnight()

        if (minutes == currentAlarmMinutes) {
            // DESELECT: revert to manual backup if it exists
            manualTimeBackupMinutes?.let { backupMinutes ->
                viewModelScope.launch {
                    alarmRepo.setAlarmTime(minutesToTime12(backupMinutes))
                }
            }
            manualTimeBackupMinutes = null
        } else {
            // SELECT: save backup once, then apply REM time
            if (manualTimeBackupMinutes == null) {
                manualTimeBackupMinutes = currentAlarmMinutes
            }
            viewModelScope.launch {
                alarmRepo.setAlarmTime(minutesToTime12(minutes))
            }
        }
    }






}
