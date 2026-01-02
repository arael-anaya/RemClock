package com.araelAnaya.remclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.araelAnaya.remclock.alarm.*
import com.araelAnaya.remclock.repository.*
import com.araelAnaya.remclock.storage.AlarmModeStorage
import com.araelAnaya.remclock.time.findClosestRemTime
import com.araelAnaya.remclock.time.formatSleepDuration
import com.araelAnaya.remclock.time.minutesToTime12
import com.araelAnaya.remclock.ui.theme.RemClockTheme
import com.araelAnaya.remclock.viewmodel.MainViewModel
import androidx.lifecycle.SavedStateHandle



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RemClockTheme {
                val appContext = LocalContext.current.applicationContext
                val vmFactory = remember(appContext) {
                    object : AbstractSavedStateViewModelFactory(this@MainActivity, null) {
                        override fun <T : ViewModel> create(
                            key: String,
                            modelClass: Class<T>,
                            handle: SavedStateHandle
                        ): T {
                            @Suppress("UNCHECKED_CAST")
                            return MainViewModel(
                                alarmRepo = AlarmSettingsRepositoryImpl(appContext),
                                sleepRepo = SleepSettingsRepositoryImpl(appContext),
                                remRepo = RemSettingsRepositoryImpl(appContext),
                                alarmModeRepo = AlarmModeRepositoryImpl(
                                    AlarmModeStorage(appContext)
                                ),
                                savedStateHandle = handle
                            ) as T
                        }
                    }
                }


                val vm: MainViewModel = viewModel(factory = vmFactory)
                RemClockScreen(vm)
            }
        }
    }

    fun ensureExactAlarmPermission() {
        val alarmManager = getSystemService(android.app.AlarmManager::class.java)
        if (!alarmManager.canScheduleExactAlarms()) {
            val intent = android.content.Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
            startActivity(intent)
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemClockScreen(vm: MainViewModel) {
    val context = LocalContext.current
    val alarmTime by vm.alarmTime.collectAsState()
    val bedTime by vm.bedtime.collectAsState()
    val sleepMinutes by vm.sleepMinutes.collectAsState()
    val alarmMode by vm.alarmMode.collectAsState()
    val remEnabled by vm.remEnabled.collectAsState()

    var showBedtimeDialog by remember { mutableStateOf(false) }
    var showAlarmTimeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("RemClock", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // --- HERO SECTION: ALARM TIME ---
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("WAKE UP AT", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.secondary)
                Text(
                    text = "${alarmTime.hour.toString().padStart(2, '0')}:${alarmTime.minute.toString().padStart(2, '0')} ${if (alarmTime.isAm) "AM" else "PM"}",
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.primary
                )
                Button(onClick = { showAlarmTimeDialog = true }) {
                    Text("Edit Alarm Time")
                }
            }

            // ... (Action Buttons and InfoCard remain the same) ...

            // --- ACTION BUTTONS ---
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        (context as? MainActivity)?.ensureExactAlarmPermission()
                        val (hour24, minute) = vm.getNextAlarmTime24()

                        when (alarmMode) {
                            AlarmMode.EXACT -> AlarmScheduler.scheduleAlarm(context, hour24, minute)
                            AlarmMode.SMART_WINDOW -> {
                                val targetMillis = java.util.Calendar.getInstance().apply {
                                    set(java.util.Calendar.HOUR_OF_DAY, hour24)
                                    set(java.util.Calendar.MINUTE, minute)
                                    set(java.util.Calendar.SECOND, 0)
                                    if (timeInMillis <= System.currentTimeMillis()) add(java.util.Calendar.DAY_OF_YEAR, 1)
                                }.timeInMillis
                                val window = WakeWindowCalculator.fromTargetTime(targetMillis)
                                AlarmScheduler.scheduleWakeWindow(context, window.windowStartMillis, window.hardStopMillis)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.Notifications, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Set Alarm")
                }
                OutlinedButton(
                    onClick = { AlarmScheduler.cancelAlarm(context) },
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("Cancel")
                }
            }

            // --- SLEEP INSIGHTS CARD ---
            InfoCard(title = "Sleep Duration", icon = Icons.Default.Star) {
                val (hours, mins) = formatSleepDuration(sleepMinutes)
                Text("$hours hours $mins minutes", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Bedtime: ${bedTime.hour}:${bedTime.minute.toString().padStart(2, '0')} ${if (bedTime.isAm) "AM" else "PM"}")
                    TextButton(onClick = { showBedtimeDialog = true }) {
                        Text("Change Bedtime")
                    }
                }
            }

            // --- REM MODE CARD ---
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("REM Smart Mode", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = if (alarmMode == AlarmMode.SMART_WINDOW) "Waking during light sleep." else "Alarm rings exactly on time.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = alarmMode == AlarmMode.SMART_WINDOW,
                            onCheckedChange = { vm.setAlarmMode(if (it) AlarmMode.SMART_WINDOW else AlarmMode.EXACT) }
                        )
                    }

                    if (remEnabled && alarmMode == AlarmMode.SMART_WINDOW) {
                        val remWakeTimes by vm.remWakeTimes.collectAsState()

                        // 1. Determine which time is "Best" for bolding
                        val closestRemMinutes = findClosestRemTime(remWakeTimes, alarmTime)

                        // 2. Convert current alarm to minutes for exact selection matching
                        val currentAlarmMinutes = remember(alarmTime) {
                            val h24 = when {
                                alarmTime.isAm && alarmTime.hour == 12 -> 0
                                !alarmTime.isAm && alarmTime.hour != 12 -> alarmTime.hour + 12
                                else -> alarmTime.hour
                            }
                            h24 * 60 + alarmTime.minute
                        }

                        Text("Recommended times:", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 16.dp))
                        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            remWakeTimes.take(3).forEach { minutes ->
                                val t = minutesToTime12(minutes)
                                val isSelected = minutes == currentAlarmMinutes
                                val isBest = minutes == closestRemMinutes

                                FilterChip(
                                    selected = isSelected,
                                    onClick = { vm.onRemTimeClicked(minutes) },
                                    label = {
                                        Text(
                                            text = "${t.hour}:${t.minute.toString().padStart(2, '0')} ${if (t.isAm) "AM" else "PM"}",
                                            fontWeight = if (isBest) FontWeight.ExtraBold else FontWeight.Normal
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- DIALOGS ---

    if (showAlarmTimeDialog) {
        SetTimePopup(
            isAlarmMode = true,
            initialTime = alarmTime,
            onDismiss = { showAlarmTimeDialog = false },
            onSave = { newTime ->
                vm.manualTimeBackupMinutes = null
                vm.updateAlarmTime { newTime }
                showAlarmTimeDialog = false
            }
        )
    }

    if (showBedtimeDialog) {
        SetTimePopup(
            isAlarmMode = false,
            initialTime = bedTime,
            onDismiss = { showBedtimeDialog = false },
            onSave = { newTime ->
                vm.setBedtime(newTime)
                showBedtimeDialog = false
            }
        )
    }
}

@Composable
fun InfoCard(title: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}