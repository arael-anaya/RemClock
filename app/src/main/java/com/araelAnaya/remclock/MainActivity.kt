package com.araelAnaya.remclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.araelAnaya.remclock.alarm.*
import com.araelAnaya.remclock.repository.*
import com.araelAnaya.remclock.ui.theme.RemClockTheme
import com.araelAnaya.remclock.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RemClockTheme {

                // Use applicationContext so we don't accidentally hold an Activity reference
                val appContext = LocalContext.current.applicationContext

                // Remember the factory so we don't recreate it on every recomposition
                val vmFactory = remember(appContext) {
                    object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            @Suppress("UNCHECKED_CAST")
                            return MainViewModel(
                                alarmRepo = AlarmSettingsRepositoryImpl(appContext),
                                sleepRepo = SleepSettingsRepositoryImpl(appContext),
                                remRepo = RemSettingsRepositoryImpl(appContext),
                                alarmScheduler = AlarmScheduler(appContext)
                            ) as T
                        }
                    }
                }

                val vm: MainViewModel = viewModel(factory = vmFactory)

                val locale = LocalConfiguration.current.locales[0]
                val amPm = java.text.DateFormatSymbols(locale).amPmStrings
                val amLabel = amPm.getOrNull(0) ?: "AM"
                val pmLabel = amPm.getOrNull(1) ?: "PM"

                val remEnabled by vm.remEnabled.collectAsState()
                val alarmTime by vm.alarmTime.collectAsState()
                val bedTime by vm.bedtime.collectAsState()
                val sleepMinutes by vm.sleepMinutes.collectAsState()

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

                    Spacer(Modifier.height(24.dp))

                    Text(
                        text = "${alarmTime.hour.toString().padStart(2, '0')}:${
                            alarmTime.minute.toString().padStart(2, '0')
                        } ${if (alarmTime.isAm) amLabel else pmLabel}",
                        fontSize = 28.sp
                    )

                    Spacer(Modifier.height(18.dp))

                    TimeInput(
                        value = alarmTime,
                        onChange = vm::updateAlarmTime
                    )

                    Spacer(Modifier.height(32.dp))

                    Button(onClick = {
                        ensureExactAlarmPermission()
                        vm.scheduleAlarmFromCurrentTime()
                    }) {
                        Text("Set Alarm")
                    }
                    Button(onClick = vm::cancelAlarm) {
                        Text("Cancel Alarm")
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(onClick = { showBedtimeDialog = true }) {
                        Text("Set Bedtime")
                    }

                    if (showBedtimeDialog) {
                        BedtimeDialog(
                            initialTime = bedTime,
                            onDismiss = { showBedtimeDialog = false },
                            onSave = { newTime ->
                                vm.setBedtime(newTime)
                                showBedtimeDialog = false
                            }
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Bedtime: ${bedTime.hour}:${
                            bedTime.minute.toString().padStart(2, '0')
                        } ${if (bedTime.isAm) "AM" else "PM"}"
                    )

                    val (sleepHours, sleepRemainderMinutes) = formatSleepDuration(sleepMinutes)

                    Spacer(Modifier.height(12.dp))

                    Text("Sleep Duration", fontWeight = FontWeight.Bold)
                    Text("$sleepHours hours $sleepRemainderMinutes minutes", fontSize = 18.sp)

                    Spacer(Modifier.height(24.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("REM Mode", fontWeight = FontWeight.Medium)
                        Spacer(Modifier.width(12.dp))
                        Switch(
                            checked = remEnabled,
                            onCheckedChange = vm::setRemEnabled,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = androidx.compose.ui.graphics.Color(0xFF1E88E5)
                            )
                        )
                    }

                    if (remEnabled) {
                        val remWakeTimes by vm.remWakeTimes.collectAsState()
                        val closestRem = findClosestRemTime(remWakeTimes, alarmTime)

                        Spacer(Modifier.height(16.dp))

                        Text("Recommended wake times", fontWeight = FontWeight.Bold)

                        remWakeTimes.forEach { minutes ->
                            val t = minutesToTime12(minutes)
                            val isClosest = minutes == closestRem

                            Text(
                                text = "${t.hour}:${t.minute.toString().padStart(2, '0')} ${if (t.isAm) "AM" else "PM"}",
                                color = if (isClosest)
                                    androidx.compose.ui.graphics.Color(0xFF1E88E5)
                                else
                                    androidx.compose.ui.graphics.Color.Unspecified,
                                fontWeight = if (isClosest) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        if (closestRem != null) {
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = vm::useRecommendedRemTime) {
                                Text("Use Recommended Time")
                            }
                        }
                    }
                }
            }
        }
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
