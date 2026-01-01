package com.araelAnaya.remclock.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.araelAnaya.remclock.*
import com.araelAnaya.remclock.repository.*
import com.araelAnaya.remclock.alarm.AlarmScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val alarmRepo: AlarmSettingsRepository,
    private val sleepRepo: SleepSettingsRepository,
    private val remRepo: RemSettingsRepository,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    // ---------- Persisted state ----------

    val alarmTime: StateFlow<Time12> = alarmRepo.alarmTime

    val bedtime: StateFlow<Time12> = sleepRepo.bedtime

    val remEnabled: StateFlow<Boolean> = remRepo.remEnabled

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

    fun scheduleAlarmFromCurrentTime() {
        val (hour24, minute) = alarmTime.value.to24Hour()
        alarmScheduler.scheduleOrReplaceAlarm(hour24, minute)
    }

    fun cancelAlarm() {
        alarmScheduler.cancelAlarm()
    }


}
