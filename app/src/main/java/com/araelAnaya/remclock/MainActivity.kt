package com.araelAnaya.remclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.araelAnaya.remclock.ui.theme.RemClockTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.*
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.collectAsState
import com.araelAnaya.remclock.storage.RemPreferences
import com.araelAnaya.remclock.storage.SleepSettingsStorage
import com.araelAnaya.remclock.storage.AlarmSettingsStorage
import kotlinx.coroutines.launch





class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RemClockTheme {
                val locale = androidx.compose.ui.platform.LocalConfiguration.current.locales[0]
                val amPm = java.text.DateFormatSymbols(locale).amPmStrings
                val amLabel = amPm.getOrNull(0) ?: "AM"
                val pmLabel = amPm.getOrNull(1) ?: "PM"

                //store Rem Settings
                val scope = rememberCoroutineScope()

                val remEnabled by RemPreferences
                    .remEnabledFlow(this)
                    .collectAsState(initial = false)


                val alarmTime by AlarmSettingsStorage
                    .alarmTimeFlow(this)
                    .collectAsState(initial = Time12(8, 0, true))

                val bedTime by SleepSettingsStorage
                    .bedtimeFlow(this)
                    .collectAsState(initial = Time12(11, 0, false))

                var showBedtimeDialog by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "RemClock",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "${alarmTime.hour.toString().padStart(2, '0')}:${alarmTime.minute.toString().padStart(2, '0')} ${if (alarmTime.isAm) amLabel else pmLabel}",
                        fontSize = 28.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    TimeInput(
                        value = alarmTime,
                        onChange = { update ->
                            scope.launch {
                                AlarmSettingsStorage.setAlarmTime(
                                    this@MainActivity,
                                    update(alarmTime)
                                )
                            }
                        }
                    )


                    Spacer(modifier = Modifier.height(32.dp))

                    Button(onClick = {
                        ensureExactAlarmPermission()
                        val (h24, m) = alarmTime.to24Hour()
                        scheduleAlarm(h24, m)
                    }) {
                        Text("Set Alarm")
                    }

                    Button(
                        onClick = { showBedtimeDialog = true }
                    ) {
                        Text("Set Bedtime")
                    }

                    if (showBedtimeDialog) {
                        BedtimeDialog(
                            initialTime = bedTime,
                            onDismiss = { showBedtimeDialog = false },
                            onSave = { newBedtime ->
                                scope.launch {
                                    SleepSettingsStorage.setBedtime(this@MainActivity, newBedtime)
                                }
                                showBedtimeDialog = false
                            }

                        )
                    }
                    Text(
                        text = "Bedtime: ${bedTime.hour}:${bedTime.minute.toString().padStart(2, '0')} ${if (bedTime.isAm) "AM" else "PM"}"
                    )

                    val sleepMinutes = computeSleepDurationMinutes(bedTime, alarmTime)
                    val (sleepHours, sleepRemainderMinutes) =
                        formatSleepDuration(sleepMinutes)

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Sleep Duration",
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "$sleepHours hours $sleepRemainderMinutes minutes",
                        fontSize = 18.sp
                    )

                    Spacer(Modifier.height(24.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "REM Mode",
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(Modifier.width(12.dp))

                        Switch(
                            checked = remEnabled,
                            onCheckedChange = { enabled ->
                                scope.launch {
                                    RemPreferences.setRemEnabled(this@MainActivity, enabled)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = androidx.compose.ui.graphics.Color(0xFF1E88E5)
                            )
                        )

                    }

                    if (remEnabled) {
                        val remWakeTimes = computeRemWakeTimes(bedTime, sleepMinutes)
                        val closestRem = findClosestRemTime(remWakeTimes, alarmTime)

                        Spacer(Modifier.height(16.dp))

                        Text(
                            text = "Recommended wake times",
                            fontWeight = FontWeight.Bold
                        )

                        remWakeTimes.forEach { minutes ->
                            val t = minutesToTime12(minutes)
                            val isClosest = minutes == closestRem

                            Text(
                                text = "${t.hour}:${
                                    t.minute.toString().padStart(2, '0')
                                } ${if (t.isAm) "AM" else "PM"}",
                                color = if (isClosest)
                                    androidx.compose.ui.graphics.Color(0xFF1E88E5)
                                else
                                    androidx.compose.ui.graphics.Color.Unspecified,
                                fontWeight = if (isClosest) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        if (closestRem != null) {
                            Spacer(Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    scope.launch {
                                        AlarmSettingsStorage.setAlarmTime(
                                            this@MainActivity,
                                            minutesToTime12(closestRem)
                                        )
                                    }

                                }
                            ) {
                                Text("Use Recommended Time")
                            }
                        }
                    }
                }
            }
        }
    }
     private fun scheduleAlarm(hour: Int, minute: Int) {
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)

            if (before(java.util.Calendar.getInstance())) {
                add(java.util.Calendar.DAY_OF_YEAR, 1)
            }
        }

        val intent = android.content.Intent(this, AlarmReceiver::class.java)
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            this,
            0,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or
                    android.app.PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager =
            getSystemService(android.content.Context.ALARM_SERVICE)
                    as android.app.AlarmManager

        alarmManager.setExact(
            android.app.AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }
    private fun ensureExactAlarmPermission() {
        val alarmManager = getSystemService(android.app.AlarmManager::class.java)
        if (!alarmManager.canScheduleExactAlarms()) {
            val intent = android.content.Intent(
                android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
            )
            startActivity(intent)
        }
    }



}

